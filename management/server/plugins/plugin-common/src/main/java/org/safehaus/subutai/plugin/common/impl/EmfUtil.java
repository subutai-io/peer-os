package org.safehaus.subutai.plugin.common.impl;


import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;


/**
 * Created by talas on 12/9/14.
 */
public class EmfUtil
{
    private EntityManagerFactory emf;


    public EmfUtil()
    {
        //        getEmf();
    }


    public EntityManagerFactory getEmf()
    {
        //        if ( emf == null )
        //        {
        //            Bundle thisBundle = FrameworkUtil.getBundle( EmfUtil.class );
        //            // Could get this by wiring up OsgiTestBundleActivator as well.
        //            BundleContext context = thisBundle.getBundleContext();
        //
        //            ServiceReference serviceReference = context.getServiceReference( PersistenceProvider.class
        // .getName() );
        //            PersistenceProvider persistenceProvider = (PersistenceProvider ) context.getService(
        // serviceReference );
        //
        //            emf = persistenceProvider.createEntityManagerFactory( "pluginsUnit", null );
        //        }
        Bundle thisBundle = FrameworkUtil.getBundle( EmfUtil.class );
        // Could get this by wiring up OsgiTestBundleActivator as well.
        BundleContext context = thisBundle.getBundleContext();
        ServiceReference[] refs = null;
        try
        {
            refs = context.getServiceReferences( EntityManagerFactory.class.getName(), "(osgi.unit.name=pluginsUnit)" );
        }
        catch ( Exception isEx )
        {
            throw new RuntimeException( "Filter error", isEx );
        }
        if ( refs != null )
        {
            emf = ( EntityManagerFactory ) context.getService( refs[0] );
        }
        if ( emf == null )
        {


            ServiceReference serviceReference = context.getServiceReference( PersistenceProvider.class.getName() );
            PersistenceProvider persistenceProvider = ( PersistenceProvider ) context.getService( serviceReference );

            emf = persistenceProvider.createEntityManagerFactory( "pluginsUnit", null );
        }
        return emf;
    }
}
