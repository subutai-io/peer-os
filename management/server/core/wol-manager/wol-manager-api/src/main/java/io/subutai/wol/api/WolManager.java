package io.subutai.wol.api;


import java.util.ArrayList;

import org.safehaus.subutai.common.command.CommandResult;


public interface WolManager
{

    public CommandResult sendMagicPackageByMacId( String macId ) throws WolManagerException;

    public Boolean sendMagicPackageByList( ArrayList<String> macList ) throws WolManagerException;
}
