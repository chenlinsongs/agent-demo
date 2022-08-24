package com.gofun.application;

public class Application {

    private String name = "cls";
    private int num = 0;

    public static void main(String[] args) throws InterruptedException {
        Application application = new Application();
        int i = 0;
        while (true){
            application.printValue(application.num);
            application.num++;
            Thread.sleep(1000);
        }
    }

    public void printValue(int i){
        System.out.println("applicaiton print num: "+num);
    }

    public void printVar(){
        System.out.println(name+""+System.currentTimeMillis());
    }
}
