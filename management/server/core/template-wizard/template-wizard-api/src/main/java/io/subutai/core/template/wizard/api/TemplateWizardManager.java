package io.subutai.core.template.wizard.api;


import java.util.List;
import java.util.UUID;

import io.subutai.common.tracker.TrackerOperation;


/**
 * Manages template creation process with passed parameters for it. They might be software products to install,
 * pre-installation scripts and post-installation scripts.
 */
public interface TemplateWizardManager
{
    public InstallationPhase getCurrentPhase();

    public void createTemplate( final String newTemplateName, final String templateName,
                                List<String> postInstallationScripts, List<String> products,
                                List<String> preInstallationScripts, String resourceHostId,
                                final TrackerOperation trackerOperation );

    public void preInstallationScripts( List<String> scripts );

    public void postInstallationScripts( List<String> scripts );

    public void createContainerHost( String newTemplateName, String templateName, final String uuid,
                                     final TrackerOperation trackerOperation );

    public void installProducts( List<String> products, String containerHostId, TrackerOperation trackerOperation );
}
