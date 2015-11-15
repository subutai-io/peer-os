package io.subutai.common.serialize;


public class ByteSerializer implements Serializable {

    /**
     * The ByteSerializer can handle byte-arrays.
     * @param clazz which kind of objects should be de/serialized
     * @return true for byte-arrays
     */
    @Override
    public boolean isResponsible(Class<?> clazz) {
        return clazz == byte[].class;
    }

    /**
     * For conversion from byte-array to byte-array, there is nothing to do.
     * @param o has to be binary data
     * @return just returns the input
     */
    @Override
    public byte[] serialize(Object o) {
        return (byte[]) o;
    }

    /**
     * For conversion from byte-array to byte-array, there is nothing to do.
     * @param data the binary data
     * @param clazz the class of the resulting object
     * @return the deserialized object
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return (T) data;
    }

}
