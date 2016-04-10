package io.subutai.core.hubmanager.impl.appscale;


class Commands
{
    static String getAppScaleStartCommand ()
    {
        return ( "sudo /root/up.sh" );
    }


    static String getAppScaleStopCommand ()
    {
        return ( "sudo /root/appscale-tools/bin/appscale down" );
    }


    static String getAppscaleInit ()
    {
        return ( "appscale init cluster" );
    }


    static String getRemoveSubutaiList ()
    {
        return ( "rm -f /etc/apt/sources.list.d/subutai-repo.list" );
    }


    static String getCreateLogDir ()
    {
        return ( "mkdir -p /var/log/appscale" );
    }


    static String getRunShell ()
    {
        return ( "sudo /root/run.sh" );
    }


    static String getPsAUX ()
    {
        return ( "cat /AppScalefile" );
    }


    static String getChangeHostHame ()
    {
        return ( "echo 'domain.com' > /etc/hostname" );
    }


    static String backUpSSH ()
    {
        return ( "cp -rf /root/.ssh /root/sshBACK" );
    }


    static String backUpAppscale ()
    {
        return ( "cp -rf /root/.appscale /root/appBACK" );
    }


    static String revertBackUpSSH ()
    {
        return ( "mv /root/sshBACK/* /root/.ssh" );
    }


    static String revertBackupAppscale ()
    {
        return ( "mv /root/appBACK/* /root/.appscale" );
    }


}

