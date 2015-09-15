package io.subutai.core.peer.ui.registration;


import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.*;

import io.subutai.common.protocol.Template;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.core.registry.api.TemplateRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by talas on 8/24/15.
 */
public class ResourceHostRegistration extends CustomComponent
{

    private static final Logger LOGGER = LoggerFactory.getLogger( ResourceHostRegistration.class );
    private static final String VALUE_PROPERTY = "value";
    private static final String ICON = "icon";
    private static final String CAPTION = "caption";
    private static final String PHYSICAL_IMG = "img/lxc/physical.png";
    private static final String TEMPLATE_PROPERTY = "Template Property";
    private static final String TEMPLATE_VALUE = "Value";

    private BeanContainer<String, ResourceHostInfo> requestedResourceHosts;



    private interface TemplateValue
    {
        String getTemplateProperty( Template template );
    }


    public ResourceHostRegistration( final TemplateRegistry registryManager )
    {
        setHeight( 100, Unit.PERCENTAGE );

        initializeControls();
        setupView();
    }


    private void initializeControls()
    {
        requestedResourceHosts = new BeanContainer<>( ResourceHostInfo.class );
        requestedResourceHosts.setBeanIdProperty( "requestedResourceHosts" );
    }


    private void setupView()
    {
        // Build templates layout
        HorizontalLayout generalInfoLayout = new HorizontalLayout();
        generalInfoLayout.setSpacing( true );
        generalInfoLayout.setSizeFull();
        generalInfoLayout.setId( "generalInfoHorizontalLayoutId" );
    }
}


