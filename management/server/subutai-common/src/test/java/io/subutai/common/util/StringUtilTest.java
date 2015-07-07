package io.subutai.common.util;


import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import io.subutai.common.util.StringUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class StringUtilTest
{

    @Test
    public void testTrimToSize() throws Exception
    {
        assertNotNull( StringUtil.trimToSize( "test", 3 ) );
        assertNull( StringUtil.trimToSize( null, 5 ) );
    }


    @Test
    public void testSplitString() throws Exception
    {
        assertNotNull( StringUtil.splitString( "test,sdf sdf.sdg", "," ) );
    }


    @Test
    public void testCountNumberOfOccurrences() throws Exception
    {
        StringUtil.countNumberOfOccurrences( "test", "2" );
    }


    @Test
    public void testIsStringNullOrEmpty() throws Exception
    {
        assertFalse( StringUtil.isStringNullOrEmpty( "test" ) );
    }


    @Test
    public void testIsNumeric() throws Exception
    {
        assertTrue( StringUtil.isNumeric( "5.5" ) );
        assertFalse( StringUtil.isNumeric( "test" ) );
    }


    @Test
    public void testAreStringsEqual() throws Exception
    {
        assertTrue( StringUtil.areStringsEqual( "test", "test" ) );
    }


    @Test
    public void testJoinStrings() throws Exception
    {
        Set<String> mySet = new HashSet<>();
        mySet.add( "test\"" );
        mySet.add( "asd" );

        assertNotNull( StringUtil.joinStrings( mySet, '*', true ) );
    }


    @Test
    public void testGetLen() throws Exception
    {
        assertNotNull( StringUtil.getLen( "test" ) );
        assertEquals(0, StringUtil.getLen( null ) );
    }
}