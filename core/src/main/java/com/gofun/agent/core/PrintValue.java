package com.gofun.agent.core;

import com.gofun.agent.core.advisor.MonitorClassFileTransformer;

import java.io.File;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.security.CodeSource;
import java.spy.SpyApi;
import java.util.jar.JarFile;

public class PrintValue {

    private static final String SPY_JAR = "spy-1.0-SNAPSHOT.jar";

    Thread printThread;

    int run = 1;

    private static PrintValue printValue;
    private static PrintStream ps = System.err;

    private Instrumentation instrumentation;

    private PrintValue(Instrumentation instrumentation) throws Throwable {
        this.instrumentation = instrumentation;
        initSpy();

        Class[] classes = instrumentation.getAllLoadedClasses();
        Class<?> targetCls = null;
        ClassLoader targetClassLoader = null;
        for (Class clazz:classes){
            if ("com.gofun.application.Application".equals(clazz.getName())){
                ps.println("loadedClass:"+clazz.getName());
                targetCls = clazz;
                targetClassLoader = targetCls.getClassLoader();

                instrumentation.addTransformer(new MonitorClassFileTransformer(targetCls.getName(), targetClassLoader), true);
                try {
                    instrumentation.retransformClasses(targetCls);
                } catch (Exception ex) {
                    throw new RuntimeException("Transform failed for class: [" + targetCls.getName() + "]", ex);
                }
                return;
            }
        }
    }

    /**
     * 单例
     *
     * @param instrumentation JVM增强
     * @return ArthasServer单例
     * @throws Throwable
     */
    public synchronized static PrintValue getInstance(Instrumentation instrumentation, String args) throws Throwable {
        if (printValue != null) {
            return printValue;
        }
        printValue = new PrintValue(instrumentation);
        return printValue;
    }

    public void startPrintThread(){
        ps.println("call startPrintThread method");
        String applicationMethod = "printVar";
        printThread = new Thread(new Runnable() {
            public void run() {
                while (run == 1){
                    try {
                        try {
                            Thread.sleep(1200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        SpyApi.getObject().getClass().getMethod(applicationMethod).invoke( SpyApi.getObject());

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        });
        printThread.start();
    }

//    public Object callApplicationMethod(){
//
//    }

    private void initSpy() throws Throwable {
        // TODO init SpyImpl ?

        // 将Spy添加到BootstrapClassLoader
        ClassLoader parent = ClassLoader.getSystemClassLoader().getParent();
        Class<?> spyClass = null;
        if (parent != null) {
            try {
                spyClass =parent.loadClass("java.spy.SpyApi");
            } catch (Throwable e) {
                // ignore
            }
        }
        if (spyClass == null) {
            CodeSource codeSource = PrintValue.class.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                File arthasCoreJarFile = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
                File spyJarFile = new File(arthasCoreJarFile.getParentFile(), SPY_JAR);
                instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(spyJarFile));
            } else {
                throw new IllegalStateException("can not find " + SPY_JAR);
            }
        }
    }

    public Thread getPrintThread() {
        return printThread;
    }

    public void setPrintThread(Thread printThread) {
        this.printThread = printThread;
    }

    public int getRun() {
        return run;
    }

    public void setRun(int run) {
        this.run = run;
    }
}
