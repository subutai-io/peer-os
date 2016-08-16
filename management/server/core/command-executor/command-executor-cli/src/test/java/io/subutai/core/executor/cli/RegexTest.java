package io.subutai.core.executor.cli;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;



public class RegexTest
{
    @Test
    public void name() throws Exception
    {
        String PATTERN_STRING = "(\\d+\\.\\d+)%";
        final Pattern pattern = Pattern.compile( PATTERN_STRING );

        Matcher m = pattern.matcher( " 20.44 MB / 23.33 MB   87.61% 0" );

        if(m.groupCount() > 0 && m.find()){
            System.out.printf( Double.valueOf( m.group(1)).intValue() + "" );
        }
    }
}
