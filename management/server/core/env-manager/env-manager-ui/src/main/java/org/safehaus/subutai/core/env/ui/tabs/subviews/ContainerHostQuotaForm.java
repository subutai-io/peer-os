package org.safehaus.subutai.core.env.ui.tabs.subviews;


import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


public class ContainerHostQuotaForm extends VerticalLayout
{


    private TextField ramQuotaTextField = new TextField( "RAM Quota" );

    private TextField cpuQuotaTextField = new TextField( "CPU Quota" );

    private TextField diskHomeTextField = new TextField( "Home directory quota" );

    private TextField diskRootfsTextField = new TextField( "Rootfs directory quota" );

    private TextField diskVarTextField = new TextField( "Var directory quota" );

    private TextField diskOptTextField = new TextField( "Opt directory quota" );

    private Button updateChanges = new Button( "Update changes" );

    private ContainerHost containerHost;

    private int prevRamQuota;
    private int prevCpuQuota;
    private DiskQuota prevHomeDiskQuota;
    private DiskQuota prevVarDiskQuota;
    private DiskQuota prevOptDiskQuota;
    private DiskQuota prevRootFsDiskQuota;

    private ExecutorService executorService;
    private Component parent;


    public ContainerHostQuotaForm( ExecutorService executorService, Component parent )
    {
        this.executorService = executorService;
        this.parent = parent;
        init();
    }


    private void init()
    {
        setSpacing( true );
        FormLayout form = new FormLayout();
        form.addComponents( ramQuotaTextField, cpuQuotaTextField, diskHomeTextField, diskVarTextField,
                diskRootfsTextField, diskOptTextField, updateChanges );
        addComponent( form );
        updateChanges.addClickListener( updateChangesListener );
    }


    public void setContainerHost( final ContainerHost containerHost )
    {

        try
        {
            this.containerHost = containerHost;

            prevRamQuota = containerHost.getRamQuota();
            prevCpuQuota = containerHost.getCpuQuota();
            prevHomeDiskQuota = containerHost.getDiskQuota( DiskPartition.HOME );
            prevOptDiskQuota = containerHost.getDiskQuota( DiskPartition.OPT );
            prevRootFsDiskQuota = containerHost.getDiskQuota( DiskPartition.ROOT_FS );
            prevVarDiskQuota = containerHost.getDiskQuota( DiskPartition.VAR );

            ramQuotaTextField.setValue( String.valueOf( prevRamQuota ) );
            cpuQuotaTextField.setValue( String.valueOf( prevCpuQuota ) );
            diskHomeTextField.setValue( prevHomeDiskQuota.getQuotaValue() );
            diskVarTextField.setValue( prevVarDiskQuota.getQuotaValue() );
            diskOptTextField.setValue( prevOptDiskQuota.getQuotaValue() );
            diskRootfsTextField.setValue( prevRootFsDiskQuota.getQuotaValue() );
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
                updateChanges.setEnabled( false );
                parent.setEnabled( false );

                Notification.show( "Please, wait..." );
                final int newRamQuota = Integer.parseInt( ramQuotaTextField.getValue() );

                final int newCpuQuota = Integer.parseInt( cpuQuotaTextField.getValue() );

                final DiskQuota newHomeDiskQuota = DiskQuota.parse( DiskPartition.HOME, diskHomeTextField.getValue() );
                final DiskQuota newRootFsDiskQuota =
                        DiskQuota.parse( DiskPartition.ROOT_FS, diskRootfsTextField.getValue() );
                final DiskQuota newOptDiskQuota = DiskQuota.parse( DiskPartition.OPT, diskOptTextField.getValue() );
                final DiskQuota newVarDiskQuota = DiskQuota.parse( DiskPartition.VAR, diskVarTextField.getValue() );

                executorService.submit( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {

                            if ( newRamQuota != prevRamQuota )
                            {
                                containerHost.setRamQuota( newRamQuota );
                                prevRamQuota = newRamQuota;
                            }

                            if ( newCpuQuota != prevCpuQuota )
                            {
                                containerHost.setCpuQuota( newCpuQuota );
                                prevCpuQuota = newCpuQuota;
                            }

                            if ( !newHomeDiskQuota.equals( prevHomeDiskQuota ) )
                            {
                                containerHost.setDiskQuota( newHomeDiskQuota );
                                prevHomeDiskQuota = newHomeDiskQuota;
                            }

                            if ( !newRootFsDiskQuota.equals( prevRootFsDiskQuota ) )
                            {
                                containerHost.setDiskQuota( newRootFsDiskQuota );
                                prevRootFsDiskQuota = newRootFsDiskQuota;
                            }

                            if ( !newOptDiskQuota.equals( prevOptDiskQuota ) )
                            {
                                containerHost.setDiskQuota( newOptDiskQuota );
                                prevOptDiskQuota = newOptDiskQuota;
                            }

                            if ( !newVarDiskQuota.equals( prevVarDiskQuota ) )
                            {
                                containerHost.setDiskQuota( newVarDiskQuota );
                                prevVarDiskQuota = newVarDiskQuota;
                            }

                            Notification.show( "Quotas are updated" );
                        }
                        catch ( Exception e )
                        {
                            Notification.show( String.format( "Error setting quota: %s", e.getMessage() ),
                                    Notification.Type.ERROR_MESSAGE );
                        }
                        finally
                        {
                            parent.setEnabled( true );
                            updateChanges.setEnabled( true );
                        }
                    }
                } );
            }
            catch ( Exception e )
            {
                updateChanges.setEnabled( true );
                parent.setEnabled( true );
                Notification.show( String.format( "Error setting quota: %s", e.getMessage() ),
                        Notification.Type.ERROR_MESSAGE );
            }
        }
    };
}