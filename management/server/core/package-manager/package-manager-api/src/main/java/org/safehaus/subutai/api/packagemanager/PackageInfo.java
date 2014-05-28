package org.safehaus.subutai.api.packagemanager;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class PackageInfo {

    public static final String SOURCE_NAME = "DebPackageManager";
    private final String name;
    private final String version;
    private PackageState state;
    private SelectionState selectionState;
    private Set<PackageFlag> flags;
    private String arch;
    private String description;

    public PackageInfo(String name, String version) {
        this.name = name;
        this.version = version;
        this.flags = EnumSet.noneOf(PackageFlag.class);
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public PackageState getState() {
        return state;
    }

    public void setState(PackageState state) {
        this.state = state;
    }

    public SelectionState getSelectionState() {
        return selectionState;
    }

    public void setSelectionState(SelectionState selectionState) {
        this.selectionState = selectionState;
    }

    public Set<PackageFlag> getFlags() {
        return flags;
    }

    public void setFlags(Set<PackageFlag> flags) {
        this.flags = flags;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PackageInfo) {
            PackageInfo other = (PackageInfo)obj;
            return name.equals(other.name) && version.equals(other.version);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.version);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(selectionState.getAbbrev());
        sb.append(state.getAbbrev());
        for(PackageFlag f : flags) sb.append(f);
        sb.append("\t").append(name).append("\t").append(version);
        sb.append("\t").append(description);
        return sb.toString();
    }

}
