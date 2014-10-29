package org.safehaus.subutai.core.messenger.cli;


import java.io.Serializable;

import com.google.common.base.Objects;


/**
 * Test shared object
 */
public class CustomObject implements Serializable
{
    private int num;
    private String str;


    public CustomObject( final int num, final String str )
    {
        this.num = num;
        this.str = str;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "num", num ).add( "str", str ).toString();
    }
}
