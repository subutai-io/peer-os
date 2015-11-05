package io.subutai.common.serialize;

public interface Serializable {

    /**
     * Returns true if the implementing Serializer can serialize and deserialize
     * objects of given class.
     * @param clazz which kind of objects should be de/serialized
     * @return true if the implementation can handle objects of given class
     */
    public boolean isResponsible(Class<?> clazz);
    
    /**
     * Serializes an given object to a binary representation.
     * @param o an object
     * @return a binary representation
     */
    public byte[] serialize(Object o);
    
    /**
     * Deserializes binary data to an object of given class.
     * @param data the binary data
     * @param clazz the class of the resulting object
     * @return the deserialized object
     */
    public <T> T deserialize(byte[] data, Class<T> clazz);

}
