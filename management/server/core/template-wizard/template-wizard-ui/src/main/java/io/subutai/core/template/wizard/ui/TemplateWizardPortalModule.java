package io.subutai.core.template.wizard.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.subutai.common.util.FileUtil;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.core.template.wizard.api.TemplateWizardManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class TemplateWizardPortalModule implements PortalModule
{
    private final static String MODULE_IMAGE = "magic.gif";
    private final static String MODULE_NAME = "Template Wizard";

    private TemplateRegistry templateRegistry;
    private PeerManager peerManager;
    private Tracker tracker;
    private ExecutorService executor;
    private TemplateWizardManager templateWizard;


    public Tracker getTracker()
    {
        return tracker;
    }


    public TemplateRegistry getTemplateRegistry()
    {
        return templateRegistry;
    }


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public TemplateWizardManager getTemplateWizard()
    {
        return templateWizard;
    }


    public void setTemplateWizard( final TemplateWizardManager templateWizard )
    {
        this.templateWizard = templateWizard;
    }


    public TemplateWizardPortalModule( final TemplateRegistry templateRegistry, final PeerManager peerManager,
                                       final Tracker tracker )
    {
        this.executor = Executors.newSingleThreadExecutor();
        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.tracker = tracker;
    }


    public void dispose()
    {
        executor.shutdown();
    }


    @Override
    public String getId()
    {
        return MODULE_NAME;
    }


    @Override
    public String getName()
    {
        return MODULE_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        return new TemplateWizardComponent( this );
    }


    /**
     * Function to differentiate core plugins from plugins needed to show-up in different tabs in main dashboard
     */
    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
    }
}