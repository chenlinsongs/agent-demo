package com.gofun.agent.core.advisor;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitorClassFileTransformer implements ClassFileTransformer {

    /** The internal form class name of the class to transform */
    private String targetClassName;
    /** The class loader of the class we want to transform */
    private ClassLoader targetClassLoader;

    Map<String, byte[]> historyBytes = new ConcurrentHashMap<>();

    boolean isRestore;

    public MonitorClassFileTransformer(String targetClassName, ClassLoader targetClassLoader,boolean isRestore) {
        this.targetClassName = targetClassName;
        this.targetClassLoader = targetClassLoader;
        this.isRestore = isRestore;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        byte[] byteCode = classfileBuffer;
        String finalTargetClassName = this.targetClassName.replaceAll("\\.", "/"); //replace . with /
        System.out.println("className1："+finalTargetClassName);
        if (!className.equals(finalTargetClassName)) {
            return byteCode;
        }
        if (className.equals(finalTargetClassName) && loader.equals(targetClassLoader)){
            if (!isRestore){
                System.out.println("isRestore is false");
                //记录历史
                historyBytes.put(targetClassName,classfileBuffer);
                System.out.println("className2："+finalTargetClassName);
                ClassPool pool = ClassPool.getDefault();
                try {
//                className = className.replace("/",".");
                    CtClass cc = pool.get(targetClassName);
                    CtMethod ctMethod = cc.getDeclaredMethod("printValue");
//                ctMethod.insertBefore("{java.spy.SpyApi.put($1+\"\",$1+\"\");System.out.println(\"application print spy key:\"+java.spy.SpyApi.getKeySet());}");
                    ctMethod.insertBefore("{java.spy.SpyApi.put($1+\"\",$1+\"\");" +
                            "System.out.println(\"application print spy size:\"+java.spy.SpyApi.getSize());" +
                            "java.spy.SpyApi.put2(\"object\",$0);}");
                    System.out.println("代码注入成功");

                    byteCode = cc.toBytecode();
                    cc.detach();

                } catch (NotFoundException e) {
                    e.printStackTrace();
                } catch (CannotCompileException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                System.out.println("isRestore is ture");
                byteCode = historyBytes.get(targetClassName);
                if (byteCode != null){
                    return byteCode;
                }else {
                    byteCode = classfileBuffer;
                }
            }
        }

        return byteCode;
    }

    public boolean isRestore() {
        return isRestore;
    }

    public void setRestore(boolean restore) {
        isRestore = restore;
    }
}
