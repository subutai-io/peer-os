package org.safehaus.subutai.core.environment.ui.text;


/**
 * Created by bahadyr on 9/25/14.
 */
public enum EnvAnswer
{
    SUCCESS( "Cloning containers finished successfully." ), FAIL( "Cloning containers failed." );


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
