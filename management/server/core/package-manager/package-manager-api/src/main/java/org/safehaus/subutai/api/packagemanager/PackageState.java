package org.safehaus.subutai.api.packagemanager;

/**
 * Debian package states. Refer to dpkg man pages for more info.
 *
 */
public enum PackageState {

    NOT_INSTALLED,
    CONFIG_FILES,
    HALF_INSTALLED,
    UNPACKED,
    HALF_CONFIGURED,
    TRIGGERS_AWATED,
    TRIGGERS_PENDING,
    INSTALLED;

}
