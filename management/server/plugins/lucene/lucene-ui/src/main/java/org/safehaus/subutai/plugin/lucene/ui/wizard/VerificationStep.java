package org.safehaus.subutai.plugin.lucene.ui.wizard;


import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.plugin.lucene.api.*;
import org.safehaus.subutai.plugin.lucene.ui.LuceneUI;

import java.util.UUID;


public class VerificationStep extends Panel {

	public VerificationStep(final Wizard wizard) {

		setSizeFull();

		GridLayout grid = new GridLayout(1, 5);
		grid.setSpacing(true);
		grid.setMargin(true);
		grid.setSizeFull();

		Label confirmationLbl = new Label("<strong>Please verify the installation settings "
				+ "(you may change them by clicking on Back button)</strong><br/>");
		confirmationLbl.setContentMode(ContentMode.HTML);

        final Config config = wizard.getConfig();

        ConfigView cfgView = new ConfigView( "Installation configuration" );
        cfgView.addStringCfg( "Installation Name", wizard.getConfig().getClusterName() );
        cfgView.addStringCfg( "Hadoop cluster name", wizard.getConfig().getHadoopClusterName() );

        if ( config.getSetupType() == SetupType.OVER_HADOOP ) {
            for ( Agent agent : wizard.getConfig().getNodes() ) {
                cfgView.addStringCfg( "Node to install", agent.getHostname() + "" );
            }
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP ) {
            HadoopClusterConfig hc = wizard.getHadoopConfig();

            cfgView.addStringCfg( "Number of Hadoop slave nodes", hc.getCountOfSlaveNodes() + "" );
            cfgView.addStringCfg( "Replication factor", hc.getReplicationFactor() + "" );
            cfgView.addStringCfg( "Domain name", hc.getDomainName() );
        }

        Button install = new Button( "Install" );
        install.addStyleName( "default" );
        install.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                UUID trackId = null;

                if ( config.getSetupType() == SetupType.OVER_HADOOP ) {
                    trackId = LuceneUI.getLuceneManager().installCluster( config );
                }
                else if ( config.getSetupType() == SetupType.WITH_HADOOP ) {
                    trackId = LuceneUI.getLuceneManager().installCluster( config, wizard.getHadoopConfig() );
                }

                ProgressWindow window = new ProgressWindow( LuceneUI.getExecutor(), LuceneUI.getTracker(), trackId,
                        Config.PRODUCT_KEY );
                window.getWindow().addCloseListener( new Window.CloseListener() {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent ) {
                        wizard.init();
                    }
                } );

                getUI().addWindow( window.getWindow() );
            }
        } );

		Button back = new Button("Back");
		back.addStyleName("default");
		back.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				wizard.back();
			}
		});

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.addComponent(back);
		buttons.addComponent(install);

		grid.addComponent(confirmationLbl, 0, 0);

		grid.addComponent(cfgView.getCfgTable(), 0, 1, 0, 3);

		grid.addComponent(buttons, 0, 4);

		setContent(grid);
	}
}
