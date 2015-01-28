package org.safehaus.subutai.core.env.ui.forms;


import org.safehaus.subutai.core.env.api.Environment;
import org.safehaus.subutai.core.env.api.EnvironmentManager;

import com.google.common.base.Strings;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class SshKeyWindow extends Window
{
    private final EnvironmentManager environmentManager;
    private final Environment environment;
    private final TextArea sshKeyTxt;


    public SshKeyWindow( final EnvironmentManager environmentManager, final Environment environment )
    {
        this.environmentManager = environmentManager;
        this.environment = environment;

        setCaption( "Set ssh key" );
        setWidth( "800px" );
        setHeight( "600px" );
        setModal( true );
        setClosable( true );

        VerticalLayout content = new VerticalLayout();
        content.setSpacing( true );
        content.setMargin( true );
        content.setStyleName( "default" );
        content.setSizeFull();

        CheckBox keyExistsChk = new CheckBox( "Key exists" );
        keyExistsChk.setReadOnly( true );
        keyExistsChk.setValue( !Strings.isNullOrEmpty( environment.getPublicKey() ) );

        content.addComponent( keyExistsChk );

        sshKeyTxt = createSshKeyTxt();

        content.addComponent( sshKeyTxt );

        Button setSshKeyBtn = new Button( "Set" );
        setSshKeyBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {

            }
        } );

        content.addComponent( setSshKeyBtn );

        setContent( content );
    }


    private TextArea createSshKeyTxt()
    {
        TextArea sshKeyTxt = new TextArea( "Ssh key" );
        sshKeyTxt.setId( "sshKeyTxt" );
        sshKeyTxt.setRows( 13 );
        sshKeyTxt.setColumns( 42 );
        sshKeyTxt.setImmediate( true );
        sshKeyTxt.setWordwrap( true );

        return sshKeyTxt;
    }
}
