package org.safehaus.subutai.core.strategy.api;


import org.safehaus.subutai.common.protocol.Criteria;


/**
 * Created by timur on 11/10/14.
 */
public class CriteriaDef extends Criteria
{
    // title for displaying in UI
    private String title;


    public CriteriaDef( final String id, final String title, final Object value )
    {
        super( id, value );
        this.title = title;
    }


    public String getTitle()
    {
        return title;
    }


    public void setTitle( final String title )
    {
        this.title = title;
    }
}
