package org.safehaus.subutai.wol.api;


import org.safehaus.subutai.common.command.CommandResult;
import java.util.ArrayList;

/**
 * Created by emin on 14/11/14.
 */
public interface WolManager
{

    public CommandResult sendMagicPackagebyMacId(String macId) throws WolManagerException;

    public Boolean sendMagicPackagebyList(ArrayList<String> macList) throws WolManagerException;

}
