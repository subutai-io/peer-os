/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.subutai.core.blueprint.auth;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.security.auth.Subject;

import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.aries.blueprint.Interceptor;

import io.subutai.common.security.relation.RelationCredibility;


public class AuthorizationInterceptor implements Interceptor
{
    private static final Logger logger = LoggerFactory.getLogger( AuthorizationInterceptor.class );
    private Class<?> beanClass;
    private Object bean;
    private MethodWalker methodWalker;


    public AuthorizationInterceptor( Object bean )
    {
        this.bean = bean;
        this.beanClass = bean.getClass();
        this.methodWalker = new MethodWalker( logger );
    }


    public int getRank()
    {
        return 0;
    }


    public void postCallWithException( ComponentMetadata cm, Method m, Throwable ex, Object preCallToken )
    {
    }


    public void postCallWithReturn( ComponentMetadata cm, Method method, Object returnType, Object preCallToken )
            throws Exception
    {
        Annotation ann = new SecurityAnnotationParser().getEffectiveAnnotation( beanClass, method );
        if ( ann instanceof RelationCredibility )
        {
            try
            {
                methodWalker.performCheck( bean, method, returnType );
            }
            catch ( Exception ex )
            {
                String msg = "Sorry you don't have sufficient relations for this operation for details see logs.";
                throw new SecurityException( msg, ex );
            }
        }
    }


    public Object preCall( ComponentMetadata cm, Method method, Object... parameters ) throws Throwable
    {
        Annotation ann = new SecurityAnnotationParser().getEffectiveAnnotation( beanClass, method );
        if ( ann == null )
        {
            return null;
        }

        if ( ann instanceof PermitAll )
        {
            return null;
        }
        String[] rolesAr = new String[] {}; // Also applies for @DenyAll
        if ( ann instanceof RolesAllowed )
        {
            rolesAr = ( ( RolesAllowed ) ann ).value();
        }

        if ( ann instanceof RelationCredibility )
        {
            try
            {
                methodWalker.performCheck( bean, method, parameters );
                return null;
            }
            catch ( Exception ex )
            {
                String msg = "Sorry you don't have sufficient relations for this operation for details see logs.";
                throw new SecurityException( msg, ex );
            }
        }

        Set<String> roles = new HashSet<>( Arrays.asList( rolesAr ) );
        AccessControlContext acc = AccessController.getContext();
        Subject subject = Subject.getSubject( acc );
        if ( subject == null )
        {
            throw new AccessControlException( "Method call " + method.getDeclaringClass() + "." + method.getName()
                    + " denied. No JAAS login present" );
        }
        Set<Principal> principals = subject.getPrincipals();

        for ( Principal principal : principals )
        {
            if ( roles.contains( principal.getName() ) )
            {
                logger.debug( "Granting access to Method: {} for {}.", method, principal );
                return null;
            }
        }
        String msg = String.format( "Method call %s.%s denied. Roles allowed are %s. Your principals are %s.",
                method.getDeclaringClass(), method.getName(), roles, getNames( principals ) );
        throw new AccessControlException( msg );
    }


    private String getNames( Set<Principal> principals )
    {
        StringBuilder sb = new StringBuilder();
        for ( Principal principal : principals )
        {
            sb.append( principal.getName() ).append( " " );
        }
        return sb.toString();
    }
}
