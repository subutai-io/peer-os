package org.safehaus.kiskis.mgmt.rest.services;

/**
 * Created by bahadyr on 5/6/14.
 */

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello, "+name;
    }
}