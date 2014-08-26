package org.safehaus.subutai.configuration.manager.api;


/**
 * Created by bahadyr on 7/19/14.
 */
public interface TextInjector {

	public boolean echoTextIntoAgent(String hostname, String pathToFile, String content);

	/**
	 * Gets the content of the given file on given agent.
	 *
	 * @param pathToFile
	 * @return
	 */
	public String catFile(String hostname, String pathToFile);

	public String getConfigTemplate(String path);
}
