/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.persistence;

import java.util.List;

/**
 *
 * @author dilshat
 */
public interface Agent {

    public Long getId();

    public String getUUID();

    public String getMacAddress();

    public String getHostName();

    public List<String> getIps();
}
