package org.safehaus.subutai.impl.strategy;

import org.safehaus.subutai.api.lxcmanager.ServerMetric;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;

abstract class MetricComparator {

    static MetricComparator create(PlacementStrategyENUM st) {
        MetricComparator mc = null;
        switch(st) {
            case MORE_HDD:
                mc = new MetricComparator() {
                    @Override
                    public int getValue(ServerMetric m) {
                        return m.getFreeHddMb();
                    }
                };
                break;
            case MORE_RAM:
                mc = new MetricComparator() {
                    @Override
                    int getValue(ServerMetric m) {
                        return m.getFreeRamMb();
                    }
                };
                break;
            default:
                throw new AssertionError(st.name());
        }
        return mc;
    }

    abstract int getValue(ServerMetric m);

    boolean isLessBetter() {
        return false;
    }
}
