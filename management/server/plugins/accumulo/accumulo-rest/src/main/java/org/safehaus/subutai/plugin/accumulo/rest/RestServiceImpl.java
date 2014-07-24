package org.safehaus.subutai.plugin.accumulo.rest;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {
    //Just like Spring.  Please add Getters/Setters. Blueprint annotations are still work in progress
    //    private HelloService helloService;


    /*
        Constructor
     */
    public RestServiceImpl() {
    }


    public String handleGet( String name ) {
        //        return helloService.sayHello(name);
        return "Accumulo " + name;
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