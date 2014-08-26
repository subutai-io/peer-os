package org.safehaus.subutai.impl.packagemanager.info;

import org.safehaus.subutai.api.packagemanager.PackageInfo;

import java.util.EnumSet;
import java.util.Set;

public class DebPackageInfo extends PackageInfo {

	private PackageState state;
	private SelectionState selectionState;
	private Set<PackageFlag> flags;

	public DebPackageInfo(String name, String version) {
		super(name, version);
		this.flags = EnumSet.noneOf(PackageFlag.class);
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

	@Override
	public String toString() {
		String sep = "\t";
		StringBuilder sb = new StringBuilder();
		sb.append(selectionState.getAbbrev());
		sb.append(state.getAbbrev());
		for (PackageFlag f : flags) sb.append(f);
		sb.append(sep).append(super.toString());
		return sb.toString();
	}

}
