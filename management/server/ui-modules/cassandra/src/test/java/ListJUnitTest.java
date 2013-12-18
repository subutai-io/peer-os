/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ListSelect;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author bahadyr
 */
public class ListJUnitTest {

    public ListJUnitTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void hello() {
        List<Agent> list = new ArrayList();
        Agent agent = new Agent();
        agent.setHostname("hostname");
        agent.setIsLXC(false);
        agent.setLastHeartbeat(new Date());
        agent.setListIP(null);
        agent.setMacAddress("mac");
        agent.setParentHostName("parenthost");
        list.add(agent);
        BeanItemContainer<Agent> agents = new BeanItemContainer<Agent>(Agent.class, list);

        final ListSelect hostSelect = new ListSelect("Enter a list of hosts using Fully Qualified Domain Name or IP", agents);
        hostSelect.setItemCaptionPropertyId("hostname");
        hostSelect.setRows(6); // perfect length in out case
        hostSelect.setNullSelectionAllowed(true); // user can not 'unselect'
        hostSelect.setMultiSelect(true);
        System.out.println("=======================================");
        for (Iterator i = hostSelect.getItemIds().iterator(); i.hasNext();) {
            Agent a = (Agent) i.next();
            System.out.println("HOSTNAME " + a.getHostname());
        }

    }
}
