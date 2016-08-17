package io.subutai.core.executor.cli;


import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.command.Response;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "command", name = "template-test" )
public class TemplateTestCommand extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( TemplateTestCommand.class.getName() );

    private final CommandExecutor executor;

    @Argument( index = 0, name = "host id", required = true )
    String hostId;
    @Argument( index = 1, name = "template", required = true )
    String template;


    public TemplateTestCommand( final CommandExecutor executor )
    {
        Preconditions.checkNotNull( executor );

        this.executor = executor;
    }


    @Override
    protected Object doExecute() throws CommandException
    {

        final String TEMPLATE_EXISTS_PATTERN = "\\[(\\w+) instance exist\\]";
        final String TEMPLATE_IS_BEING_INSTALLED_PATTERN = "\\[Installing template (\\w+)\\]";
        final String TEMPLATE_IS_BEING_IMPORTED_PATTERN = "\\[Importing (\\w+)\\]";
        final String TEMPLATE_IS_BEING_DOWNLOADED_PATTERN = "\\[Downloading (\\w+)\\]";
        final String TEMPLATE_DOWNLOAD_PERCENT_PATTERN = "(\\d+\\.\\d+)%";
        final Pattern templateExistsPattern = Pattern.compile( TEMPLATE_EXISTS_PATTERN, Pattern.MULTILINE );
        final Pattern templateIsBeingInstalledPattern = Pattern.compile( TEMPLATE_IS_BEING_INSTALLED_PATTERN, Pattern.MULTILINE );
        final Pattern templateIsBeingImportedPattern = Pattern.compile( TEMPLATE_IS_BEING_IMPORTED_PATTERN, Pattern.MULTILINE );
        final Pattern templateIsBeingDownloadedPattern = Pattern.compile( TEMPLATE_IS_BEING_DOWNLOADED_PATTERN );
        final Pattern templateDownloadPercentPattern = Pattern.compile( TEMPLATE_DOWNLOAD_PERCENT_PATTERN );


        final Map<String, Integer> templateProgress = Maps.newHashMap();

        final RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "subutai import %s", template ) ).withTimeout( 360000 );

        executor.executeAsync( hostId, requestBuilder, new CommandCallback()
        {
            String currentDownloadedTemplate;


            @Override
            public void onResponse( final Response response, final CommandResult commandResult )
            {

                if ( response != null && response.getStdOut() != null )
                {
                    //template download start, mark template as 0% downloaded
                    Matcher templateDownloadStartedMatcher =
                            templateIsBeingImportedPattern.matcher( response.getStdOut() );
                    if ( templateDownloadStartedMatcher.groupCount() > 0 )
                    {
                        while ( templateDownloadStartedMatcher.find() )
                        {
                            templateProgress.put( templateDownloadStartedMatcher.group( 1 ), 0 );
                        }
                    }

                    //download in progress

                    StringTokenizer stringTokenizer = new StringTokenizer( commandResult.getStdOut(), "\n\r\f" );

                    while ( stringTokenizer.hasMoreTokens() )
                    {
                        String line = stringTokenizer.nextToken();

                        Matcher downloadingMatcher = templateIsBeingDownloadedPattern.matcher( line );

                        if ( downloadingMatcher.groupCount() > 0 && downloadingMatcher.find() )
                        {
                            currentDownloadedTemplate = downloadingMatcher.group( 1 );
                        }


                        if ( currentDownloadedTemplate != null )
                        {
                            Matcher downloadPercentMatcher = templateDownloadPercentPattern.matcher( line );
                            if ( downloadPercentMatcher.groupCount() > 0 && downloadPercentMatcher.find() )
                            {
                                templateProgress.put( currentDownloadedTemplate,
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
                            templateProgress.put( templateExistsMatcher.group( 1 ), 100 );
                        }
                    }

                    Matcher installingMatcher = templateIsBeingInstalledPattern.matcher( response.getStdOut() );
                    //suppose if a template is being installed that indicates it is 100% downloaded
                    if ( installingMatcher.groupCount() > 0 )
                    {
                        while ( installingMatcher.find() )
                        {
                            templateProgress.put( installingMatcher.group( 1 ), 100 );
                        }
                    }


                    LOG.info( templateProgress.toString() );
                    LOG.info( "RESPONSE: {}", response.getStdOut() );
                }
            }
        } );

        return null;
    }
}
