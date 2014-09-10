package org.safehaus.subutai.plugin.common.mock;



import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandStatus;

import java.util.Map;
import java.util.UUID;


public class CommandMock implements Command {

	private String description;
	private boolean succeeded;

	@Override
	public boolean hasCompleted() {
		return false;
	}

	@Override
	public boolean hasSucceeded() {
		return succeeded;
	}

	@Override
	public CommandStatus getCommandStatus() {
		return null;
	}

	@Override
	public Map<UUID, AgentResult > getResults() {
		return null;
	}

	@Override
	public UUID getCommandUUID() {
		return null;
	}

	@Override
	public Object getData() {
		return null;
	}

	@Override
	public void setData(final Object data) {

	}

	@Override
	public String getAllErrors() {
		return null;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public CommandMock setDescription(final String description) {
		this.description = description;
		return this;
	}

	public CommandMock setSucceeded(boolean succeeded) {
		this.succeeded = succeeded;
		return this;
	}
}
