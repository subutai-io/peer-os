/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.solr;

import java.util.UUID;

/**
 *
 * @author dilshat
 */
public interface Solr {

    public UUID installNode(String lxcHostName);

    public UUID uninstallNode(String lxcHostName);

    public UUID startNode(String lxcHostName);

    public UUID stopNode(String lxcHostName);

    public UUID checkNode(String lxcHostName);
}
