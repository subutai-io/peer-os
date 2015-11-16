package io.subutai.common.gson.required;


import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;


public class RequiredDeserializer<T> implements JsonDeserializer<T>
{
    private static final Logger LOG = LoggerFactory.getLogger( RequiredDeserializer.class );

    @Override
    public T deserialize( final JsonElement jsonElement, final Type type,
                          final JsonDeserializationContext jsonDeserializationContext ) throws JsonParseException
    {
        T object = new Gson().fromJson( jsonElement, type );

        Field[] fields = object.getClass().getDeclaredFields();

        for( Field field : fields )
        {
            if( field.getAnnotation( GsonRequired.class ) != null )
            {
                try
                {
                    field.setAccessible(true);
                    if( field.get( object ) == null )
                    {
                        throw new JsonParseException( "Missing Json field: " + field.getName() );
                    }
                }
                catch ( IllegalAccessException | IllegalArgumentException e )
                {
                    LOG.error( "Deserialization error", e );
                }
            }

        }

        return object;
    }
}
