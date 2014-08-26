package org.safehaus.subutai.api.template.manager;


import java.util.Set;


/**
 * A wrapper interface that wraps LXC and ZFS command scripts on physical hosts.
 */
public interface TemplateManager {

	public String getMasterTemplateName();

	/**
	 * Checks environment and setups master template. It is safe to run this method multiple times.
	 *
	 * @param hostName the physical host name
	 * @return <tt>true</tt> if check and setup is successful, <tt>false</tt> otherwise
	 */
	public boolean setup(String hostName);

	/**
	 * Clone an instance container from a given template.
	 *
	 * @param hostName     the physical host name
	 * @param templateName the template name from which a new instance container is to be cloned
	 * @param cloneName    the clone name of the new instance container
	 * @return <tt>true</tt> if successfully cloned, <tt>false</tt> otherwise
	 */
	public boolean clone(String hostName, String templateName, String cloneName);

	public boolean clone(String hostName, String templateName, Set<String> cloneNames);

	/**
	 * Destroys a clone with given name.
	 *
	 * @param hostName  the physical host name
	 * @param cloneName name of a clone to be destroyed
	 * @return <tt>true</tt> if destroyed successfully, <tt>false</tt> otherwise
	 */
	public boolean cloneDestroy(String hostName, String cloneName);

	public boolean cloneDestroy(String hostName, Set<String> cloneNames);

	/**
	 * Renames an instance container.
	 *
	 * @param hostName the physical host name where container resides
	 * @param oldName  current name of the container
	 * @param newName  new name for the container
	 * @return <tt>true</tt> if successfully renamed; <tt>false</tt> otherwise
	 */
	public boolean cloneRename(String hostName, String oldName, String newName);

	/**
	 * Promotes a given clone into a template.
	 *
	 * @param hostName  the physical host name
	 * @param cloneName name of the clone to be converted
	 * @return <tt>true</tt> if promote successfully completed
	 */
	public boolean promoteClone(String hostName, String cloneName);

	/**
	 * Promotes a given clone into a template with given name. This method gives possibility to promote a copy of the
	 * clone instead of the clone itself.
	 *
	 * @param hostName  the physical host name
	 * @param cloneName name of the clone to be converted
	 * @param newName   new name for template
	 * @param copyit    if set <tt>true</tt>, a copy of clone is made first and a copied clone is promoted to template
	 * @return <tt>true</tt> if promote successfully completed
	 */
	public boolean promoteClone(String hostName, String cloneName, String newName, boolean copyit);

	public boolean importTemplate(String hostName, String templateName);

	/**
	 * Exports the template in the given server into a deb package.
	 *
	 * @param hostName     the physical host name
	 * @param templateName the template name to be exported
	 * @return path to generated deb package
	 */
	public String exportTemplate(String hostName, String templateName);
}
