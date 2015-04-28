package org.safehaus.subutai.core.template.wizard.impl;


import java.util.List;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.core.template.wizard.api.exception.TemplateWizardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


/**
 * manages products installation
 */
public class ProductsInstallationProcedure extends AbstractPhaseLifecycle
{
    private static final Logger LOGGER = LoggerFactory.getLogger( ProductsInstallationProcedure.class );
    private List<String> products = Lists.newArrayList();
    private Host targetHost;
    private CommandUtil commandUtil = new CommandUtil();


    public ProductsInstallationProcedure( final List<String> products, Host targetHost )
    {
        Preconditions.checkNotNull( products, "Template products cannot be null." );
        Preconditions.checkNotNull( targetHost, "Invalid argument targetHost" );
        this.products = products;
        this.targetHost = targetHost;
    }


    @Override
    protected void doStart() throws TemplateWizardException
    {
        if ( isStopped() )
        {
            start();
            installProducts();
            stop();
        }
    }


    private void installProducts() throws TemplateWizardException
    {
        List<ProductInstallationProcess> installationProcessList = Lists.newArrayList();
        for ( final String product : products )
        {
            ProductInstallationProcess installationProcess = new ProductInstallationProcess( product );
            installationProcessList.add( installationProcess );
        }
        for ( final ProductInstallationProcess installationProcess : installationProcessList )
        {
            installationProcess.doStart();
            LOGGER.debug( installationProcess.getCommandResult().getStdOut() );
        }
    }


    @Override
    public String toString()
    {
        return "ProductsInstallationProcedure{" +
                "products=" + products +
                ", targetHost=" + targetHost +
                ", commandUtil=" + commandUtil +
                '}';
    }


    public class ProductInstallationProcess extends AbstractPhaseLifecycle
    {
        private String productName;
        private CommandResult commandResult;


        public ProductInstallationProcess( final String productName )
        {
            Preconditions.checkNotNull( productName, "Invalid argument productName." );
            this.productName = productName;
        }


        @Override
        protected void doStart() throws TemplateWizardException
        {
            if ( isStopped() )
            {
                start();
                RequestBuilder requestBuilder =
                        new RequestBuilder( String.format( "apt-get --force-yes --yes install -f %s", productName ) );
                requestBuilder.withTimeout( 90 );
                try
                {
                    commandResult = targetHost.execute( requestBuilder );
                }
                catch ( CommandException e )
                {
                    throw new TemplateWizardException( e );
                }
                stop();
            }
        }


        public CommandResult getCommandResult()
        {
            return commandResult;
        }


        @Override
        public String toString()
        {
            return "ProductInstallationProcess{" +
                    "productName='" + productName +
                    '}';
        }
    }
}
