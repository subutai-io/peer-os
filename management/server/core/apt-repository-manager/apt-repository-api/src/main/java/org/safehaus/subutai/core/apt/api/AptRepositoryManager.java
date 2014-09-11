/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.apt.api;


import org.safehaus.subutai.common.protocol.Agent;

import java.util.List;


/**
 * This class provids APi to add debian package to APT repository, remove it, list packages and read files inside
 * packages
 */
public interface AptRepositoryManager {


	/**
	 * Returns list of packages in apt repository
	 *
	 * @param agent   - agent of node where apt repository resides
	 * @param pattern - pattern to search for in packages names, might be empty
	 * @return - list of packages {@code PackageInfo}
	 */
	public List<PackageInfo> listPackages(Agent agent, String pattern) throws AptRepoException;

	/**
	 * Adds debian packages to apt repository. As a result of this call all connected agents will execute apt-get
	 * update.
	 *
	 * @param agent               - agent of node where apt repository resides
	 * @param pathToPackageFile   - absolute path to debian package file
	 * @param deleteSourcePackage - indicates whether to delete source package after addition to apt repo
	 */
	public void addPackageByPath(Agent agent, String pathToPackageFile, boolean deleteSourcePackage)
			throws AptRepoException;

	/**
	 * Removes package from apt repository.As a result of this call all connected agents will execute apt-get
	 *
	 * @param agent       - agent of node where apt repository resides
	 * @param packageName - name of package to delete
	 */
	public void removePackageByName(Agent agent, String packageName) throws AptRepoException;

	/**
	 * Returns contents of files inside debian packages
	 *
	 * @param agent                     -agent of node where package resides
	 * @param pathToPackageFile         - absolute path to debian package file
	 * @param pathsToFilesInsidePackage - relative paths to files whose contents to return
	 * @return - list of contents of files in the same order as in  @pathsToFilesInsidePackage argument
	 */
	public List<String> readFileContents(Agent agent, final String pathToPackageFile,
	                                     final List<String> pathsToFilesInsidePackage) throws AptRepoException;
}
