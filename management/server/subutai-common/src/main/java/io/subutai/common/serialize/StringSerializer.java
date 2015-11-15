package io.subutai.common.serialize;


public class StringSerializer implements Serializable {

    /**
     * The StringSerializer can handle Strings.
     * @param clazz which kind of objects should be de/serialized
     * @return true for String
     */
    @Override
    public boolean isResponsible(Class<?> clazz) {
        return clazz == String.class;
    }

    /**
     * Converts a String to a byte-array.
     * @param o the string object
     * @return the byte representation of the given string 
     */
    @Override
    public byte[] serialize(Object o) {
        return ((String) o).getBytes();
    }

    /**
     * Converts a byte-array to a String.
     * @param data the byte-array to derserialize
     * @param clazz the class of the resulting object
     * @return the deserialized String
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return (T) new String(data);
    }

}
