/**
 * DISCLAIMER
 *
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 *
 * @author jouni@vaadin.com
 *
 */

package io.subutai.server.ui.views;


import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;


public class ScheduleView extends CssLayout implements View
{

    private CssLayout catalog;

    private Window popup;

    // private CSSInject css;


    public ScheduleView()
    {
        setSizeFull();
        addStyleName( "schedule" );

        // css = new CSSInject(UI.getCurrent());

        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addStyleName( "borderless" );
        addComponent( tabs );
        tabs.addComponent( new Label( "Shedule" ) );

        catalog = new CssLayout();
        catalog.setCaption( "Catalog" );
        catalog.addStyleName( "catalog" );
        tabs.addComponent( catalog );
    }


    @Override
    public void enter( ViewChangeEvent event )
    {

    }
}
