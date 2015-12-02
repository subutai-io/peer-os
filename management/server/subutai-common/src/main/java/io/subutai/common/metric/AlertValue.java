package io.subutai.common.metric;


/**
 * Alert value interface
 */
public interface AlertValue<T>
{
    <T> T getValue();

    //TODO: throw ValidationException and return exception descriptions
    boolean validate();
}
