/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.services;

import java.util.Queue;

/**
 *
 * @author dilshat
 */
public interface ModuleNotifier {

    public Queue<Module> getModules();

    public Queue<ModuleServiceListener> getListeners();

    public void addListener(ModuleServiceListener listener);

    public void removeListener(ModuleServiceListener listener);
}
