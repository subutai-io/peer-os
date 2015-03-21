package org.safehaus.subutai.core.template.wizard.api.exception;


/**
 * Created by talas on 3/18/15.
 */
public class ScriptProcessingException extends TemplateWizardException
{
    public ScriptProcessingException()
    {
        super();
    }


    public ScriptProcessingException( final String message )
    {
        super( message );
    }


    public ScriptProcessingException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public ScriptProcessingException( final Throwable cause )
    {
        super( cause );
    }


    public ScriptProcessingException( final String message, final Throwable cause, final boolean enableSuppression,
                                      final boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }
}
