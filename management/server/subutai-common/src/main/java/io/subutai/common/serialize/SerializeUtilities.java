package io.subutai.common.serialize;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.codec.binary.Base64;

public class SerializeUtilities {

    /**
     * Reads a file and deserializes it to an object of given class. 
     * @param path where the file is located
     * @param clazz type of the resulting object
     * @return an object of given class
     */
    public static <T> T deserializeFile(String path, Class<T> clazz){
        try {

            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller m = context.createUnmarshaller();
            Object o = m.unmarshal(new FileInputStream(path));
            return clazz.cast(o);

        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Serializes an object and saves it to a file, located at given path.
     * @param path where the file should be saved
     * @param o the object, that should be saved
     */
    public static void serializeFile(String path, Object o){
        try {

            JAXBContext context = JAXBContext.newInstance(o.getClass());
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            FileOutputStream stream = new FileOutputStream(path);
            m.marshal(o, stream);

        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
    }

    /**
     * Converts a Base64-String to binary.
     * @param input a Base64-String
     * @return decoded binary data
     */
    public static byte[] fromBase64(String input){
        return new Base64().decode(input); 
    }

    /**
     * Converts binary data to a Base64-String.
     * @param input the binary data
     * @return the encoded Base64-String
     */
    public static String toBase64(byte[] input){
        return new Base64().encodeToString(input);
    }

}
