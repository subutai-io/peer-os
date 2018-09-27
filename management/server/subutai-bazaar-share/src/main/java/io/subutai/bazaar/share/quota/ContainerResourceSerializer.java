package io.subutai.bazaar.share.quota;


import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


public class ContainerResourceSerializer extends StdSerializer<ContainerResource>
{

    public ContainerResourceSerializer()
    {
        this( null );
    }


    public ContainerResourceSerializer( Class<ContainerResource> t )
    {
        super( t );
    }


    @Override
    public void serialize( final ContainerResource containerResource, final JsonGenerator jsonGenerator,
                           final SerializerProvider serializerProvider ) throws IOException
    {
        jsonGenerator.writeStringField( "value", containerResource.getPrintValue() );
    }


    @Override
    public void serializeWithType( final ContainerResource value, final JsonGenerator gen,
                                   final SerializerProvider serializers, final TypeSerializer typeSer )
            throws IOException
    {
        typeSer.writeTypePrefixForObject( value, gen );
        serialize( value, gen, serializers );
        typeSer.writeTypeSuffixForObject( value, gen );
    }
}
