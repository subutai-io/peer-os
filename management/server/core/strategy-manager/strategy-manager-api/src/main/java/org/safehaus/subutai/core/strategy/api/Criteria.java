package org.safehaus.subutai.core.strategy.api;


/**
 * Class for holding container placement strategy criteria.
 */
public class Criteria {
    private String id;
    // title for displaying in UI
    private String title;
    // Value of criteria. Usually boolean.
    private Object value;


    public Criteria( String id, String title, Object value )
    {
        this.id = id;
        this.title = title;
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


    public String getTitle()
    {
        return title;
    }


    public void setTitle( String title )
    {
        this.title = title;
    }


    public Object getValue()
    {
        return value;
    }


    public void setValue( Object value )
    {
        this.value = value;
    }
}
