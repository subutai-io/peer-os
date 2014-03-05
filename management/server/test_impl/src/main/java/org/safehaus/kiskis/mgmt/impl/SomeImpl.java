/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl;

import java.util.List;
import org.safehaus.kiskis.mgmt.api.SomeApi;

/**
 *
 * @author bahadyr
 */
public class SomeImpl implements SomeApi {

    @Override
    public String sayHello(String name) {
        return name;
    }

    @Override
    public boolean install(String program) {

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean start(String serviceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean stop(String serviceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean status(String serviceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean purge(String serviceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean runCommand(String program) {
        return true;
    }

    SomeDAO someDAO = new SomeDAO();

    @Override
    public void writeLog(String log) {
        someDAO.writeLog(log);
    }

    @Override
    public List<String> getLogs() {
        return someDAO.getLogs();
    }

}
