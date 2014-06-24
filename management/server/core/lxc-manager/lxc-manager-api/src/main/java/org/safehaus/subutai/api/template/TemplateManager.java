package org.safehaus.subutai.api.template;

import java.util.Set;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.shared.protocol.Agent;

/**
 * A wrapper interface that wraps LXC and ZFS command scripts on physical hosts.
 *
 */
public interface TemplateManager {

    /**
     * Clone an instance container from a given template.
     *
     * @param hostName the physical host name
     * @param templateName the template name from which a new instance container
     * is to be cloned
     * @param cloneName the clone name of the new instance container
     * @return an <tt>Agent</tt> instance representing newly created instance
     * container (clone)
     */
    public Agent clone(String hostName, String templateName, String cloneName);

    public Set<Agent> clone(Set<String> hostNames, String templateName, String cloneName);

    public Set<Agent> clone(Set<String> hostNames, String templateName, String cloneName, PlacementStrategyENUM strategy);

    /**
     * Destroys a clone with given name.
     *
     * @param hostName the physical host name
     * @param cloneName name of a clone to be destroyed
     * @return <tt>true</tt> if destroyed successfully, <tt>false</tt> otherwise
     */
    public boolean cloneDestroy(String hostName, String cloneName);

    /**
     * Promotes a given clone into a new template.
     *
     * @param hostName the physical host name
     * @param cloneName name of the clone to be converted
     */
    public boolean convertClone(String hostName, String cloneName);
}
