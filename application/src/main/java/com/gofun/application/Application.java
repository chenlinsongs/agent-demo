package com.gofun.application;

public class Application {

    private String name = "cls";

    public static void main(String[] args) throws InterruptedException {
        Application application = new Application();
        int i = 0;
        while (true){
            application.printValue(i++);
            Thread.sleep(1000);
        }
    }

    public void printValue(int i){
        System.out.println("applicaiton print : "+i);
    }

    public void printVar(){
        System.out.println(name+""+System.currentTimeMillis());
    }
}
