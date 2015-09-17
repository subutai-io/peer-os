package io.subutai.core.environment.ui.forms;


import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;

import com.google.common.base.Strings;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;


public class SshKeyWindow extends Window
{
    private final TextArea sshKeyTxt;


    public SshKeyWindow( final Environment environment )
    {

        setCaption( "Ssh key" );
        setWidth( "600px" );
        setHeight( "300px" );
        setModal( true );
        setClosable( true );


        GridLayout content = new GridLayout( 2, 2 );
        content.setSizeFull();
        content.setMargin( true );
        content.setSpacing( true );


        boolean keyExists = !Strings.isNullOrEmpty( environment.getSshKey() );
        CheckBox keyExistsChk = new CheckBox( "Key exists" );
        keyExistsChk.setValue( keyExists );
        keyExistsChk.setReadOnly( true );

        content.addComponent( keyExistsChk );

        Button removeKeyBtn = new Button( "Remove key" );
        removeKeyBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                try
                {
                    environment.setSshKey( null, true );
                    Notification.show( "Please, wait..." );
                    close();
                }
                catch ( EnvironmentModificationException e )
                {
                    Notification.show( "Error setting ssh key", e.getMessage(), Notification.Type.ERROR_MESSAGE );
                }
            }
        } );
        removeKeyBtn.setEnabled( keyExists );

        content.addComponent( removeKeyBtn );

        sshKeyTxt = createSshKeyTxt();

        content.addComponent( sshKeyTxt );

        Button setSshKeyBtn = new Button( "Set key" );
        setSshKeyBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {

                String sshKey = sshKeyTxt.getValue();
                if ( Strings.isNullOrEmpty( sshKey ) )
                {
                    Notification.show( "Please, enter key" );
                    return;
                }
                try
                {
                    environment.setSshKey( sshKey, true );
                    Notification.show( "Please, wait..." );
                    close();
                }
                catch ( EnvironmentModificationException e )
                {
                    Notification.show( "Error setting ssh key", e.getMessage(), Notification.Type.ERROR_MESSAGE );
                }
            }
        } );

        content.addComponent( setSshKeyBtn, 1, 1 );

        setContent( content );
    }


    private TextArea createSshKeyTxt()
    {
        TextArea sshKeyTxt = new TextArea( "Ssh key" );
        sshKeyTxt.setId( "sshKeyTxt" );
        sshKeyTxt.setRows( 7 );
        sshKeyTxt.setColumns( 30 );
        sshKeyTxt.setImmediate( true );
        sshKeyTxt.setWordwrap( true );

        return sshKeyTxt;
    }
}
