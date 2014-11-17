/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.api;


/**
 * Holds mongo node operation timeouts
 */
public class Timeouts
{

    public static final int CHECK_NODE_STATUS_TIMEOUT_SEC = 20;
    public static final int STOP_NODE_TIMEOUT_SEC = 60;
    public static final int START_ROUTER_TIMEOUT_SEC = 30;
    public static final int START_CONFIG_SERVER_TIMEOUT_SEC = 30;
    public static final int START_DATE_NODE_TIMEOUT_SEC = 30;
}
