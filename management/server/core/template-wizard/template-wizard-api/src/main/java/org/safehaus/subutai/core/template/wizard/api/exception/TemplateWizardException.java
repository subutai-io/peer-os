package org.safehaus.subutai.core.template.wizard.api.exception;


/**
 * Created by talas on 3/18/15.
 */
public class TemplateWizardException extends Exception
{
    public TemplateWizardException()
    {
        super();
    }


    public TemplateWizardException( final String message )
    {
        super( message );
    }


    public TemplateWizardException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public TemplateWizardException( final Throwable cause )
    {
        super( cause );
    }


    public TemplateWizardException( final String message, final Throwable cause, final boolean enableSuppression,
                                    final boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }
}
