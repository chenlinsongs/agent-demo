package com.gofun.agent.core.advisor;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class MonitorClassFileTransformer implements ClassFileTransformer {

    /** The internal form class name of the class to transform */
    private String targetClassName;
    /** The class loader of the class we want to transform */
    private ClassLoader targetClassLoader;

    public MonitorClassFileTransformer(String targetClassName, ClassLoader targetClassLoader) {
        this.targetClassName = targetClassName;
        this.targetClassLoader = targetClassLoader;
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
        }

        return byteCode;
    }


}
