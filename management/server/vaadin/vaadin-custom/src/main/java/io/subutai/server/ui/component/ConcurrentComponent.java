/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.server.ui.component;


import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;


/**
 * @author dilshat
 */
public class ConcurrentComponent extends VerticalLayout
{

    protected void executeUpdate( Runnable update )
    {
        UI application;
        synchronized ( this )
        {
            application = UI.getCurrent();
            if ( application == null )
            {
                new Thread( update ).start();
                //                update.run();
                return;
            }
        }
        synchronized ( application )
        {

            new Thread( update ).start();
            //            update.run();
        }
    }
}
