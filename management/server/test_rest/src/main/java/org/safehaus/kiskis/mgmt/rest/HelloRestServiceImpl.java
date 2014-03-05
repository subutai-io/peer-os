/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.rest;

import org.safehaus.kiskis.mgmt.api.SomeApi;

/**
 *
 * @author bahadyr
 */
public class HelloRestServiceImpl implements HelloRestService {

    private SomeApi someApi;

    public SomeApi getSomeApi() {
        return someApi;
    }

    @Override
    public String handleGet(String name) {
        return someApi.sayHello(name);

    }

    /*
     Constructor
     */
    public HelloRestServiceImpl() {
    }

}
