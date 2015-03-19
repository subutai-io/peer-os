package org.safehaus.subutai.core.template.wizard.api;


/**
 * Created by talas on 3/18/15.
 */


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.tracker.TrackerOperation;


/**
 * Manages template creation process with passed parameters for it. They might be software products to install,
 * pre-installation scripts and post-installation scripts.
 */
public interface TemplateWizardManager
{
    public InstallationPhase getCurrentPhase();

    public void createTemplate( final String newTemplateName, final String templateName,
                                List<String> postInstallationScripts, List<String> products,
                                List<String> preInstallationScripts, UUID resourceHostId,
                                final TrackerOperation trackerOperation );

    public void preInstallationScripts( List<String> scripts );

    public void postInstallationScripts( List<String> scripts );

    public void createContainerHost( String newTemplateName, String templateName );

    public void installProducts( List<String> products );
}
