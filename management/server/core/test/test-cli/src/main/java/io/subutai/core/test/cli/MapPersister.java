package io.subutai.core.test.cli;


import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;


/**
 * Serialize/deserialize map of string keys and values
 */
public class MapPersister
{
    public void serialize( Map<String, Object> map, String filePath ) throws FileNotFoundException
    {
        XMLEncoder xmlEncoder = new XMLEncoder( new FileOutputStream( filePath ) );
        xmlEncoder.writeObject( map );
        xmlEncoder.flush();
    }


    public Map<String, Object> deserialize( String filePath ) throws FileNotFoundException
    {
        XMLDecoder xmlDecoder = new XMLDecoder( new FileInputStream( filePath ) );
        return ( Map<String, Object> ) xmlDecoder.readObject();
    }
}
