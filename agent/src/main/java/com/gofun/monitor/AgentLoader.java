package com.gofun.monitor;

import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

public class AgentLoader {


    public static void main(String[] args) {
        String agentFilePath = "/Users/chenlinsong1/IdeaProjects/my/agent-demo/agent/target/agent-jar-with-dependencies.jar";
        String applicationName = "application-1.0-SNAPSHOT-jar-with-dependencies";

        //iterate all jvms and get the first one that matches our application name
        Optional<String> jvmProcessOpt = Optional.ofNullable(VirtualMachine.list()
                .stream()
                .filter(jvm -> {
                    System.out.println("jvm:"+ jvm.displayName());
                    return jvm.displayName().contains(applicationName);
                })
                .findFirst().get().id());

        if(!jvmProcessOpt.isPresent()) {
            System.out.println("Target Application not found");
            return;
        }
        File agentFile = new File(agentFilePath);
        try {
            String jvmPid = jvmProcessOpt.get();
            System.out.println("Attaching to target JVM with PID: " + jvmPid);
            VirtualMachine jvm = VirtualMachine.attach(jvmPid);
            String corePath = "/Users/chenlinsong1/IdeaProjects/my/agent-demo/agent/target/core-1.0-SNAPSHOT-jar-with-dependencies.jar;";
            jvm.loadAgent(agentFile.getAbsolutePath(),corePath);
            jvm.detach();
            System.out.println("Attached to target JVM and loaded Java agent successfully");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
