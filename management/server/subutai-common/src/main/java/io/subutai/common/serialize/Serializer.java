package io.subutai.common.serialize;

import java.util.ArrayList;
import java.util.List;

public class Serializer implements Serializable {

    private static Serializer instance;

    private List<Serializable> serializers = new ArrayList<Serializable>();

    /**
     * Registers Serializers responsible for different types. 
     */
    private Serializer() {
        serializers.add(new ByteSerializer());
        serializers.add(new StringSerializer());
        serializers.add(new DateSerializer());
        serializers.add(new ObjectSerializer());
    }

    /**
     * Creates or returns the Serializer-Singleton.
     * @return the Serializer-Singleton
     */
    public static Serializer getInstance() {
        if (instance == null)
            instance = new Serializer();

        return instance;
    }

    /**
     * Serializes an given object to a binary representation by delegating the
     * request to the responsible Serializer.
     * @param o an object
     * @return a binary representation
     */
    @Override
    public byte[] serialize(Object o) {
        Serializable serializer = findResponsible(o.getClass());
        return serializer.serialize(o);
    }

    /**
     * Deserializes binary data to an object of given class by delegating the
     * request to the responsible Serializer.
     * @param data the binary data
     * @param clazz the class of the resulting object
     * @return the deserialized object
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        Serializable serializer = findResponsible(clazz);
        return serializer.deserialize(data, clazz);
    }

    /**
     * Searches the registered Serializers for one, that is responsible for the 
     * given class.
     * @param clazz class of objects, that can be de/serialized with 
     *        returned Serializer 
     * @return a Serializer that can handle objects of given class
     */
    private Serializable findResponsible(Class<?> clazz) {
        for (Serializable serializer : serializers)
            if (serializer.isResponsible(clazz))
                return serializer;
        return null;
    }

    /**
     * The Serializer-Singleton can handle all given objects or will throw an 
     * exception.
     * @param clazz which kind of objects should be de/serialized
     * @return always true
     */
    @Override
    public boolean isResponsible(Class<?> clazz) {
        return true;
    }

}
