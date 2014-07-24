package org.safehaus.subutai.plugin.hadoop.ui.manager;

import com.google.common.base.Strings;
import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.Window;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.ui.HadoopUI;
import org.safehaus.subutai.plugin.hadoop.ui.manager.components.*;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.CompleteEvent;
import org.safehaus.subutai.shared.protocol.enums.NodeState;

import java.util.List;
import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class HadoopTable extends TreeTable {
	private static final Action UNINSTALL_ITEM_ACTION = new Action("Uninstall cluster");
	private static final Action ADD_ITEM_ACTION = new Action("Add new node");
	private static final Action EXCLUDE_ITEM_ACTION = new Action("Exclude node");
	private static final Action INCLUDE_ITEM_ACTION = new Action("Include node");

	public static final String CLUSTER_NAME_PROPERTY = "Cluster Name";
	public static final String DOMAIN_NAME_PROPERTY = "Domain Name";
	public static final String NAMENODE_PROPERTY = "NameNode/DataNodes";
	public static final String SECONDARY_NAMENODE_PROPERTY = "Secondary NameNode";
	public static final String JOBTRACKER_PROPERTY = "JobTracker/TaskTrackers";
	public static final String REPLICATION_PROPERTY = "Replication Factor";

	private Embedded indicator;

	public HadoopTable(String caption, final Embedded indicator) {
		this.indicator = indicator;
		setCaption(caption);

		this.setSizeFull();

		this.setPageLength(10);
		this.setSelectable(true);
		this.setImmediate(true);

		addContainerProperty(CLUSTER_NAME_PROPERTY, String.class, null);
		addContainerProperty(DOMAIN_NAME_PROPERTY, String.class, null);
		addContainerProperty(NAMENODE_PROPERTY, ClusterNode.class, null);
		addContainerProperty(SECONDARY_NAMENODE_PROPERTY, ClusterNode.class, null);
		addContainerProperty(JOBTRACKER_PROPERTY, ClusterNode.class, null);
		addContainerProperty(REPLICATION_PROPERTY, Integer.class, null);

		addActionHandler(new Action.Handler() {

			public void handleAction(Action action, Object sender, Object target) {
				if (action == UNINSTALL_ITEM_ACTION) {
					Item row = getItem(target);

					indicator.setVisible(true);
					UUID trackID = HadoopUI.getHadoopManager().uninstallCluster((String) row.getItemProperty(CLUSTER_NAME_PROPERTY).getValue());
					HadoopUI.getExecutor().execute(new WaitTask(trackID, new CompleteEvent() {

						public void onComplete(NodeState state) {
							refreshDataSource();
						}
					}));
				} else if (action == ADD_ITEM_ACTION) {
					Item row = getItem(target);

					indicator.setVisible(true);

					UUID trackID = HadoopUI.getHadoopManager().addNode((String) row.getItemProperty(CLUSTER_NAME_PROPERTY).getValue());
					ProgressWindow window = new ProgressWindow(HadoopUI.getExecutor(), HadoopUI.getTracker(), trackID, HadoopClusterConfig.PRODUCT_KEY);
					window.getWindow().addCloseListener(new Window.CloseListener() {
						@Override
						public void windowClose(Window.CloseEvent closeEvent) {
							refreshDataSource();
						}
					});
					getUI().addWindow(window.getWindow());

				} else if (action == EXCLUDE_ITEM_ACTION) {
					Item row = getItem(target);

					SlaveNode dataNode = (SlaveNode) row.getItemProperty(NAMENODE_PROPERTY).getValue();
					SlaveNode taskTracker = (SlaveNode) row.getItemProperty(JOBTRACKER_PROPERTY).getValue();

					indicator.setVisible(true);

					HadoopUI.getHadoopManager().blockDataNode(dataNode.getCluster(), dataNode.getAgent());
					UUID trackID = HadoopUI.getHadoopManager().blockTaskTracker(taskTracker.getCluster(), taskTracker.getAgent());
					HadoopUI.getExecutor().execute(new WaitTask(trackID, new CompleteEvent() {

						public void onComplete(NodeState state) {
							refreshDataSource();
						}
					}));
				} else if (action == INCLUDE_ITEM_ACTION) {
					Item row = getItem(target);

					SlaveNode dataNode = (SlaveNode) row.getItemProperty(NAMENODE_PROPERTY).getValue();
					SlaveNode taskTracker = (SlaveNode) row.getItemProperty(JOBTRACKER_PROPERTY).getValue();

					indicator.setVisible(true);
					HadoopUI.getHadoopManager().unblockDataNode(dataNode.getCluster(), dataNode.getAgent());

					UUID trackID = HadoopUI.getHadoopManager().unblockTaskTracker(taskTracker.getCluster(), taskTracker.getAgent());
					HadoopUI.getExecutor().execute(new WaitTask(trackID, new CompleteEvent() {

						public void onComplete(NodeState state) {
							refreshDataSource();
						}
					}));
				}
			}

			public Action[] getActions(Object target, Object sender) {

				if (target != null) {
					Item row = getItem(target);

					if (areChildrenAllowed(target)) {
						if (!Strings.isNullOrEmpty((String) row.getItemProperty(DOMAIN_NAME_PROPERTY).getValue())) {
							return new Action[] {UNINSTALL_ITEM_ACTION, ADD_ITEM_ACTION};
						}
					}

					if (!areChildrenAllowed(target)) {
						if (row.getItemProperty(CLUSTER_NAME_PROPERTY).getValue() != null &&
								row.getItemProperty(CLUSTER_NAME_PROPERTY).getValue().toString().equalsIgnoreCase("Blocked")) {
							return new Action[] {INCLUDE_ITEM_ACTION};
						}
					}

					if (!areChildrenAllowed(target)) {
						if (row.getItemProperty(NAMENODE_PROPERTY).getValue() != null ||
								row.getItemProperty(JOBTRACKER_PROPERTY).getValue() != null) {
							return new Action[] {EXCLUDE_ITEM_ACTION};
						}
					}
				}

				return null;
			}
		});

		refreshDataSource();
	}

	public void refreshDataSource() {
		indicator.setVisible(true);
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

		List<HadoopClusterConfig> list = HadoopUI.getHadoopManager().getClusters();
		for (HadoopClusterConfig cluster : list) {
			NameNode nameNode = new NameNode(cluster);
			JobTracker jobTracker = new JobTracker(cluster);
			SecondaryNameNode secondaryNameNode = new SecondaryNameNode(cluster);
			nameNode.addSlaveNode(secondaryNameNode);

			Object rowId = addItem(new Object[] {
							cluster.getClusterName(),
							cluster.getDomainName(),
							nameNode,
							secondaryNameNode,
							jobTracker,
							cluster.getReplicationFactor()},
					null
			);

			for (Agent agent : cluster.getDataNodes()) {
				Object childID = null;

				SlaveNode dataNode = new SlaveNode(cluster, agent, true);
				SlaveNode taskTracker = new SlaveNode(cluster, agent, false);

				nameNode.addSlaveNode(dataNode);
				jobTracker.addSlaveNode(taskTracker);

				if (cluster.getBlockedAgents().contains(agent)) {
					childID = addItem(new Object[] {
									"Blocked",
									null,
									dataNode,
									null,
									taskTracker,
									null},
							null
					);
				} else {
					childID = addItem(new Object[] {
									null,
									null,
									dataNode,
									null,
									taskTracker,
									null},
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
		indicator.setVisible(false);
	}
}
