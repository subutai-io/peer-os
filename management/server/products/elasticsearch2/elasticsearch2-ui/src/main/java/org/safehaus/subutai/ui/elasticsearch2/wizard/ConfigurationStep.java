package org.safehaus.subutai.ui.elasticsearch2.wizard;

import com.vaadin.data.Property;
import com.vaadin.ui.*;
import org.safehaus.subutai.shared.protocol.Util;

import java.util.Arrays;

public class ConfigurationStep extends VerticalLayout {

	public ConfigurationStep(final Wizard wizard) {

		setSizeFull();

		GridLayout content = new GridLayout(1, 3);
		content.setSizeFull();
		content.setSpacing(true);
		content.setMargin(true);

		final TextField clusterNameTxtFld = new TextField("Enter cluster name:");
		clusterNameTxtFld.setInputPrompt("Cluster name");
		clusterNameTxtFld.setMaxLength(20);
		clusterNameTxtFld.setValue(wizard.getConfig().getClusterName());
		clusterNameTxtFld.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				wizard.getConfig().setClusterName(event.getProperty().getValue().toString().trim());
			}
		});


        ComboBox nodesCountCombo = new ComboBox("Number of nodes:", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        nodesCountCombo.setImmediate(true);
        nodesCountCombo.setTextInputAllowed(false);
        nodesCountCombo.setNullSelectionAllowed(false);
        nodesCountCombo.setValue(wizard.getConfig());

        nodesCountCombo.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                wizard.getConfig().setNumberOfNodes((Integer) event.getProperty().getValue());
            }
        });


        ComboBox mastersCountCombo = new ComboBox("Number of master nodes:", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        mastersCountCombo.setImmediate( true );
        mastersCountCombo.setTextInputAllowed( false );
        mastersCountCombo.setNullSelectionAllowed( false );
        mastersCountCombo.setValue( wizard.getConfig() );

        mastersCountCombo.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                wizard.getConfig().setNumberOfMasterNodes( ( Integer ) event.getProperty().getValue() );
            }
        } );

        ComboBox dataNodesCountCombo = new ComboBox("Number of data nodes:", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        dataNodesCountCombo.setImmediate( true );
        dataNodesCountCombo.setTextInputAllowed( false );
        dataNodesCountCombo.setNullSelectionAllowed( false );
        dataNodesCountCombo.setValue( wizard.getConfig() );

        dataNodesCountCombo.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                wizard.getConfig().setNumberOfDataNodes( ( Integer ) event.getProperty().getValue() );
            }
        } );


        ComboBox numberOfShardsCombo = new ComboBox("Number of shards:", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        numberOfShardsCombo.setImmediate( true );
        numberOfShardsCombo.setTextInputAllowed( false );
        numberOfShardsCombo.setNullSelectionAllowed( false );
        numberOfShardsCombo.setValue( wizard.getConfig() );

        numberOfShardsCombo.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                wizard.getConfig().setNumberOfShards( ( Integer ) event.getProperty().getValue() );
            }
        } );


        ComboBox numberOfReplicasCombo = new ComboBox("Number of replicas:", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        numberOfReplicasCombo.setImmediate( true );
        numberOfReplicasCombo.setTextInputAllowed( false );
        numberOfReplicasCombo.setNullSelectionAllowed( false );
        numberOfReplicasCombo.setValue( wizard.getConfig() );

        numberOfReplicasCombo.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                wizard.getConfig().setNumberOfReplicas( ( Integer ) event.getProperty().getValue() );
            }
        } );

		Button next = new Button("Next");
		next.addStyleName("default");
		next.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				if (Util.isStringEmpty(wizard.getConfig().getClusterName())) {
					show("Please provide cluster name");
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

		content.addComponent(clusterNameTxtFld);
		content.addComponent(nodesCountCombo);
		content.addComponent(mastersCountCombo);
		content.addComponent(dataNodesCountCombo);
		content.addComponent(numberOfShardsCombo);
		content.addComponent(numberOfReplicasCombo);
		content.addComponent(buttons);

		addComponent(layout);

	}

	private void show(String notification) {
		Notification.show(notification);
	}

}
