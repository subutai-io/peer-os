import java.io.File;
import java.util.concurrent.ExecutorService;

import com.vaadin.ui.Component;

import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.server.ui.api.PortalModule;


public class NodeRegistrationPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "plugs.png";
    public static final String MODULE_NAME = "Plugin";
    private RegistrationManager registrationManager;
    private ExecutorService executor;
    private Tracker tracker;


    @Override
    public String getId()
    {
        return null;
    }


    @Override
    public String getName()
    {
        return null;
    }


    @Override
    public File getImage()
    {
        return null;
    }


    @Override
    public Component createComponent()
    {
        return null;
    }


    @Override
    public Boolean isCorePlugin()
    {
        return null;
    }
}
