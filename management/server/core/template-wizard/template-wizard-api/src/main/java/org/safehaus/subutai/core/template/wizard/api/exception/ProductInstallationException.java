package org.safehaus.subutai.core.template.wizard.api.exception;


public class ProductInstallationException extends TemplateWizardException
{
    public ProductInstallationException()
    {
        super();
    }


    public ProductInstallationException( final String message )
    {
        super( message );
    }


    public ProductInstallationException( final String message, final Throwable cause )
    {
        super( message, cause );
    }


    public ProductInstallationException( final Throwable cause )
    {
        super( cause );
    }


    public ProductInstallationException( final String message, final Throwable cause, final boolean enableSuppression,
                                         final boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }
}
