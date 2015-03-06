package org.safehaus.subutai.core.env.ui.tabs.subviews;


import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


public class ContainerHostQuotaForm extends VerticalLayout
{

    private Logger LOGGER = LoggerFactory.getLogger( ContainerHostQuotaForm.class );

    private TextField ramQuotaTextField = new TextField( "RAM Quota" );

    private TextField cpuQuotaTextField = new TextField( "CPU Quota" );

    private TextField diskHomeTextField = new TextField( "Home directory quota" );

    private TextField diskRootfsTextField = new TextField( "Rootfs directory quota" );

    private TextField diskVarTextField = new TextField( "Var directory quota" );

    private TextField diskOptTextField = new TextField( "Opt directory quota" );

    private Button updateChanges = new Button( "Update changes" );

    private ContainerHost containerHost;


    public ContainerHostQuotaForm()
    {
        init();
    }


    private void init()
    {
        FormLayout form = new FormLayout();
        form.addComponents( ramQuotaTextField, cpuQuotaTextField, diskHomeTextField, diskVarTextField,
                diskRootfsTextField, diskOptTextField, updateChanges );
        addComponent( form );
        updateChanges.addClickListener( updateChangesListener );
        setSpacing( true );
    }


    public void setContainerHostBeanItem( BeanItem<ContainerHost> containerHostBeanItem )
    {
        try
        {
            containerHost = containerHostBeanItem.getBean();
            ramQuotaTextField.setValue( String.valueOf( containerHost.getRamQuota() ) );
            cpuQuotaTextField.setValue( String.valueOf( containerHost.getCpuQuota() ) );
            diskHomeTextField.setValue( containerHost.getDiskQuota( DiskPartition.HOME ).getQuotaValue() );
            diskVarTextField.setValue( containerHost.getDiskQuota( DiskPartition.VAR ).getQuotaValue() );
            diskOptTextField.setValue( containerHost.getDiskQuota( DiskPartition.OPT ).getQuotaValue() );
            diskRootfsTextField.setValue( containerHost.getDiskQuota( DiskPartition.ROOT_FS ).getQuotaValue() );
        }
        catch ( PeerException e )
        {
            Notification.show( String.format( "Error getting quota: %s", e.getMessage() ),
                    Notification.Type.ERROR_MESSAGE );
        }
    }


    private Button.ClickListener updateChangesListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            try
            {
                Notification.show( "Please, wait..." );

                containerHost.setRamQuota( Integer.parseInt( ramQuotaTextField.getValue() ) );

                containerHost.setCpuQuota( Integer.parseInt( cpuQuotaTextField.getValue() ) );

                containerHost.setDiskQuota( DiskQuota.parse( DiskPartition.HOME, diskHomeTextField.getValue() ) );

                containerHost.setDiskQuota( DiskQuota.parse( DiskPartition.ROOT_FS, diskRootfsTextField.getValue() ) );

                containerHost.setDiskQuota( DiskQuota.parse( DiskPartition.OPT, diskOptTextField.getValue() ) );

                containerHost.setDiskQuota( DiskQuota.parse( DiskPartition.VAR, diskVarTextField.getValue() ) );

                Notification.show( "Quotas are updated" );
            }
            catch ( Exception e )
            {
                Notification.show( String.format( "Error setting quota: %s", e.getMessage() ),
                        Notification.Type.ERROR_MESSAGE );
            }
        }
    };
}