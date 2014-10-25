package org.safehaus.subutai.core.peer.rest;


import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import org.safehaus.subutai.common.util.JsonUtil;


/**
 * Created by timur on 10/24/14.
 */


public class SubutaiParamConvertProvider implements ParamConverterProvider
{
    @Override
    public <T> ParamConverter<T> getConverter( final Class<T> rawType, final Type type, final Annotation[] annotations )
    {
        return new ParamConverter<T>()
        {

            @Override
            public T fromString( final String value )
            {
                    return JsonUtil.fromJson( value, rawType );
            }


            @Override
            public String toString( final T value )
            {
                return JsonUtil.toJson( value );
            }
        };
    }
}
