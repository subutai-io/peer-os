package org.safehaus.subutai.ui.hive.query.components;

import com.vaadin.ui.TreeTable;
import org.safehaus.subutai.api.hadoop.HadoopClusterConfig;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.ui.hive.query.HiveQueryUI;

import java.util.List;

/**
 * Created by daralbaev on 06.05.14.
 */
public class HadoopTreeTable extends TreeTable {

	public static final String NODE_NAME_PROPERTY = "Hadoop Clusters";

	public HadoopTreeTable() {
		this.setSizeFull();

		this.setPageLength(10);
		this.setSelectable(true);
		this.setImmediate(true);
		this.setMultiSelect(true);

		addContainerProperty(NODE_NAME_PROPERTY, AgentContainer.class, null);
		refreshDataSource();
	}

	private void refreshDataSource() {
		removeAllItems();

		final Object parentId = addItem(new Object[] {
						"All clusters",
						null,
						null,
						null,
						null,
						null},
				null
		);
		setCollapsed(parentId, false);

		List<HadoopClusterConfig> list = HiveQueryUI.getManager().getHadoopClusters();
		for (HadoopClusterConfig cluster : list) {

			Object rowId = addItem(new Object[] {
							new AgentContainer(cluster.getNameNode(),
									String.format("NameNode - %s", cluster.getNameNode().getHostname()))},
					null
			);

			int index = 1;
			for (Agent agent : cluster.getDataNodes()) {
				Object childID = null;

				if (!cluster.getBlockedAgents().contains(agent)) {
					childID = addItem(new Object[] {
									new AgentContainer(agent,
											String.format("DataNode %d - %s", index++, agent.getHostname()))},
							null
					);
				}

				setParent(childID, rowId);
				setCollapsed(childID, true);
				setChildrenAllowed(childID, false);
			}

			setParent(rowId, parentId);
			setCollapsed(rowId, false);
		}
	}
}
