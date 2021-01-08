package com.gofun.agent.core.advisor;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class MonitorClassFileTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        System.out.println("className1："+className);
        if (className != null && "com/gofun/application/Application".endsWith(className)){
            System.out.println("className2："+className);
            ClassPool pool = ClassPool.getDefault();
            try {
                className = className.replace("/",".");
                CtClass cc = pool.get(className);
                CtMethod ctMethod = cc.getDeclaredMethod("printValue");
//                ctMethod.insertBefore("{System.out.println(\"application print spy key:\"+java.spy.SpyApi.getKeySet());}");
                ctMethod.insertBefore("{java.spy.SpyApi.put($1+\"\",$1+\"\");System.out.println(\"application print spy key:\"+java.spy.SpyApi.getKeySet());}");
                System.out.println("代码注入成功");
               return cc.toBytecode();
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (CannotCompileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return classfileBuffer;
    }
}
