package com.mycompany.app;

/**
 * Created with IntelliJ IDEA.
 * User: skardan
 * Date: 9/19/13
 * Time: 12:54 PM
 * To change this template use File | Settings | File Templates.
 */
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DictionaryTest extends TestCase{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DictionaryTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( DictionaryTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

}
