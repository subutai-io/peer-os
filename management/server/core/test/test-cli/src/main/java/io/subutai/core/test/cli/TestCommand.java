package io.subutai.core.test.cli;


import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.bundle.core.BundleStateService;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "test", name = "do", description = "test command" )
public class TestCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( TestCommand.class.getName() );


    @Override
    protected Object doExecute()
    {

        BundleContext ctx = FrameworkUtil.getBundle( TestCommand.class ).getBundleContext();

        Bundle[] bundles = ctx.getBundles();

        BundleStateService bundleStateService = ServiceLocator.getServiceNoCache( BundleStateService.class );


        for ( Bundle bundle : bundles )
        {
            System.out.println(bundle.getSymbolicName() + " : "+ bundleStateService.getState( bundle )  + " : " + bundle.getState());
        }


        return null;
    }
}
