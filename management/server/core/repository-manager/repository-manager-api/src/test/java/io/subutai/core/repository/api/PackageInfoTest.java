package io.subutai.core.repository.api;


import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;

import io.subutai.core.repository.api.PackageInfo;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


public class PackageInfoTest
{
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";


    PackageInfo packageInfo = new PackageInfo( NAME, DESCRIPTION );


    @Test
    public void testConstructor() throws Exception
    {

        try
        {
            new PackageInfo( null, DESCRIPTION );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {
            new PackageInfo( NAME, null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( NAME, packageInfo.getName() );
        assertEquals( DESCRIPTION, packageInfo.getDescription() );
    }


    @Test
    public void testToString() throws Exception
    {

        assertThat( packageInfo.toString(), containsString( NAME ) );
        assertThat( packageInfo.toString(), containsString( DESCRIPTION ) );
    }


    @Test
    public void testEqualsNHashCode() throws Exception
    {
        Map<PackageInfo, PackageInfo> map = Maps.newHashMap();

        map.put( packageInfo, packageInfo );

        PackageInfo expected = new PackageInfo( NAME, DESCRIPTION );


        assertEquals( expected, map.get( expected ) );
        assertEquals( packageInfo, packageInfo );
        assertNotEquals( packageInfo, this );
        assertNotEquals( new PackageInfo( "", DESCRIPTION ), packageInfo );
        assertNotEquals( new PackageInfo( NAME, "" ), packageInfo );
    }
}
