/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management;

/**
 *
 * @author bahadyr
 */
public enum OozieCommandEnum {

    START_SERVER("service oozie-server start", 120),
    STOP_SERVER("service oozie-server stop", 120),
    STATUS("service oozie status", 120),
    INSTALL_SERVER("apt-get --assume-yes --force-yes install ksks-oozie-server", 120),
    INSTALL_CLIENT("apt-get --assume-yes --force-yes install ksks-oozie-client", 120),
    CONFIGURE(". /etc/profile && oozie-configure.sh configure", 120),
    PURGE_SERVER("apt-get --assume-yes --force-yes purge oozie-server", 120),
    PURGE_CLIENT("apt-get --assume-yes --force-yes purge oozie-client", 120);

    String program;
    int timeout;

    private OozieCommandEnum(String program, int timeout) {
        this.program = program;
        this.timeout = timeout;
    }

    public String getProgram() {
        return program;
    }

}
