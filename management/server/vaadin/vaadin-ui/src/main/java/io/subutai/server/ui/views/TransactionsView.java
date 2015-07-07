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
import com.vaadin.ui.VerticalLayout;


public class TransactionsView extends VerticalLayout implements View
{

    public TransactionsView()
    {
        setSizeFull();
        addStyleName( "transactions" );
    }


    @Override
    public void enter( ViewChangeEvent event )
    {


    }
}
