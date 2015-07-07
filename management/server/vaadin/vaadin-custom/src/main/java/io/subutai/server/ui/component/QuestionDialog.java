/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package io.subutai.server.ui.component;


import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class QuestionDialog<T>
{

    private Window alert;
    private Button cancel, ok;
    private TextField inputField;
    private T convertedValue;
    private VerticalLayout l;
    private Class<T> expectedParameterClass;


    public QuestionDialog( Action action, String question, Class<T> expectedParameterClass, String yesLabel,
                           String cancelLabel )
    {
        this.expectedParameterClass = expectedParameterClass;
        l = new VerticalLayout();
        l.setSizeUndefined();
        l.setMargin( true );
        l.setSpacing( true );

        alert = new Window( action.getCaption(), l );
        alert.setModal( true );
        alert.setResizable( false );
        alert.setDraggable( false );
        alert.addStyleName( "dialog" );
        alert.setClosable( false );

        inputField = new TextField();
        inputField.setCaption( question );
        inputField.setConverter( expectedParameterClass );
        cancel = new Button( cancelLabel );
        ok = new Button( yesLabel );
    }


    public Window getAlert()
    {

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidth( "100%" );
        buttons.setSpacing( true );
        l.addComponent( inputField );
        l.addComponent( buttons );

        cancel.addStyleName( "small" );
        cancel.addStyleName( "wide" );
        cancel.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                alert.close();
            }
        } );
        buttons.addComponent( cancel );


        ok.addStyleName( "default" );
        ok.addStyleName( "small" );
        ok.addStyleName( "wide" );
        ok.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {


                String uiValue = inputField.getValue();
                if ( isConvertible( uiValue ) )
                {
                    inputField.setComponentError( null );
                    inputField.setValidationVisible( false );
                    alert.close();
                }
                else
                {
                    inputField.setComponentError( new UserError( "Input " + uiValue +
                            " could not be converted to " + expectedParameterClass ) );
                    Notification.show( "Please enter proper input", Notification.Type.ERROR_MESSAGE );
                }
            }
        } );

        buttons.addComponent( ok );
        ok.focus();

        alert.addShortcutListener( new ShortcutListener( "Cancel", ShortcutAction.KeyCode.ESCAPE, null )
        {
            @Override
            public void handleAction( Object sender, Object target )
            {
                alert.close();
            }
        } );

        return alert;
    }


    public Button getOk()
    {
        return ok;
    }


    public Button getCancel()
    {
        return cancel;
    }


    public TextField getInputField()
    {
        return inputField;
    }


    private void setConvertedValue( T convertedValue )
    {
        this.convertedValue = convertedValue;
    }


    public boolean isConvertible( String uiValue )
    {
        if ( uiValue == null )
        {
            return false;
        }
        try
        {
            setConvertedValue( ( T ) inputField.getConvertedValue() );
            return true;
        }
        catch ( Converter.ConversionException e )
        {
            return false;
        }
    }


    public T getConvertedValue()
    {
        return convertedValue;
    }
}
