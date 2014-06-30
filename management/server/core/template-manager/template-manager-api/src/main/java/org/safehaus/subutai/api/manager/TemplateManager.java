package org.safehaus.subutai.api.manager;

/**
 * A wrapper interface that wraps LXC and ZFS command scripts on physical hosts.
 *
 */
public interface TemplateManager {

    public String getMasterTemplateName();

    /**
     * Clone an instance container from a given template.
     *
     * @param hostName the physical host name
     * @param templateName the template name from which a new instance container
     * is to be cloned
     * @param cloneName the clone name of the new instance container
     * @return <tt>true</tt> if successfully cloned, <tt>false</tt> otherwise
     */
    public boolean clone(String hostName, String templateName, String cloneName);

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
    public boolean promoteClone(String hostName, String cloneName);

    public boolean importTemplate(String hostName, String templateName);

    public boolean exportTemplate(String hostName, String templateName);
}
