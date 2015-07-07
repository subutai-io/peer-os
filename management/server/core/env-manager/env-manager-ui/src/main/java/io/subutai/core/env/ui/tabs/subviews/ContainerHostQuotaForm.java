package io.subutai.core.env.ui.tabs.subviews;


import java.util.concurrent.ExecutorService;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.PeerException;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.RamQuota;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


public class ContainerHostQuotaForm extends VerticalLayout
{
    private static final Logger LOGGER = LoggerFactory.getLogger( ContainerHostQuotaForm.class );

    private final TextField ramQuotaTextField;
    private final TextField cpuQuotaTextField;
    private final TextField diskHomeTextField;
    private final TextField diskRootfsTextField;
    private final TextField diskVarTextField;
    private final TextField diskOptTextField;

    private Button updateChanges = new Button( "Update changes" );

    private ContainerHost containerHost;

    private RamQuota prevRamQuota;
    private int prevCpuQuota;
    private DiskQuota prevHomeDiskQuota;
    private DiskQuota prevVarDiskQuota;
    private DiskQuota prevOptDiskQuota;
    private DiskQuota prevRootFsDiskQuota;

    private ExecutorService executorService;
    private Component parent;


    public ContainerHostQuotaForm( Component parent )
    {
        this.parent = parent;
        ramQuotaTextField = new TextField( "RAM Quota" );
        ramQuotaTextField.setBuffered( true );

        cpuQuotaTextField = new TextField( "CPU Quota" );
        cpuQuotaTextField.setBuffered( true );

        diskHomeTextField = new TextField( "Home directory quota" );
        diskHomeTextField.setBuffered( true );

        diskRootfsTextField = new TextField( "Rootfs directory quota" );
        diskRootfsTextField.setBuffered( true );

        diskVarTextField = new TextField( "Var directory quota" );
        diskVarTextField.setBuffered( true );

        diskOptTextField = new TextField( "Opt directory quota" );
        diskOptTextField.setBuffered( true );

        init();
    }


    private void init()
    {
        setSpacing( true );
        FormLayout form = new FormLayout();
        form.addComponents( ramQuotaTextField, cpuQuotaTextField, diskHomeTextField, diskVarTextField,
                diskRootfsTextField, diskOptTextField, updateChanges );
        addComponent( form );

        Button.ClickListener updateChangesListener = new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {

            }
        };

        updateChanges.addClickListener( updateChangesListener );
    }


    private void updateChanges()
    {
        try
        {
            updateChanges.setEnabled( false );
            parent.setEnabled( false );

            Notification.show( "Please, wait..." );
            final RamQuota newRamQuota = RamQuota.parse( ramQuotaTextField.getValue() );

            final int newCpuQuota = Integer.parseInt( cpuQuotaTextField.getValue() );

            final DiskQuota newHomeDiskQuota = DiskQuota.parse( DiskPartition.HOME, diskHomeTextField.getValue() );
            final DiskQuota newRootFsDiskQuota =
                    DiskQuota.parse( DiskPartition.ROOT_FS, diskRootfsTextField.getValue() );
            final DiskQuota newOptDiskQuota = DiskQuota.parse( DiskPartition.OPT, diskOptTextField.getValue() );
            final DiskQuota newVarDiskQuota = DiskQuota.parse( DiskPartition.VAR, diskVarTextField.getValue() );

            Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    applyChanges( newRamQuota, newCpuQuota, newHomeDiskQuota, newRootFsDiskQuota, newOptDiskQuota,
                            newVarDiskQuota );
                }
            };
            executorService.submit( runnable );
        }
        catch ( Exception e )
        {
            updateChanges.setEnabled( true );
            parent.setEnabled( true );
            String msg = String.format( "Error setting quota: %s", e.getMessage() );
            Notification.show( msg, Notification.Type.ERROR_MESSAGE );
            LOGGER.error( "Error setting quota", e );
        }
    }


    private void applyChanges( final RamQuota newRamQuota, final int newCpuQuota, final DiskQuota newHomeDiskQuota,
                               final DiskQuota newRootFsDiskQuota, final DiskQuota newOptDiskQuota,
                               final DiskQuota newVarDiskQuota )
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
            String msg = String.format( "Error setting quota: %s", e.getMessage() );
            Notification.show( msg, Notification.Type.ERROR_MESSAGE );
            LOGGER.error( "Couldn't set container quota", e );
        }
        finally
        {
            parent.setEnabled( true );
            updateChanges.setEnabled( true );
        }
    }


    public void setContainerHost( final ContainerHost containerHost )
    {

        try
        {
            this.containerHost = containerHost;

            prevRamQuota = RamQuota.parse( String.valueOf( containerHost.getRamQuota() ) );
            prevCpuQuota = containerHost.getCpuQuota();
            prevHomeDiskQuota = containerHost.getDiskQuota( DiskPartition.HOME );
            prevOptDiskQuota = containerHost.getDiskQuota( DiskPartition.OPT );
            prevRootFsDiskQuota = containerHost.getDiskQuota( DiskPartition.ROOT_FS );
            prevVarDiskQuota = containerHost.getDiskQuota( DiskPartition.VAR );

            ramQuotaTextField.setValue( prevRamQuota.getQuotaValue() );
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
            LOGGER.error( "Couldn't get container quota", e );
        }
    }


    public void setExecutorService( final ExecutorService executorService )
    {
        this.executorService = executorService;
    }
}