package io.subutai.server.ui.util;


import io.subutai.server.ui.MainUI;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;


public class MgmtUIProvider extends UIProvider
{

    @Override
    public Class<? extends UI> getUIClass( UIClassSelectionEvent event )
    {
        if ( event.getRequest().getParameter( "mobile" ) != null && event.getRequest().getParameter( "mobile" )
                                                                         .equals( "false" ) )
        {
            return MainUI.class;
        }

        if ( event.getRequest().getHeader( "user-agent" ).toLowerCase().contains( "mobile" ) && !event.getRequest()
                                                                                                      .getHeader(
                                                                                                              "user-agent" )
                                                                                                      .toLowerCase()
                                                                                                      .contains(
                                                                                                              "ipad" ) )
        {
            return MobileCheckUI.class;
        }

        return MainUI.class;
    }
}