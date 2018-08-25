package io.subutai.core.blueprint.auth;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;

import org.apache.commons.collections.Unmodifiable;

import com.google.common.base.Strings;

import io.subutai.common.security.relation.RelationCredibility;
import io.subutai.common.security.relation.RelationInfoManager;
import io.subutai.common.security.relation.RelationLink;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.RelationVerificationException;
import io.subutai.common.security.relation.Trait;
import io.subutai.common.security.relation.model.RelationInfoMeta;


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


    public void performCheck( final Object bean, final Method method, final Object returnObject )
            throws RelationVerificationException, NoSuchMethodException
    {
        this.bean = bean;
        this.beanClass = bean.getClass();
        this.method = method;
        this.parameters = parameters;

        if ( returnObject == null )
        {
            return;
        }

        Method beanMethod = beanClass.getMethod( method.getName(), method.getParameterTypes() );
        RelationCredibility credibility = beanMethod.getAnnotation( RelationCredibility.class );
        String sourceValueName = credibility.source();
        String targetValueName = credibility.target();

        if ( !"return".equals( targetValueName ) )
        {
            return;
        }

        RelationLink source = getLink( sourceValueName );

        if ( Collection.class.isAssignableFrom( returnObject.getClass() ) )
        {
            boolean allowed = true;
            Collection collection = ( Collection<?> ) returnObject;
            for ( final Object item : collection )
            {
                if ( !( item instanceof RelationLink ) )
                {
                    allowed = false;
                    break;
                }
            }
            if ( allowed )
            {
                if ( Unmodifiable.class.isAssignableFrom( returnObject.getClass() ) )
                {
                    //todo return new unmodifiable collection but with filtered content
                    throw new RelationVerificationException(
                            "Cannot filter items, correct return type or remove annotation." );
                }
                else
                {
                    for ( Iterator it = collection.iterator(); it.hasNext(); )
                    {
                        RelationLink target = ( RelationLink ) it.next();
                        try
                        {
                            check( source, target, credibility.traits() );
                        }
                        catch ( RelationVerificationException ex )
                        {
                            it.remove();
                        }
                    }
                }
            }
            else
            {
                throw new RelationVerificationException(
                        "Cannot filter items, correct return type or remove annotation." );
            }
        }
        else if ( returnObject instanceof RelationLink )
        {
            check( source, ( RelationLink ) returnObject, credibility.traits() );
        }
        else
        {
            throw new RelationVerificationException( "Cannot filter items, correct return type or remove annotation." );
        }
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
            RelationCredibility credibility = beanMethod.getDeclaredAnnotation( RelationCredibility.class );

            String targetValueName = credibility.target();
            String sourceValueName = credibility.source();

            if ( "return".equals( targetValueName ) )
            {
                return;
            }

            RelationLink target = getLink( targetValueName );
            RelationLink source = getLink( sourceValueName );

            check( source, target, credibility.traits() );
        }
        catch ( NoSuchMethodException e )
        {
            // SWALLOW
            logger.warn( "Error while checking relation" );
        }
    }


    private void check( RelationLink source, RelationLink target, Trait[] traits ) throws RelationVerificationException
    {
        Map<String, String> relationTraits = new HashMap<>();
        for ( final Trait trait : traits )
        {
            relationTraits.put( trait.key(), trait.value() );
        }
        RelationInfoMeta meta = new RelationInfoMeta();
        meta.setRelationTraits( relationTraits );
        RelationInfoManager relationInfoManager = getRelationManager().getRelationInfoManager();
        if ( source == null )
        {
            relationInfoManager.checkRelation( target, meta, null );
        }
        else
        {
            relationInfoManager.checkRelation( source, target, meta, null );
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
            // SWALLOW
            logger.error( "Error getting link: {}", ex.getMessage() );
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
            // SWALLOW ?
            logger.error( "Failed to get field with name {}: {}", varName, e.getMessage() );
        }
        return null;
    }
}
