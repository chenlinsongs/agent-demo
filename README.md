使用方法
1.运行application-1.0-SNAPSHOT-jar-with-dependencies.jar包
2.运行AgentLoader类


如果已经attache过，后续不需要在attache，在代码层面通过一些标识判断是否已经attache过，在执行新命令时，如果不需要污染之前的代码，
我觉得可以使用代理方式，封装一个新类，执行完以后，用原始的类替换回来？