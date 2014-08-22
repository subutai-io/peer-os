package org.safehaus.subutai.impl.strategy;


import org.safehaus.subutai.api.lxcmanager.ServerMetric;
import org.safehaus.subutai.shared.protocol.PlacementStrategy;


abstract class MetricComparator {

    static MetricComparator create( PlacementStrategy st ) {
        MetricComparator mc = null;
        // TODO: add missing case clauses
        switch ( st ) {
            case MORE_HDD:
                mc = new MetricComparator() {
                    @Override
                    public int getValue( ServerMetric m ) {
                        return m.getFreeHddMb();
                    }
                };
                break;
            case MORE_RAM:
                mc = new MetricComparator() {
                    @Override
                    int getValue( ServerMetric m ) {
                        return m.getFreeRamMb();
                    }
                };
                break;
            case MORE_CPU:
                mc = new MetricComparator() {
                    @Override
                    int getValue( ServerMetric m ) {
                        return m.getCpuLoadPercent();
                    }


                    @Override
                    boolean isLessBetter() {
                        return true;
                    }
                };
                break;
            default:
                throw new AssertionError( st.name() );
        }
        return mc;
    }


    abstract int getValue( ServerMetric m );


    boolean isLessBetter() {
        return false;
    }
}
