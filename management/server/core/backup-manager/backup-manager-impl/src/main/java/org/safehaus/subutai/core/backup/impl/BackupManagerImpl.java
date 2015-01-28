package org.safehaus.subutai.core.backup.impl;


import java.util.Date;
import java.util.UUID;

import org.safehaus.subutai.core.backup.api.BackupManager;


/**
 * Created by talas on 12/25/14.
 */
public class BackupManagerImpl implements BackupManager
{
    @Override
    public void backupContainer( final String containerName )
    {

    }


    @Override
    public void restoreContainer( final String containerName, final UUID backupId )
    {

    }


    @Override
    public void getRecentBackupList()
    {

    }


    @Override
    public void getContainerBackupList( final String containerName )
    {

    }


    @Override
    public void getBackupsForPeriod( final Date fromDate, final Date toDate )
    {

    }
}
