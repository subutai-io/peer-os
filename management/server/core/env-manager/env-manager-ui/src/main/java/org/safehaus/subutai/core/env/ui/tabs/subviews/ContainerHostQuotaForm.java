package org.safehaus.subutai.core.env.ui.tabs.subviews;


import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.DiskQuotaUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


/**
 * Created by talas on 2/13/15.
 */
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
            LOGGER.error( "Error getting quota", e );
        }
    }


    private Button.ClickListener updateChangesListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            try
            {
                Double ram = Double.parseDouble( ramQuotaTextField.getValue() );
                containerHost.setRamQuota( ram.intValue() );

                Double cpu = Double.parseDouble( cpuQuotaTextField.getValue() );
                containerHost.setCpuQuota( cpu.intValue() );


                if ( diskHomeTextField.getValue().equals( "none" ) )
                {
                    containerHost.setDiskQuota( new DiskQuota( DiskPartition.HOME, DiskQuotaUnit.UNLIMITED, -1 ) );
                }
                else
                {
                    Double diskHome = Double.parseDouble(
                            diskHomeTextField.getValue().substring( 0, diskHomeTextField.getValue().length() - 1 ) );
                    containerHost
                            .setDiskQuota( new DiskQuota( DiskPartition.HOME, DiskQuotaUnit.MB, diskHome.intValue() ) );
                }


                if ( diskRootfsTextField.getValue().equals( "none" ) )
                {
                    containerHost.setDiskQuota( new DiskQuota( DiskPartition.ROOT_FS, DiskQuotaUnit.UNLIMITED, -1 ) );
                }
                else
                {
                    Double diskRootfs = Double.parseDouble( diskRootfsTextField.getValue().substring( 0,
                            diskRootfsTextField.getValue().length() - 1 ) );
                    containerHost.setDiskQuota(
                            new DiskQuota( DiskPartition.ROOT_FS, DiskQuotaUnit.MB, diskRootfs.intValue() ) );
                }

                if ( diskOptTextField.getValue().equals( "none" ) )
                {
                    containerHost.setDiskQuota( new DiskQuota( DiskPartition.OPT, DiskQuotaUnit.UNLIMITED, -1 ) );
                }
                else
                {
                    Double diskOpt = Double.parseDouble(
                            diskOptTextField.getValue().substring( 0, diskOptTextField.getValue().length() - 1 ) );
                    containerHost
                            .setDiskQuota( new DiskQuota( DiskPartition.OPT, DiskQuotaUnit.MB, diskOpt.intValue() ) );
                }

                if ( diskVarTextField.getValue().equals( "none" ) )
                {
                    containerHost.setDiskQuota( new DiskQuota( DiskPartition.VAR, DiskQuotaUnit.UNLIMITED, -1 ) );
                }
                else
                {
                    Double diskVar = Double.parseDouble(
                            diskVarTextField.getValue().substring( 0, diskVarTextField.getValue().length() - 1 ) );
                    containerHost
                            .setDiskQuota( new DiskQuota( DiskPartition.VAR, DiskQuotaUnit.MB, diskVar.intValue() ) );
                }
            }
            catch ( PeerException e )
            {
                LOGGER.error( "Error updating quota", e );
            }
        }
    };
}