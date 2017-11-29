package io.subutai.core.localpeer.impl.command;


import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.Response;
import io.subutai.core.localpeer.impl.entity.ResourceHostEntity;


public class TemplateDownloadTracker implements CommandCallback
{
    private final ResourceHostEntity resourceHostEntity;
    private final String environmentId;

    private final static String TEMPLATE_EXISTS_PATTERN = "\"(\\S+) instance exist";
    private final static String TEMPLATE_IS_BEING_INSTALLED_PATTERN = "Installing template (\\S+)\"";
    private final static String TEMPLATE_IS_BEING_DOWNLOADED_PATTERN = "Downloading (.+)\"";
    private final static String TEMPLATE_DOWNLOAD_PERCENT_PATTERN = "(\\d+\\.\\d+)%";
    private final static Pattern templateExistsPattern = Pattern.compile( TEMPLATE_EXISTS_PATTERN, Pattern.MULTILINE );
    private final static Pattern templateIsBeingInstalledPattern =
            Pattern.compile( TEMPLATE_IS_BEING_INSTALLED_PATTERN, Pattern.MULTILINE );
    private final static Pattern templateIsBeingDownloadedPattern = Pattern.compile( TEMPLATE_IS_BEING_DOWNLOADED_PATTERN );
    private final static Pattern templateDownloadPercentPattern = Pattern.compile( TEMPLATE_DOWNLOAD_PERCENT_PATTERN );

    private String currentTemplate;


    public TemplateDownloadTracker( final ResourceHostEntity resourceHostEntity, final String environmentId )
    {
        this.resourceHostEntity = resourceHostEntity;
        this.environmentId = environmentId;
    }


    @Override
    public void onResponse( final Response response, final CommandResult commandResult )
    {
        if ( response != null && response.getStdOut() != null )
        {

            //download in progress, update current percentage
            StringTokenizer stringTokenizer = new StringTokenizer( commandResult.getStdOut(), "\n\r\f" );

            while ( stringTokenizer.hasMoreTokens() )
            {
                String line = stringTokenizer.nextToken();

                Matcher downloadingMatcher = templateIsBeingDownloadedPattern.matcher( line );

                if ( downloadingMatcher.groupCount() > 0 && downloadingMatcher.find() )
                {
                    currentTemplate = downloadingMatcher.group( 1 );
                }


                if ( currentTemplate != null )
                {
                    Matcher downloadPercentMatcher = templateDownloadPercentPattern.matcher( line );
                    if ( downloadPercentMatcher.groupCount() > 0 && downloadPercentMatcher.find() )
                    {
                        resourceHostEntity.updateTemplateDownloadProgress( environmentId, currentTemplate,
                                Double.valueOf( downloadPercentMatcher.group( 1 ) ).intValue() );
                    }
                }
            }


            //existing template, mark it as 100% downloaded
            Matcher templateExistsMatcher = templateExistsPattern.matcher( response.getStdOut() );
            if ( templateExistsMatcher.groupCount() > 0 )
            {
                while ( templateExistsMatcher.find() )
                {
                    resourceHostEntity
                            .updateTemplateDownloadProgress( environmentId, templateExistsMatcher.group( 1 ), 100 );
                }
            }

            Matcher installingMatcher = templateIsBeingInstalledPattern.matcher( response.getStdOut() );
            //suppose if a template is being installed that indicates it is 100% downloaded
            if ( installingMatcher.groupCount() > 0 )
            {
                while ( installingMatcher.find() )
                {
                    resourceHostEntity
                            .updateTemplateDownloadProgress( environmentId, installingMatcher.group( 1 ), 100 );
                }
            }
        }
    }
}
