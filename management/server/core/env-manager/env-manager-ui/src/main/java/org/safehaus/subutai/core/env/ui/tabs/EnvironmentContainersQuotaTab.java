package org.safehaus.subutai.core.env.ui.tabs;


import java.util.UUID;

import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.env.ui.EnvironmentManagerComponent;
import org.safehaus.subutai.core.env.ui.tabs.subviews.ContainerHostQuotaForm;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class EnvironmentContainersQuotaTab extends CustomComponent
{
    private EnvironmentManagerComponent environmentComponent;

    private BeanContainer<String, Environment> environmentContainer;

    private ContainerHostQuotaForm form = new ContainerHostQuotaForm();

    private static final String LOAD_ICON_SOURCE = "img/spinner.gif";

    private BeanContainer<String, ContainerHost> envContainerHostContainer =
            new BeanContainer<String, ContainerHost>( ContainerHost.class )
            {
                {
                    setBeanIdProperty( "id" );
                    addNestedContainerProperty( "ramQuota" );
                    addNestedContainerProperty( "cpuQuota" );
                }
            };

    private Table containerHosts = new Table( "Environment container hosts", envContainerHostContainer )
    {
        {
            setVisibleColumns( new Object[] { "hostname", "templateName", "ramQuota", "cpuQuota" } );
            setColumnHeader( "hostname", "Container name" );
            setColumnHeader( "templateName", "Template" );
            setColumnHeader( "ramQuota", "Ram Quota (MB)" );
            setColumnHeader( "cpuQuota", "CPU Quota (%)" );
            setBuffered( false );
            setSelectable( true );
            addValueChangeListener( new ValueChangeListener()
            {
                @Override
                public void valueChange( final Property.ValueChangeEvent event )
                {
                    form.setVisible( false );
                    new Thread( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if ( event.getProperty().getValue() == null )
                            {
                                form.setVisible( false );
                                return;
                            }
                            BeanItem<ContainerHost> permission =
                                    envContainerHostContainer.getItem( containerHosts.getValue() );
                            form.setContainerHostBeanItem( permission );
                            form.setVisible( true );
                        }
                    } ).start();
                }
            } );
        }
    };

    private ComboBox envListComboBox = new ComboBox( "Environments" )
    {
        {
            setItemCaptionMode( ItemCaptionMode.PROPERTY );
            setItemCaptionPropertyId( "name" );
            setNullSelectionAllowed( false );
            setImmediate( true );
            setTextInputAllowed( false );
            setRequired( true );
        }
    };


    private Window showProgress;


    public EnvironmentContainersQuotaTab( final EnvironmentManagerComponent environmentComponent )
    {
        this.environmentComponent = environmentComponent;
        init();
    }


    private void init()
    {
        VerticalLayout verticalLayout = new VerticalLayout();

        environmentContainer = new BeanContainer<>( Environment.class );
        environmentContainer.setBeanIdProperty( "id" );
        environmentContainer.addAll( environmentComponent.getEnvironmentManager().getEnvironments() );

        envListComboBox.setContainerDataSource( environmentContainer );
        envListComboBox.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                getUI().addWindow( showProgress );
                showProgress.focus();
            }
        } );

        form.setVisible( false );
        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.addComponents( containerHosts, form );

        verticalLayout.addComponents( envListComboBox, hLayout );

        setCompositionRoot( verticalLayout );

        // Notify user about ongoing progress
        Label icon = new Label();
        icon.setId( "indicator" );
        icon.setIcon( new ThemeResource( LOAD_ICON_SOURCE ) );
        icon.setContentMode( ContentMode.HTML );
        //        icon.setHeight( 11, Sizeable.Unit.PIXELS );
        //        icon.setWidth( 50, Sizeable.Unit.PIXELS );

        HorizontalLayout indicatorLayout = new HorizontalLayout();
        indicatorLayout.addComponent( icon );
        indicatorLayout.setComponentAlignment( icon, Alignment.TOP_LEFT );

        showProgress = new Window( "", indicatorLayout );
        showProgress.setModal( true );
        showProgress.setClosable( false );
        showProgress.setResizable( false );
        showProgress.center();
        showProgress.addFocusListener( new FieldEvents.FocusListener()
        {
            @Override
            public void focus( final FieldEvents.FocusEvent event )
            {
                updateContainersTable();
            }
        } );
    }


    private void updateContainersTable()
    {
        form.setVisible( false );
        UUID envId = ( UUID ) envListComboBox.getValue();

        if ( envId == null )
        {
            return;
        }

        BeanItem beanItem = ( BeanItem ) envListComboBox.getItem( envId );
        Environment selectedEnvironment = ( Environment ) beanItem.getBean();

        envContainerHostContainer.removeAllItems();
        envContainerHostContainer.addAll( selectedEnvironment.getContainerHosts() );

        showProgress.close();
    }
}
