package org.safehaus.subutai.impl.strategymanager;


import org.safehaus.subutai.api.strategymanager.Criteria;
import org.safehaus.subutai.api.strategymanager.ServerMetric;


abstract class MetricComparator {

	static MetricComparator create(Criteria criteria) {
		MetricComparator mc = null;
		// TODO: add missing case clauses
		switch (criteria) {
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
			case MORE_CPU:
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
				break;
			default:
				throw new AssertionError(criteria.name());
		}
		return mc;
	}


	abstract int getValue(ServerMetric m);


	boolean isLessBetter() {
		return false;
	}
}
