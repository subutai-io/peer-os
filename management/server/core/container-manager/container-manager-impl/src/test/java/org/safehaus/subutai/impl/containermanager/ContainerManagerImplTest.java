/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.containermanager;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.container.impl.ContainerManagerImpl;
//import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.monitor.api.Monitoring;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.safehaus.subutai.core.template.api.TemplateManager;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for ContainerManagerImpl class
 */
public class ContainerManagerImplTest
{

    private ContainerManager containerManager;
    private TemplateManager templateManager;
    private TemplateRegistry templateRegistry;
//    private DbManager dbManager;
    private StrategyManager strategyManager;


    @Before
    public void setUpMethod()
    {
        Monitoring monitor = mock( Monitoring.class );
        //        when( monitor.getData( any( String.class ), any( MetricType.class ), any( Date.class ),
        // any( Date.class ) ) )
        //                .thenReturn( Collections.EMPTY_MAP );
        templateManager = mock( TemplateManager.class );
        templateRegistry = mock( TemplateRegistry.class );
//        dbManager = mock( DbManager.class );
        strategyManager = mock( StrategyManager.class );
        containerManager = new ContainerManagerImpl( new AgentManagerFake(), MockUtils.getAutoCommandRunner(), monitor,
                templateManager, templateRegistry, strategyManager );
        ( ( ContainerManagerImpl ) containerManager ).init();
    }


    @After
    public void tearDownMethod()
    {

        ( ( ContainerManagerImpl ) containerManager ).destroy();
    }


    @Test
    public void testCloneSingleContainer() throws Exception
    {
        when( templateManager.clone( MockUtils.PHYSICAL_HOSTNAME, MockUtils.templateName, MockUtils.LXC_HOSTNAME,
                MockUtils.envUUID.toString() ) ).thenReturn( true );
        Agent agent = containerManager.clone( MockUtils.envUUID, MockUtils.PHYSICAL_HOSTNAME, MockUtils.templateName,
                MockUtils.LXC_HOSTNAME );
        assertNotNull( agent );
    }

      /*
    @Test
    public void testClones() throws Exception
    {
        containerManager.clone(  containerManager.clone( MockUtils.envUUID, MockUtils.PHYSICAL_HOSTNAME,
        MockUtils.templateName,
                        MockUtils.LXC_HOSTNAME );

    }


    @Test
	public void testGetLxcOnPhysicalServers() {

		Map<String, EnumMap<ContainerState, List<String>>> result = lxcManager.getLxcOnPhysicalServers();

		assertFalse(result.isEmpty());
	}


	@Test
	public void testGetPhysicalServerMetrics() {

		Map<Agent, ServerMetric> result = lxcManager.getPhysicalServerMetrics();

		assertFalse(result.isEmpty());
	}


	@Test
	public void testGetPhysicalServersWithLxcSlots() {

		Map<Agent, Integer> result = lxcManager.getPhysicalServersWithLxcSlots();

		assertTrue(result.entrySet().iterator().next().getValue() > 0);
	}


	@Test
	public void testStartLxcOnHost() {

		boolean result =
				lxcManager.startLxcOnHost(MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname());

		assertTrue(result);
	}


	@Test
	public void testStopLxcOnHost() {
		ContainerManager lxcManager = new ContainerManagerImpl(new AgentManagerFake(),
				MockUtils.getHardCodedCommandRunner(true, true, 0, "STOPPED", null), mock(Monitor.class));

		boolean result =
				lxcManager.stopLxcOnHost(MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname());

		assertTrue(result);
	}


	@Test
	public void testDestroyLxcOnHost() {

		boolean result =
				lxcManager.destroyLxcOnHost(MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname());

		assertTrue(result);
	}


	@Test
	public void testCloneNStartLxcOnHost() {

		boolean result =
				lxcManager.cloneNStartLxcOnHost(MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname());

		assertTrue(result);
	}


	@Test
	public void testDestroyLxcs() {

		boolean error = false;
		try {
			Set<String> lxcHostnames = new HashSet<>();
			lxcHostnames.add(MockUtils.getLxcAgent().getHostname());
			lxcManager.destroyLxcsByHostname(lxcHostnames);
		} catch (LxcDestroyException ex) {
			error = true;
		}
		assertFalse(error);commands
	}


	@Test
	public void testCreateLxcsByStrategy() throws LxcCreateException {

		Map<String, Map<Agent, Set<Agent>>> agentMap =
				lxcManager.createLxcsByStrategy(new DefaultContainerPlacementStrategy(1));

		assertFalse(agentMap.isEmpty());
	}


	@Test
	public void testCreateLxcs() throws LxcCreateException {

		Map<Agent, Set<Agent>> agentMap = lxcManager.createLxcs(1);

		assertFalse(agentMap.isEmpty());
	}

	*/
}
