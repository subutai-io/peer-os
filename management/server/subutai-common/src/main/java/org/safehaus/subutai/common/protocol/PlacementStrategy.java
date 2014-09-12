package org.safehaus.subutai.common.protocol;

public enum PlacementStrategy {

    MORE_RAM("More RAM"), MORE_HDD("More HDD"), MORE_CPU("More CPU"), BEST_SERVER("Best server strategy"), ROUND_ROBIN("Round robin strategy"), FILLUP_PROCEED("Fillip proceed strategy");
    String value;
    PlacementStrategy(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }

}
