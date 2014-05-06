package org.safehaus.kiskis.mgmt.hbase.services;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {
    //Just like Spring.  Please add Getters/Setters. Blueprint annotations are still work in progress
//    private HelloService helloService;

    public String handleGet(String name) {
//        return helloService.sayHello(name);
        return "Hello " + name;
    }

    /*
        Constructor
     */
    public RestServiceImpl() {
    }

    /*
        Getters and Setters
     */
//    public HelloService getHelloService() {
//        return helloService;
//    }
//
//    public void setHelloService(HelloService helloService) {
//        this.helloService = helloService;
//    }
}