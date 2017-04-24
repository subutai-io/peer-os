package io.subutai.core.test.cli;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Test
{

    @org.junit.Test
    public void name() throws Exception
    {
        String TEMPLATE_IS_BEING_DOWNLOADED_PATTERN = "Downloading (.+)\"";
        Pattern templateIsBeingDownloadedPattern = Pattern.compile( TEMPLATE_IS_BEING_DOWNLOADED_PATTERN );
        String line = "time=\"2016-08-19 13:34:58\" level=info msg=\"Downloading cassandra\"";

        Matcher downloadingMatcher = templateIsBeingDownloadedPattern.matcher( line );

        if ( downloadingMatcher.groupCount() > 0 && downloadingMatcher.find() )
        {
            System.out.println(downloadingMatcher.group( 1 ));
        }
    }
}
