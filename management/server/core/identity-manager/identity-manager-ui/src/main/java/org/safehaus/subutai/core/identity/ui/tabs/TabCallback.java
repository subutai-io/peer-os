package org.safehaus.subutai.core.identity.ui.tabs;


/**
 * Created by talas on 2/5/15.
 */
public interface TabCallback<T>
{
    public void savePermission( T value );

    public void removeOperation( T value );

    public void updatePermission( T value );
}
