package org.safehaus.subutai.core.identity.ui.tabs.subviews;


import java.util.EnumSet;

import org.safehaus.subutai.core.identity.api.PermissionGroup;

import com.vaadin.ui.ComboBox;


/**
 * Created by talas on 2/5/15.
 */
public class PermissionGroupComboBox extends ComboBox
{
    public PermissionGroupComboBox( String caption )
    {
        super( caption, EnumSet.allOf( PermissionGroup.class ) );
    }


    @Override
    public PermissionGroup getValue()
    {
        for ( final PermissionGroup group : EnumSet.allOf( PermissionGroup.class ) )
        {
            if ( group.getName().equals( super.getValue() ) )
            {
                return group;
            }
        }
        return null;
    }
}
