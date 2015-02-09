package org.safehaus.subutai.core.identity.ui.tabs;


/**
 * Created by talas on 2/5/15.
 */
public interface TabCallback<T>
{
    public void saveOperation( T value, final boolean newValue );

    public void removeOperation( T value, final boolean newValue );

    public void cancelOperation();
}
