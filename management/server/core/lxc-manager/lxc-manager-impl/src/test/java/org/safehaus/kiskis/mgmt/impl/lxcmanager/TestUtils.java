/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;

import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class TestUtils {

    private static final UUID physicalUUID = UUID.randomUUID();
    private static final UUID lxcUUID = UUID.randomUUID();

    public static Agent getPhysicalAgent() {
        Agent agent = new Agent(physicalUUID, "py111");
        agent.setIsLXC(false);
        return agent;
    }

    public static Agent getLxcAgent() {
        Agent agent = new Agent(lxcUUID, "py111-lxc-222");
        agent.setIsLXC(true);
        agent.setParentHostName("py111");
        return agent;
    }

    public static String getLxcListOutput() {
        return "RUNNING\n"
                + "  py111-lxc-222\n"
                + "  py111-lxc-888ba0c7-c559-11e3-acc9-af19ef907de1\n"
                + "  py111-lxc-888bc7d8-c559-11e3-acc9-af19ef907de1\n"
                + "  py111-lxc-888bc7da-c559-11e3-acc9-af19ef907de1\n"
                + "  py111-lxc-99780f42-c573-11e3-8493-59facd645e07\n"
                + "  py111-lxc-99783654-c573-11e3-8493-59facd645e07\n"
                + "  py111-lxc-99783656-c573-11e3-8493-59facd645e07\n"
                + "  py111-lxc-99783658-c573-11e3-8493-59facd645e07\n"
                + "  py111-lxc-acda8860-c55d-11e3-acc9-af19ef907de1\n"
                + "  py111-lxc-acda8861-c55d-11e3-acc9-af19ef907de1\n"
                + "  py111-lxc-acda8863-c55d-11e3-acc9-af19ef907de1\n"
                + "  py111-lxc-acda8865-c55d-11e3-acc9-af19ef907de1\n"
                + "  py111-lxc-acdaaf77-c55d-11e3-acc9-af19ef907de1\n"
                + "\n"
                + "FROZEN\n"
                + "\n"
                + "STOPPED\n"
                + "  base-container\n"
                + "  py111-lxc-test1\n"
                + "  py111-lxc-test2";
    }

    public static String getMetricsOutput() {
        return "-/+ buffers/cache:       8561       7362\n"
                + "/dev/sda1      950982348 15033292 887635220   2% /\n"
                + " 15:05:11 up 7 days, 21:28,  1 user,  load average: 0.01, 0.09, 0.27\n"
                + "8";
    }
}
