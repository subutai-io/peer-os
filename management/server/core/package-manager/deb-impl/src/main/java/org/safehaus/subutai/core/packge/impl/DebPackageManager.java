package org.safehaus.subutai.core.packge.impl;


import java.util.Collection;

import org.safehaus.subutai.core.packge.api.PackageInfo;
import org.safehaus.subutai.core.packge.impl.handler.DeleteHandler;
import org.safehaus.subutai.core.packge.impl.handler.FindHandler;
import org.safehaus.subutai.core.packge.impl.handler.ListHandler;
import org.safehaus.subutai.core.packge.impl.handler.SaveHandler;


public class DebPackageManager extends DebPackageManagerBase
{

    @Override
    public Collection<PackageInfo> listPackages( String hostname )
    {
        return listPackages( hostname, null );
    }


    @Override
    public Collection<PackageInfo> listPackages( String hostname, String namePattern )
    {
        ListHandler h = new ListHandler( this, hostname );
        h.setNamePattern( namePattern );
        return h.performAction();
    }


    @Override
    public Collection<PackageInfo> findPackagesInfo( String hostname )
    {
        FindHandler h = new FindHandler( this, hostname );
        return h.performAction();
    }


    @Override
    public Collection<PackageInfo> savePackagesInfo( String hostname )
    {
        SaveHandler h = new SaveHandler( this, hostname );
        return h.performAction();
    }


    @Override
    public boolean deletePackagesInfo( String hostname )
    {
        DeleteHandler h = new DeleteHandler( this, hostname );
        Boolean b = h.performAction();
        return b != null && b;
    }
}
