package org.safehaus.subutai.core.env.api.exception;


/**
 * The class {@code EnvironmentCreationException} extends class {@code Exception}
 * Thrown if error occurred while creating new environment
 * Exception may be thrown on any event relevant to environment creation process,
 * if some conditions don't apply to target result needs.
 */
public class EnvironmentCreationException extends Exception
{

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * java.security.PrivilegedActionException}).
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public EnvironmentCreationException( final Throwable cause )
    {
        super( cause );
    }


    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public EnvironmentCreationException( final String message )
    {
        super( message );
    }
}
