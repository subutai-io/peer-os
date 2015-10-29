package io.subutai.core.environment.ui.tabs;


import java.util.concurrent.ExecutorService;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import io.subutai.common.environment.Environment;
import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.common.peer.ContainerHost;
import io.subutai.core.environment.ui.EnvironmentManagerComponent;
import io.subutai.core.environment.ui.tabs.subviews.ContainerHostQuotaForm;


public class EnvironmentContainersQuotaTab extends CustomComponent
{
    private EnvironmentManagerComponent environmentComponent;

    private ExecutorService executorService;
    private ContainerHostQuotaForm form = new ContainerHostQuotaForm( this );

    private Label indicator;

    private BeanContainer<String, ContainerHost> envContainerHostContainer =
            new BeanContainer<String, ContainerHost>( ContainerHost.class )
            {
                {
                    setBeanIdProperty( "id" );
                }
            };

    private Table containerHosts = new Table( "Environment container hosts", envContainerHostContainer )
    {
        {
            setVisibleColumns( "hostname", "templateName" );
            setColumnHeader( "hostname", "Container name" );
            setColumnHeader( "templateName", "Template" );
            setBuffered( true );
            setSelectable( true );
            addValueChangeListener( new ValueChangeListener()
            {
                @Override
                public void valueChange( final Property.ValueChangeEvent event )
                {
                    if ( event.getProperty().getValue() != null )
                    {
                        containerHosts.setEnabled( false );
                        envListComboBox.setEnabled( false );
                        executorService.submit( new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        try
                                                        {
                                                            showHideIndicator( true );
                                                            form.setVisible( false );
                                                            ContainerHost containerHost = envContainerHostContainer
                                                                    .getItem( containerHosts.getValue() ).getBean();
                                                            form.setContainerHost( containerHost );
                                                        }
                                                        finally
                                                        {
                                                            containerHosts.setEnabled( true );
                                                            envListComboBox.setEnabled( true );
                                                            form.setVisible( true );
                                                            showHideIndicator( false );
                                                        }
                                                    }
                                                }


                                              );
                    }
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


    public EnvironmentContainersQuotaTab( final EnvironmentManagerComponent environmentComponent )
    {
    this.environmentComponent = environmentComponent;
        init();
    }


    private void init()
    {
        VerticalLayout verticalLayout = new VerticalLayout();

        final BeanContainer<String, Environment> environmentContainer = new BeanContainer<>( Environment.class );
        environmentContainer.setBeanIdProperty( "id" );
        environmentContainer.addAll( environmentComponent.getEnvironmentManager().getEnvironments() );

        envListComboBox.setContainerDataSource( environmentContainer );
        envListComboBox.addFocusListener( new FieldEvents.FocusListener()
        {
            @Override
            public void focus( final FieldEvents.FocusEvent event )
            {
                envListComboBox.setValue( null );
                environmentContainer.removeAllItems();
                environmentContainer.addAll( environmentComponent.getEnvironmentManager().getEnvironments() );
            }
        } );
        envListComboBox.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                updateContainersTable();
            }
        } );

        form.setVisible( false );


        indicator = new Label();
        indicator.setIcon( new ThemeResource( "img/spinner.gif" ) );
        indicator.setContentMode( ContentMode.HTML );
        indicator.setHeight( 11, Unit.PIXELS );
        indicator.setWidth( 50, Unit.PIXELS );
        indicator.setVisible( false );

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.addComponents( envListComboBox, indicator );

        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.addComponents( containerHosts, form );

        verticalLayout.addComponents( topRow, hLayout );

        setCompositionRoot( verticalLayout );

        addAttachListener( new AttachListener()
        {
            @Override
            public void attach( final AttachEvent event )
            {
                executorService = SubutaiExecutors.newCachedThreadPool();
                form.setExecutorService( executorService );
            }
        } );
        addDetachListener( new DetachListener()
        {
            @Override
            public void detach( final DetachEvent event )
            {
                executorService.shutdown();
            }
        } );
    }


    private void showHideIndicator( boolean showHide )
    {
        if ( showHide )
        {
            getUI().access( new Runnable()
            {
                @Override
                public void run()
                {
                    indicator.setVisible( true );
                }
            } );
        }
        else
        {
            getUI().access( new Runnable()
            {
                @Override
                public void run()
                {
            indicator.setVisible( false );
                }
            } );
        }
    }


    private void updateContainersTable()
    {
        String envId = envListComboBox.getValue().toString();
        envContainerHostContainer.removeAllItems();
        if ( envId != null )
        {
            form.setVisible( false );

            BeanItem beanItem = ( BeanItem ) envListComboBox.getItem( envId );
            Environment selectedEnvironment = ( Environment ) beanItem.getBean();

            envContainerHostContainer.addAll( selectedEnvironment.getContainerHosts() );
        }
    }
}
