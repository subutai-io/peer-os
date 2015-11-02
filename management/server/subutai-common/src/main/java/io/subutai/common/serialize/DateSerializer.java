package io.subutai.common.serialize;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateSerializer extends XmlAdapter<String, Date> implements Serializable {

    /**
     * The DateSerializer can handle instances of the Date class.
     * @param clazz which kind of objects should be de/serialized
     * @return true for Date class
     */
    @Override
    public boolean isResponsible(Class<?> clazz) {
        return clazz == Date.class;
    }

    /**
     * Converts from Date-object to byte-array.
     * @param o the Date-object
     * @return a binary representation
     */
    @Override
    public byte[] serialize(Object o) {
        try {
            return this.marshal((Date) o).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converts from byte-array to Date-object.
     * @param data the binary data
     * @param clazz the class of the resulting object
     * @return the Date-object
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try {
            return (T) this.unmarshal(new String(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converts from Date-object to String.
     * @param date an Date-object
     * @return the String representation of the input
     */
    @Override
    public String marshal(Date date) throws Exception {
        return date.getTime() + "";
    }

    /**
     * Converts from String to Date-object.
     * @param string an String
     * @return the Date representation of the input
     */
    @Override
    public Date unmarshal(String string) throws Exception {
        return new Date(Long.parseLong(string));
    }

}
