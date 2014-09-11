package org.safehaus.subutai.impl.strategymanager;


import org.safehaus.subutai.api.strategymanager.Criteria;
import org.safehaus.subutai.api.strategymanager.ServerMetric;


abstract class MetricComparator {

    static MetricComparator create(Criteria criteria) {
        MetricComparator mc = null;
        if ("MORE_HDD".equals(criteria.getId()))
            mc = new MetricComparator() {
                @Override
                public int getValue(ServerMetric m) {
                    return m.getFreeHddMb();
                }
            };
        if ("MORE_RAM".equals(criteria.getId()))
            mc = new MetricComparator() {
                @Override
                int getValue(ServerMetric m) {
                    return m.getFreeRamMb();
                }
            };
        if ("MORE_CPU".equals(criteria.getId()))
            mc = new MetricComparator() {
                @Override
                int getValue(ServerMetric m) {
                    return m.getCpuLoadPercent();
                }


                @Override
                boolean isLessBetter() {
                    return true;
                }
            };
        return null;
    }

    abstract int getValue(ServerMetric m);


    boolean isLessBetter() {
        return false;
    }
}
