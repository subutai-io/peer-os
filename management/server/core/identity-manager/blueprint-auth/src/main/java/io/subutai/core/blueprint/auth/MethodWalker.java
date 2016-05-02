package io.subutai.core.blueprint.auth;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;

import com.google.common.base.Strings;

import io.subutai.common.security.relation.RelationLink;
import io.subutai.common.security.relation.RelationPreCredibility;
import io.subutai.common.security.relation.Trait;
import io.subutai.core.object.relation.api.RelationManager;
import io.subutai.core.object.relation.api.RelationVerificationException;
import io.subutai.core.object.relation.api.model.RelationInfoMeta;


/**
 * Created by ape-craft on 5/2/16.
 */
public class MethodWalker
{
    private final Logger logger;
    private Object bean;
    private Class<?> beanClass;
    private Method method;
    private Object[] parameters;

    private ServiceTracker<RelationManager, RelationManager> serviceTracker;


    public MethodWalker( final Logger logger )
    {
        BundleContext ctx = FrameworkUtil.getBundle( this.getClass() ).getBundleContext();
        serviceTracker = new ServiceTracker<>( ctx, RelationManager.class, null );
        serviceTracker.open();
        this.logger = logger;
    }


    private RelationManager getRelationManager()
    {
        return serviceTracker.getService();
    }


    public void performCheck( final Object bean, final Method method, final Object[] parameters )
            throws RelationVerificationException
    {
        this.bean = bean;
        this.beanClass = bean.getClass();
        this.method = method;
        this.parameters = parameters;

        try
        {
            Method beanMethod = beanClass.getMethod( method.getName(), method.getParameterTypes() );
            RelationPreCredibility credibility = beanMethod.getAnnotation( RelationPreCredibility.class );
            Map<String, String> relationTraits = new HashMap<>();
            Trait[] traits = credibility.traits();
            for ( final Trait trait : traits )
            {
                relationTraits.put( trait.traitKey(), trait.traitValue() );
                logger.info( "{} :: {}", trait.traitKey(), trait.traitValue() );
            }

            String targetValueName = credibility.target();
            String sourceValueName = credibility.source();

            RelationLink target = getLink( targetValueName );
            RelationLink source = getLink( sourceValueName );

            RelationInfoMeta meta = new RelationInfoMeta();
            meta.setRelationTraits( relationTraits );
            getRelationManager().getRelationInfoManager().checkRelation( source, target, meta, null );
        }
        catch ( NoSuchMethodException e )
        {
            logger.warn( "Error while checking relation" );
        }
    }


    private RelationLink getLink( String varName )
    {
        try
        {
            RelationLink result = null;
            if ( !Strings.isNullOrEmpty( varName ) )
            {
                result = selectFromMethodParameters( varName );
                if ( result == null )
                {
                    result = selectFromClassFields( varName );
                }
                if ( result == null )
                {
                    result = ( RelationLink ) bean;
                }
            }
            return result;
        }
        catch ( Exception ex )
        {
            logger.error( "Error getting link" );
            return null;
        }
    }


    private RelationLink selectFromMethodParameters( String varName )
    {
        Parameter[] params = method.getParameters();
        int i = 0;
        for ( final Parameter param : params )
        {
            if ( varName.equals( param.getName() ) )
            {
                return ( RelationLink ) parameters[i];
            }
        }
        return null;
    }


    private RelationLink selectFromClassFields( String varName )
    {
        try
        {
            Field field = beanClass.getDeclaredField( varName );
            field.setAccessible( true );
            return ( RelationLink ) field.get( bean );
        }
        catch ( NoSuchFieldException | IllegalAccessException e )
        {
            logger.error( "Failed to get field with name: {} got NoSuchFieldException | IllegalAccessException",
                    varName );
        }
        return null;
    }
}
