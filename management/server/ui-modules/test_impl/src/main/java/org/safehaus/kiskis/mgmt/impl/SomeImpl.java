/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.safehaus.kiskis.mgmt.impl;

import org.safehaus.kiskis.mgmt.api.SomeApi;

/**
 *
 * @author bahadyr
 */
public class SomeImpl implements SomeApi {

    @Override
    public String sayHello(String name) {
        return "hello " + name;
    }
    
}
