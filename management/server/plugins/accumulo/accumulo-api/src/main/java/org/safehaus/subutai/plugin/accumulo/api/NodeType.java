package org.safehaus.subutai.plugin.accumulo.api;


/**
 * Created by dilshat on 4/29/14.
 */
public enum NodeType {
	MASTER, GC, MONITOR, TRACER, LOGGER, TABLET_SERVER;


	public boolean isSlave() {
		return this == LOGGER || this == TABLET_SERVER;
	}
}
