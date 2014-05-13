/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.hive;

import java.util.Arrays;
import java.util.UUID;
import org.junit.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author azilet
 */
public class CommandsTest {

    private String hiveServerIp = "127.0.0.1";
    private Agent hiveServer;

    public CommandsTest() {
    }

    @Before
    public void before() throws Exception {
        hiveServer = new Agent(UUID.randomUUID(), "hostname", "parenthost",
                "MAC-addr", Arrays.asList(hiveServerIp), true, "transportId");
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testMake() {
        System.out.println("make");
        for(CommandType t : CommandType.values()) {
            for(Product p : Product.values()) {
                Assert.assertNotNull(Commands.make(t, p));
            }
        }
    }

    @Test
    public void testConfigureHiveServer() {
        System.out.println("configureHiveServer");
        String s = Commands.configureHiveServer(hiveServerIp);
        Assert.assertNotNull(s);
        Assert.assertTrue(s.contains(hiveServerIp));
    }

    @Test
    public void testConfigureClient() {
        System.out.println("configureClient");
        String s = Commands.configureClient(hiveServer);
        Assert.assertNotNull(s);
    }

    @Test
    public void testAddHivePoperty() {
        System.out.println("addHivePoperty");
        String s = Commands.addHivePoperty("add", "file.xml", "property", null);
        Assert.assertNotNull(s);

        s = Commands.addHivePoperty("add", "file.xml", "property", "value");
        Assert.assertNotNull(s);
    }

}
