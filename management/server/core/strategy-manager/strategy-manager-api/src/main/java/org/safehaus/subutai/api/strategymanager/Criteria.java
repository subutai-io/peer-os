package org.safehaus.subutai.api.strategymanager;

/**
 * Created by timur on 9/10/14.
 */
public enum Criteria {
    MORE_HDD("More HDD"), MORE_RAM("More RAM"), MORE_CPU("More CPU");
    String value;
    Criteria(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }
}
