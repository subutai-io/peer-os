package org.safehaus.subutai.api.packagemanager;

/**
 * Debian package selection states. Refer to dpkg man pages for more info.
 *
 */
public enum SelectionState {

    INSTALL,
    HOLD,
    DEINSTALL,
    PURGE;

}
