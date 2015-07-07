/**
 * DISCLAIMER
 *
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 *
 * @author jouni@vaadin.com
 *
 */

package io.subutai.server.ui.util;


import java.util.ArrayList;
import java.util.List;

import com.vaadin.navigator.View;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;


public class HelpManager
{

    private UI ui;
    private List<HelpOverlay> overlays = new ArrayList<HelpOverlay>();


    public HelpManager( UI ui )
    {
        this.ui = ui;
    }


    public void closeAll()
    {
        for ( HelpOverlay overlay : overlays )
        {
            overlay.close();
        }
        overlays.clear();
    }


    public void showHelpFor( View view )
    {
        // showHelpFor(view.getClass());
    }


    public void showHelpFor( Class<? extends View> view )
    {
        // TODO need logic for showing help for View
    }


    public HelpOverlay addOverlay( String caption, String text, String style )
    {
        HelpOverlay o = new HelpOverlay();
        o.setCaption( caption );
        o.addComponent( new Label( text, ContentMode.HTML ) );
        o.setStyleName( style );
        overlays.add( o );
        return o;
    }
}
