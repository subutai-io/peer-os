package org.safehaus.subutai.api.gitmanager;


/**
 * Represents an exception which can be thrown by methods of GitManager implementation
 */
public class GitException extends Exception {
	public GitException(final String message) {
		super(message);
	}
}
