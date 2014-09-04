package org.safehaus.subutai.impl.containermanager;


import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;

import java.util.concurrent.Callable;


/**
 * Handles parallel containermanager creation/destruction
 */
public class ContainerActor implements Callable<ContainerInfo> {
	private final ContainerInfo containerInfo;
	private final ContainerManager containerManager;
	private final ContainerAction containerAction;
	private String templateName;


	public ContainerActor(final ContainerInfo containerInfo, final ContainerManager containerManager,
	                      final ContainerAction containerAction) {
		this.containerInfo = containerInfo;
		this.containerManager = containerManager;
		this.containerAction = containerAction;
	}


	public ContainerActor(final ContainerInfo containerInfo, final ContainerManager containerManager,
	                      final ContainerAction containerAction, final String templateName) {
		this.containerInfo = containerInfo;
		this.containerManager = containerManager;
		this.containerAction = containerAction;
		this.templateName = templateName;
	}


	@Override
	public ContainerInfo call() {
		if (containerAction == ContainerAction.CREATE) {

			try {
				containerManager.clonesCreate(containerInfo.getPhysicalAgent().getHostname(), templateName,
						containerInfo.getLxcHostnames());
				containerInfo.setResult(true);
			} catch (LxcCreateException ignore) {
			}
		} else {
			try {
				containerManager.clonesDestroy(containerInfo.getPhysicalAgent().getHostname(),
						containerInfo.getLxcHostnames());
				containerInfo.setResult(true);
			} catch (LxcDestroyException ignore) {

			}
		}
		return containerInfo;
	}
}
