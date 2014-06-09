package org.safehaus.subutai.impl.packagemanager.info;

/**
 * Debian package flag. Refer to dpkg man pages for more info.
 *
 */
public enum PackageFlag {

    REINST_REQUIRED('R');

    private final char abbrev;

    private PackageFlag(char abbrev) {
        this.abbrev = abbrev;
    }

    public char getAbbrev() {
        return abbrev;
    }

    public static PackageFlag getByAbbrev(char abbrev) {
        for(PackageFlag f : values()) {
            if(f.abbrev == abbrev) return f;
        }
        return null;
    }

}
