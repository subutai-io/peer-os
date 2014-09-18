/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.elasticsearch.ui.manager;

import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.ui.ElasticsearchUI;

import java.util.UUID;


public class CheckTask implements Runnable {

	private final String clusterName, lxcHostname;
	private final CompleteEvent completeEvent;
    private final Elasticsearch elasticsearch;
    private final Tracker tracker;

    public CheckTask( Elasticsearch elasticsearch, Tracker tracker, String clusterName, String lxcHostname, CompleteEvent completeEvent) {
        this.elasticsearch = elasticsearch;
        this.tracker = tracker;
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        this.completeEvent = completeEvent;
    }

	public void run() {

    	UUID trackID = elasticsearch.checkNode( clusterName, lxcHostname );

		long start = System.currentTimeMillis();
		while (!Thread.interrupted()) {
			ProductOperationView po = tracker.getProductOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY, trackID );
			if (po != null) {
				if (po.getState() != ProductOperationState.RUNNING) {
                    completeEvent.onComplete( po.getLog() );
					break;
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				break;
			}
			if (System.currentTimeMillis() - start > (30 + 3) * 1000) {
				break;
			}
		}
	}
}
