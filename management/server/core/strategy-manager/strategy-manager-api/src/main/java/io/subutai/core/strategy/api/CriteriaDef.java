package io.subutai.core.strategy.api;


import io.subutai.common.protocol.Criteria;


public class CriteriaDef<T> extends Criteria<T>
{
    // title for displaying in UI
    private String title;


    public CriteriaDef( final String id, final String title, final T value )
    {
        super( id, value );
        this.title = title;
    }


    public String getTitle()
    {
        return title;
    }
}
