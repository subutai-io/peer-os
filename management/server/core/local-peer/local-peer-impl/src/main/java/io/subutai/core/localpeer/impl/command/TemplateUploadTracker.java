package io.subutai.core.localpeer.impl.command;


import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.Response;
import io.subutai.core.localpeer.impl.entity.ResourceHostEntity;


public class TemplateUploadTracker implements CommandCallback
{
    /*
     0 B / 219.41 MiB    0.00%
     0 B / 219.41 MiB    0.00%
     0 B / 219.41 MiB    0.00%
     288.00 KiB / 219.41 MiB    0.13% 7m55s
     288.00 KiB / 219.41 MiB    0.13% 7m54s
     288.00 KiB / 219.41 MiB    0.13% 7m54s
     288.00 KiB / 219.41 MiB    0.13% 7m54s

     50.88 MiB / 52.53 MiB   96.84%
     52.54 MiB / 52.53 MiB  100.00%
     */
    private final static String TEMPLATE_UPLOAD_PERCENT_PATTERN = "(\\d+\\.\\d+)%";
    private final static String TEMPLATE_UPLOAD_COMPLETE_MARKER = "Template uploaded";
    private final static Pattern templateUploadPercentPattern = Pattern.compile( TEMPLATE_UPLOAD_PERCENT_PATTERN );

    private final ResourceHostEntity resourceHostEntity;

    private final String templateName;


    public TemplateUploadTracker( final ResourceHostEntity resourceHostEntity, final String templateName )
    {
        this.resourceHostEntity = resourceHostEntity;
        this.templateName = templateName;
    }


    @Override
    public void onResponse( final Response response, final CommandResult commandResult )
    {
        StringTokenizer stringTokenizer = new StringTokenizer( commandResult.getStdOut(), "\n\r\f" );

        while ( stringTokenizer.hasMoreTokens() )
        {
            String line = stringTokenizer.nextToken();

            Matcher uploadPercentMatcher = templateUploadPercentPattern.matcher( line );
            if ( uploadPercentMatcher.groupCount() > 0 && uploadPercentMatcher.find() )
            {
                resourceHostEntity.updateTemplateUploadProgress( templateName,
                        Double.valueOf( uploadPercentMatcher.group( 1 ) ).intValue() );
            }

            if ( line.contains( TEMPLATE_UPLOAD_COMPLETE_MARKER ) )
            {
                resourceHostEntity.updateTemplateUploadProgress( templateName, 100 );
            }
        }
    }
}
