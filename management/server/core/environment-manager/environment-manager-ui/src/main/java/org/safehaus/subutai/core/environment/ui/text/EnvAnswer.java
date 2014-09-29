package org.safehaus.subutai.core.environment.ui.text;


/**
 * Created by bahadyr on 9/25/14.
 */
public enum EnvAnswer
{
    START( "Environment build process started." ),
    SUCCESS( "Environment created successfully." ),
    FAIL( "Failed creating environment." ),
    NO_BUILD_PROCESS( "No environment build process." );


    String answer;


    EnvAnswer( final String answer )
    {
        this.answer = answer;
    }


    public String getAnswer()
    {
        return answer;
    }
}
