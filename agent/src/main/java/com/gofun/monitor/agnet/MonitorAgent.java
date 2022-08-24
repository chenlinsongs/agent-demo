package com.gofun.monitor.agnet;


import com.gofun.monitor.loader.MonitorClassloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;

public class MonitorAgent {

    private static final String CORE_JAR = "core-1.0-SNAPSHOT-jar-with-dependencies.jar";

    private static final String BOOTSTRAP = "com.gofun.agent.core.Bootstrap";

    private static final String GET_INSTANCE = "getInstance";

    private static final String PRINT = "startPrintThread";

    private static PrintStream ps = System.err;

    static {
        try {
            File arthasLogDir = new File(System.getProperty("user.home") + File.separator + "logs" + File.separator
                    + "monitor" + File.separator);
            if (!arthasLogDir.exists()) {
                arthasLogDir.mkdirs();
            }
            if (!arthasLogDir.exists()) {
                // #572
                arthasLogDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "logs" + File.separator
                        + "monitor" + File.separator);
                if (!arthasLogDir.exists()) {
                    arthasLogDir.mkdirs();
                }
            }

            File log = new File(arthasLogDir, "monitor.log");

            if (!log.exists()) {
                log.createNewFile();
            }
            ps = new PrintStream(new FileOutputStream(log, true));
        } catch (Throwable t) {
            t.printStackTrace(ps);
        }
    }

    public static void premain(String args, Instrumentation inst) {
        main(args, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        main(args, inst);
    }

    private static synchronized void main(String args, final Instrumentation inst){
        try {
            ps.println("Arthas server agent start...");
            // 传递的args参数分两个部分:arthasCoreJar路径和agentArgs, 分别是Agent的JAR包路径和期望传递到服务端的参数
            if (args == null) {
                args = "";
            }
            args = decodeArg(args);

            String arthasCoreJar;
            final String agentArgs;
            int index = args.indexOf(';');
            if (index != -1) {
                arthasCoreJar = args.substring(0, index);
                agentArgs = args.substring(index+1);
            } else {
                arthasCoreJar = "";
                agentArgs = args;
            }

            File arthasCoreJarFile = new File(arthasCoreJar);
            if (!arthasCoreJarFile.exists()) {
                ps.println("Can not find core jar file from args: " + arthasCoreJarFile);
                // try to find from arthas-agent.jar directory
                CodeSource codeSource = MonitorAgent.class.getProtectionDomain().getCodeSource();
                if (codeSource != null) {
                    try {
                        File arthasAgentJarFile = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
                        ps.println("parent File: " + arthasAgentJarFile.getParentFile().getAbsolutePath());
                        arthasCoreJarFile = new File(arthasAgentJarFile.getParentFile(), CORE_JAR);
                        if (!arthasCoreJarFile.exists()) {
                            ps.println("Can not find core jar file from agent jar directory: " + arthasAgentJarFile);
                        }
                    } catch (Throwable e) {
                        ps.println("Can not find core jar file from " + codeSource.getLocation());
                        e.printStackTrace(ps);
                    }
                }
            }
            if (!arthasCoreJarFile.exists()) {
                return;
            }

            /**
             * Use a dedicated thread to run the binding logic to prevent possible memory leak. #195
             */
            final ClassLoader agentLoader = getClassLoader(inst, arthasCoreJarFile);

            Thread bindingThread = new Thread() {
                @Override
                public void run() {
                    try {
                        bind(inst, agentLoader, agentArgs);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace(ps);
                    }
                }
            };

            bindingThread.setName("monitor-binding-thread");
            bindingThread.start();
            bindingThread.join();
        } catch (Throwable t) {
            t.printStackTrace(ps);
            try {
                if (ps != System.err) {
                    ps.close();
                }
            } catch (Throwable tt) {
                // ignore
            }
            throw new RuntimeException(t);
        }
    }

    private static void bind(Instrumentation inst, ClassLoader agentLoader, String args) throws Throwable {
        Class<?> bootstrapClass = agentLoader.loadClass(BOOTSTRAP);
        Object bootstrap = bootstrapClass.getMethod(GET_INSTANCE, Instrumentation.class, String.class).invoke(null, inst, args);
        bootstrapClass.getMethod(PRINT).invoke(bootstrap);
        ps.println("monitor agent already start.");
        System.out.println("param is:"+args);
        System.out.println("sleep 10 second");
        Thread.sleep(10000);
        System.out.println("exec restore...");
        //复原代码
        bootstrapClass.getMethod("restore").invoke(bootstrap);
        ps.println("exec restored.");
    }

    private static ClassLoader getClassLoader(Instrumentation inst, File arthasCoreJarFile) throws Throwable {
        // 构造自定义的类加载器，尽量减少monitor对现有工程的侵蚀
        return loadOrDefineClassLoader(arthasCoreJarFile);
    }

    private static ClassLoader loadOrDefineClassLoader(File arthasCoreJarFile) throws Throwable {
        return new MonitorClassloader(new URL[]{arthasCoreJarFile.toURI().toURL()});
    }

    private static String decodeArg(String arg) {
        try {
            return URLDecoder.decode(arg, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return arg;
        }
    }
}
