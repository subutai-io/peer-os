/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.containermanager;


import org.safehaus.subutai.core.container.api.ContainerManager;


/**
 * Test for ContainerManagerImpl class
 */
public class ContainerManagerImplUT {

	private ContainerManager lxcManager;

/*
	@Before
	public void setUpMethod() {
		Monitor monitor = mock(Monitor.class);
		when(monitor.getData(any(String.class), any(Metric.class), any(Date.class), any(Date.class)))
				.thenReturn(Collections.EMPTY_MAP);
		lxcManager = new ContainerManagerImpl(new AgentManagerFake(), MockUtils.getAutoCommandRunner(), monitor);
		((ContainerManagerImpl) lxcManager).init();
	}


	@After
	public void tearDownMethod() {

		((ContainerManagerImpl) lxcManager).destroy();
	}


	@Test
	public void testCloneLxcOnHost() {

		boolean result =
				lxcManager.cloneLxcOnHost(MockUtils.getPhysicalAgent(), MockUtils.getLxcAgent().getHostname());

		assertTrue(result);
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
		assertFalse(error);
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
