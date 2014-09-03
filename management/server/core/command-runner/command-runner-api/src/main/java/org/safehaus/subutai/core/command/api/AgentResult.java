/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.api;


import java.util.UUID;


/**
 * Represents result of command execution on agent
 */
public interface AgentResult {

	/**
	 * Returns exit code or null if command has not completed
	 *
	 * @return - exit code or null if command has not completed
	 */
	public Integer getExitCode();

	/**
	 * Returns std out of command or empty string
	 *
	 * @return - std out of command or empty string
	 */
	public String getStdOut();

	/**
	 * Returns std err of command or empty string
	 *
	 * @return - std err of command or empty string
	 */
	public String getStdErr();

	/**
	 * Returns agent UUID
	 *
	 * @return - agent UUID
	 */
	public UUID getAgentUUID();
}
