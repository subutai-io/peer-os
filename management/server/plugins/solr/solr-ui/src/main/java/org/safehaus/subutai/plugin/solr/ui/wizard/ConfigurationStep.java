/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.solr.ui.wizard;


import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


/**
 * @author dilshat
 */
public class ConfigurationStep extends VerticalLayout {

    public ConfigurationStep( final Wizard wizard ) {

        setSizeFull();

        GridLayout content = new GridLayout( 1, 3 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        final TextField clusterNameTxtFld = new TextField( "Enter installation name" );
        clusterNameTxtFld.setInputPrompt( "Installation name" );
        clusterNameTxtFld.setRequired( true );
        clusterNameTxtFld.setMaxLength( 20 );
        clusterNameTxtFld.setValue( wizard.getConfig().getClusterName() );
        clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                wizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
            }
        } );


        Button next = new Button( "Next" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) ) {
                    show( "Please provide installation name" );
                }
                else {
                    wizard.next();
                }
            }
        } );

        Button back = new Button( "Back" );
        back.addStyleName( "default" );
        back.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                wizard.back();
            }
        } );

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing( true );
        layout.addComponent( new Label( "Please, specify installation settings" ) );
        layout.addComponent( content );

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent( back );
        buttons.addComponent( next );

        content.addComponent( clusterNameTxtFld );
        content.addComponent( buttons );

        addComponent( layout );
    }


    private void show( String notification ) {
        Notification.show( notification );
    }
}
