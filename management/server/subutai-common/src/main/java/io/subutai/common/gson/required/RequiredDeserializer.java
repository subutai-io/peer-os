package io.subutai.common.gson.required;


import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.NodeGroup;


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
            validate( field, object );
        }

        return object;
    }


    private void validate( Field field, T object )
    {
        if( field.getAnnotation( GsonRequired.class ) != null )
        {
            try
            {
                field.setAccessible(true);

                switch ( field.getAnnotation( GsonRequired.class ).validation() )
                {
                    case GREATER_THAN_ZERO:
                        try
                        {
                            if( field.get( object ) == null && Integer.parseInt( ( String ) field.get( object ) ) <= 0 )
                            {
                                throw new JsonParseException( "Json validation failed expected x > field: " + field.getName() );
                            }
                        }
                        catch ( Exception e )
                        {
                            throw new JsonParseException( "Json parse error, expected int for the field: " + field.getName() );
                        }

                        break;
                    default:

                        if( field.get( object ) == null ||
                                field.get( object ) instanceof Collection<?> &&
                                        ( ( Collection ) field.get( object ) ).size() == 0 )
                        {
                            throw new JsonParseException( "Missing Json field: " + field.getName() );
                        }
                        if( field.get( object ) instanceof Collection<?> )
                        {
                            Gson gson = RequiredDeserializer.createValidatingGson();

                            Collection<?> iterable = ( Collection ) field.get( object );

                            gson.fromJson( new Gson().toJson( field.get( object ) ), new TypeToken<Collection<?>>() {}.getType() );
                        }
                }
            }
            catch ( IllegalAccessException | IllegalArgumentException e )
            {
                LOG.error( "Deserialization error", e );
            }
        }
    }

    public static Gson createValidatingGson()
    {
        return new GsonBuilder()
                .registerTypeAdapter( NodeGroup.class, new RequiredDeserializer<NodeGroup>() )
                .registerTypeAdapter( Blueprint.class, new RequiredDeserializer<Blueprint>() )
                .setPrettyPrinting().create();
    }
}
