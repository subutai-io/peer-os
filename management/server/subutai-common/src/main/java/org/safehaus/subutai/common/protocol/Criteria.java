package org.safehaus.subutai.common.protocol;


/**
 * Class for holding container placement strategy criteria.
 */
public class Criteria<T>
{
    private String id;
    // Value of criteria. Usually boolean.
    private T value;


    public Criteria( String id, /*String title, */T value )
    {
        this.id = id;
//        this.title = title;
        this.value = value;
    }


    public String getId()
    {
        return id;
    }


    public void setId( String id )
    {
        this.id = id;
    }

//
//    public String getTitle()
//    {
//        return title;
//    }
//
//
//    public void setTitle( String title )
//    {
//        this.title = title;
//    }


    public T getValue()
    {
        return value;
    }


    public void setValue( T value )
    {
        this.value = value;
    }
}
