package io.subutai.common.util;


import org.junit.Test;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


/**
 *
 */
public class StringUtilTest
{
    private String htmlString = "<i>111& 2222<bss>asdasda-_.?</bss><script>Test & Test</script>";
    private String nonHtmlString = "Test1 Test2";


    @Test
    public void testEscapeHtml()
    {
        String result = StringUtil.escapeHtml( htmlString );

        assertFalse (result.contains( "<" ));
        assertFalse (result.contains( ">" ));
        assertTrue (result.contains( "&lt;" ));
    }


    @Test
    public void testUnEscapeHtml()
    {
        String result = StringUtil.escapeHtml( htmlString );
        result = StringUtil.unEscapeHtml( result );

        assertTrue (result.contains( "<" ));
        assertFalse (result.contains( "&lt;" ));
    }


    @Test
    public void testRemoveHtml()
    {
        String result = StringUtil.removeHtml( htmlString );

        //System.out.println( htmlString );
        //System.out.println( result );

        assertFalse (result.contains( ">" ));
        assertFalse (result.contains( "<" ));
    }


    @Test
    public void testIsContainsHtml()
    {
        assertTrue(StringUtil.containsHtml( htmlString));
        assertFalse(StringUtil.containsHtml( nonHtmlString)) ;
    }


    @Test
    public void testRemoveSpecialChars()
    {
        String resultSpace = StringUtil.removeSpecialChars ( htmlString, false );
        String resultNoSpace = StringUtil.removeSpecialChars ( htmlString, true );

        assertFalse (resultSpace.contains( "<" ));
        assertTrue (resultSpace.contains( "script" ));

        assertTrue (resultSpace.contains( " " ));
        assertFalse (resultNoSpace.contains( " " ));
    }


    @Test
    public void testRemoveHtmlAndSpecialChars()
    {
        String result = StringUtil.removeHtmlAndSpecialChars( htmlString, true );

        assertFalse (result.contains( "<" ));
        assertFalse (result.contains( "script" ));
    }


    @Test
    public void testIsValidEmail()
    {
        assertTrue( StringUtil.isValidEmail( "valid@email.com" ) );
        assertFalse( StringUtil.isValidEmail( "invalidemail@com" ) );
        assertFalse( StringUtil.isValidEmail( null ) );
        assertFalse( StringUtil.isValidEmail( "" ) );
    }

}
