package io.subutai.common.serialize;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.ArrayUtils;


public class ObjectSerializer implements Serializable
{

    private static final Logger LOG = LoggerFactory.getLogger( ObjectSerializer.class.getName() );


    /**
     * Returns true for all other Objects (is last in chain of responsibility)
     *
     * @param clazz any type
     *
     * @return always true
     */
    @Override
    public boolean isResponsible( Class<?> clazz )
    {
        return true;
    }


    /**
     * Converts any given object to a xml-fragment-string, which is further
     * converted to a binary representation.
     *
     * @param o any object
     *
     * @return a binary representation of the xml-fragment
     */
    @Override
    public byte[] serialize( Object o )
    {
        try
        {

            JAXBContext context = JAXBContext.newInstance( o.getClass() );
            Marshaller m = context.createMarshaller();
            m.setProperty( Marshaller.JAXB_FRAGMENT, Boolean.TRUE );

            // comment this to save space and reduce readability
            m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            m.marshal( o, stream );
            return stream.toByteArray();
        }
        catch ( JAXBException e )
        {
            LOG.warn( e.getMessage() );
        }

        return ArrayUtils.EMPTY_BYTE_ARRAY;
    }


    /**
     * Deserializes binary data representing a xml-fragment to an object of
     * given class.
     *
     * @param data the binary data, representing a xml-fragment
     * @param clazz the class of the resulting object
     *
     * @return the deserialized object
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public <T> T deserialize( byte[] data, Class<T> clazz )
    {
        try
        {

            JAXBContext context = JAXBContext.newInstance( clazz );
            Unmarshaller m = context.createUnmarshaller();
            Object o = m.unmarshal( new ByteArrayInputStream( data ) );
            return ( T ) o;
        }
        catch ( JAXBException e )
        {
            LOG.warn( e.getMessage() );
        }

        return null;
    }
}
