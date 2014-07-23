package org.safehaus.subutai.ui.hive.wizard;

import com.vaadin.data.Property;
import com.vaadin.ui.*;
import java.util.*;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.ui.hive.HiveUI;

public class NodeSelectionStep extends Panel {

	private final ComboBox hadoopClusters;
	private final ComboBox serverNode;

	public NodeSelectionStep(final Wizard wizard) {

		setSizeFull();

		GridLayout content = new GridLayout(1, 2);
		content.setSizeFull();
		content.setSpacing(true);
		content.setMargin(true);

        TextField nameTxt = new TextField("Cluster name");
        nameTxt.setRequired(true);
        nameTxt.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent e) {
                wizard.getConfig().setClusterName(e.getProperty().getValue().toString().trim());
            }
        });

		hadoopClusters = new ComboBox("Hadoop cluster");
		serverNode = makeServerNodeComboBox(wizard);

		hadoopClusters.setImmediate(true);
		hadoopClusters.setTextInputAllowed(false);
		hadoopClusters.setRequired(true);
		hadoopClusters.setNullSelectionAllowed(false);
		hadoopClusters.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				serverNode.removeAllItems();
				if (event.getProperty().getValue() != null) {
                    Config hadoopInfo = (Config)event.getProperty().getValue();
                    List<Agent> slaves = hadoopInfo.getAllSlaveNodes();
					for (Agent a : hadoopInfo.getAllNodes()) {
                        serverNode.addItem(a);
                        String caption = a.getHostname();
                        if(hadoopInfo.getJobTracker().equals(a))
                            caption += " [Job tracker]";
                        else if(hadoopInfo.getNameNode().equals(a))
                            caption += " [Name node]";
                        else if(hadoopInfo.getSecondaryNameNode().equals(a))
                            caption += " [Name node 2]";
                        else if(slaves.contains(a))
                            caption += " [Slave node]";
                        serverNode.setItemCaption(a, caption);
                    }
                    filterNodes();
                    // do select if already done; o/w select name node
                    if(wizard.getConfig().getServer() != null)
                        serverNode.setValue(wizard.getConfig().getServer());
                    else
                        serverNode.setValue(hadoopInfo.getNameNode());

                    wizard.getConfig().setHadoopClusterName(hadoopInfo.getClusterName());
				}
			}
		});

		List<Config> clusters = HiveUI.getHadoopManager().getClusters();
		if (clusters.size() > 0) {
			for (Config hci : clusters) {
				hadoopClusters.addItem(hci);
				hadoopClusters.setItemCaption(hci, hci.getClusterName());
			}
		}

		Config info = HiveUI.getHadoopManager().getCluster(wizard.getConfig().getClusterName());
		if (info != null) hadoopClusters.setValue(info);

		Button next = new Button("Next");
		next.addStyleName("default");
		next.addClickListener(new Button.ClickListener() {
			@Override
            public void buttonClick(Button.ClickEvent clickEvent) {
				if (Util.isStringEmpty(wizard.getConfig().getClusterName()))
                    show("Enter name for Hive installation");
                else if(Util.isStringEmpty(wizard.getConfig().getHadoopClusterName()))
                    show("Select Hadoop cluster");
                else if(wizard.getConfig().getServer() == null)
                    show("Select server node");
                else if(Util.isCollectionEmpty(wizard.getConfig().getClients()))
                    show("Select client nodes");
                else
                    wizard.next();
			}
		});

		Button back = new Button("Back");
		back.addStyleName("default");
		back.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				wizard.back();
			}
		});

		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.addComponent(new Label("Please, specify installation settings"));
		layout.addComponent(content);

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.addComponent(back);
		buttons.addComponent(next);

        content.addComponent(nameTxt);
		content.addComponent(hadoopClusters);
		content.addComponent(serverNode);
		content.addComponent(buttons);

		setContent(layout);

	}

	private void show(String notification) {
		Notification.show(notification);
	}

	private ComboBox makeServerNodeComboBox(final Wizard wizard) {
		ComboBox cb = new ComboBox("Server node");
		cb.setImmediate(true);
		cb.setTextInputAllowed(false);
		cb.setRequired(true);
		cb.setNullSelectionAllowed(false);
		cb.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
                Agent hiveMaster = (Agent)event.getProperty().getValue();
                wizard.getConfig().setServer(hiveMaster);

				Config hci = (Config) hadoopClusters.getValue();
                wizard.getConfig().getClients().addAll(hci.getAllNodes());
                wizard.getConfig().getClients().remove(hiveMaster);
            }
		});
		if (wizard.getConfig().getServer() != null)
			cb.setValue(wizard.getConfig().getServer());
		return cb;
	}

    private void filterNodes() {
        Collection<Agent> items = (Collection<Agent>)serverNode.getItemIds();
        final Set<Agent> set = new HashSet<>(items);
        new Thread(new Runnable() {

            @Override
            public void run() {
                Map<Agent, Boolean> map = HiveUI.getManager().isInstalled(set);
                for(Map.Entry<Agent, Boolean> e : map.entrySet()) {
                    if(e.getValue()) serverNode.removeItem(e.getKey());
                }
            }
        }).start();
    }
}
