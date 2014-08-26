package org.safehaus.subutai.api.gitmanager;


/**
 * Git file change statuses
 */
public enum GitFileStatus {
	MODIFIED,
	COPIED,
	RENAMED,
	ADDED,
	DELETED,
	UNMERGED,
	UNVERSIONED,
	UNMODIFIED
}
