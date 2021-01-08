package com.gofun.agent.core;

import com.gofun.agent.core.advisor.MonitorClassFileTransformer;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.security.CodeSource;
import java.spy.SpyApi;
import java.util.jar.JarFile;

public class PrintValue {

    private static final String SPY_JAR = "spy-1.0-SNAPSHOT.jar";

    Thread printThread;

    int run = 1;

   private static PrintValue printValue;

    private Instrumentation instrumentation;

    private PrintValue(Instrumentation instrumentation) throws Throwable {
        this.instrumentation = instrumentation;
        initSpy();
        instrumentation.addTransformer(new MonitorClassFileTransformer(), true);
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
        printThread = new Thread(new Runnable() {
            public void run() {
                while (run == 1){
                    try {
                        System.out.println("core print key is: "+ SpyApi.getKeySet());
//                        System.out.println("core print value is: "+SpyApi.getKeySet());

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        printThread.start();
    }

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
