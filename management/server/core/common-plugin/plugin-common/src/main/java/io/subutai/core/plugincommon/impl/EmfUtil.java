package io.subutai.core.plugincommon.impl;


import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;


public class EmfUtil
{
    private EntityManagerFactory emf;


    public EmfUtil()
    {
    }


    public EntityManagerFactory getEmf()
    {
        Bundle thisBundle = FrameworkUtil.getBundle( EmfUtil.class );
        BundleContext context = thisBundle.getBundleContext();
        ServiceReference[] refs = null;
        try
        {
            refs = context.getServiceReferences( EntityManagerFactory.class.getName(),
                    "(osgi.unit.name=pluginsPUnit)" );
        }
        catch ( Exception isEx )
        {
            throw new IllegalStateException( "Filter error", isEx );
        }
        if ( refs != null )
        {
            emf = ( EntityManagerFactory ) context.getService( refs[0] );
        }
        if ( emf == null )
        {


            ServiceReference serviceReference = context.getServiceReference( PersistenceProvider.class.getName() );
            PersistenceProvider persistenceProvider = ( PersistenceProvider ) context.getService( serviceReference );

            emf = persistenceProvider.createEntityManagerFactory( "pluginsPUnit", null );
        }
        return emf;
    }
}
