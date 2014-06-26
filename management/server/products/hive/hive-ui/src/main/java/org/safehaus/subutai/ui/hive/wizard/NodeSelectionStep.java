package org.safehaus.subutai.ui.hive.wizard;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.ui.hive.HiveUI;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NodeSelectionStep extends Panel {

	private final ComboBox hadoopClusters;
	private final ComboBox serverNode;
	private final TwinColSelect select;

	public NodeSelectionStep(final Wizard wizard) {

		setSizeFull();

		GridLayout content = new GridLayout(1, 2);
		content.setSizeFull();
		content.setSpacing(true);
		content.setMargin(true);

		hadoopClusters = new ComboBox("Hadoop cluster");
		serverNode = makeServerNodeComboBox(wizard);
		select = makeClientNodeSelector(wizard);

		hadoopClusters.setImmediate(true);
		hadoopClusters.setTextInputAllowed(false);
		hadoopClusters.setRequired(true);
		hadoopClusters.setNullSelectionAllowed(false);
		hadoopClusters.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				serverNode.removeAllItems();
				select.setValue(null);
				if (event.getProperty().getValue() != null) {
					Config hadoopInfo = (Config) event.getProperty().getValue();
					for (Agent a : hadoopInfo.getAllNodes()) {
						serverNode.addItem(a);
						serverNode.setItemCaption(a, a.getHostname());
					}
					select.setContainerDataSource(new BeanItemContainer<>(
									Agent.class, hadoopInfo.getAllNodes())
					);
					// do select if values exist
					if (wizard.getConfig().getServer() != null)
						serverNode.setValue(wizard.getConfig().getServer());
					if (!Util.isCollectionEmpty(wizard.getConfig().getClients()))
						select.setValue(wizard.getConfig().getClients());

					wizard.getConfig().setClusterName(hadoopInfo.getClusterName());
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
				if (Util.isStringEmpty(wizard.getConfig().getClusterName())) {
					show("Select Hadoop cluster");
				} else if (wizard.getConfig().getServer() == null) {
					show("Select server node");
				} else if (Util.isCollectionEmpty(wizard.getConfig().getClients())) {
					show("Select client nodes");
				} else {
					wizard.next();
				}
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

		content.addComponent(hadoopClusters);
		content.addComponent(serverNode);
		content.addComponent(select);
		content.addComponent(buttons);

		setContent(layout);

	}

	private void show(String notification) {
		Notification.show(notification);
	}

	private TwinColSelect makeClientNodeSelector(final Wizard wizard) {
		TwinColSelect tcs = new TwinColSelect("Client nodes");
		tcs.setItemCaptionPropertyId("hostname");
		tcs.setRows(7);
		tcs.setMultiSelect(true);
		tcs.setImmediate(true);
		tcs.setLeftColumnCaption("Available Nodes");
		tcs.setRightColumnCaption("Selected Nodes");
		tcs.setWidth(100, Unit.PERCENTAGE);
		tcs.setRequired(true);
		if (!Util.isCollectionEmpty(wizard.getConfig().getClients())) {
			tcs.setValue(wizard.getConfig().getClients());
		}
		tcs.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (event.getProperty().getValue() != null) {
					Set<Agent> clients = new HashSet();
					clients.addAll((Collection) event.getProperty().getValue());
					wizard.getConfig().setClients(clients);
				}
			}
		});
		return tcs;
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
				Agent serverNode = (Agent) event.getProperty().getValue();
				wizard.getConfig().setServer(serverNode);

				Config hci = (Config) hadoopClusters.getValue();
				select.removeAllItems();
				for (Agent a : hci.getAllNodes()) {
					if (a.equals(serverNode)) continue;
					select.addItem(a);
					select.setItemCaption(a, a.getHostname());
				}
			}
		});
		if (wizard.getConfig().getServer() != null)
			cb.setValue(wizard.getConfig().getServer());
		return cb;
	}

}
