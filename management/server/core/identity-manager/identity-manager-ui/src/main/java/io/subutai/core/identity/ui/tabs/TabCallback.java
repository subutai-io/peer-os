package io.subutai.core.identity.ui.tabs;


public interface TabCallback<T>
{
    public void saveOperation( T value, final boolean newValue );

    public void removeOperation( T value, final boolean newValue );

    public void cancelOperation();
}
