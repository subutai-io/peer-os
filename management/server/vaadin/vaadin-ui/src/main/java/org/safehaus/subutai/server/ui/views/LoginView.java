package org.safehaus.subutai.server.ui.views;


import org.safehaus.subutai.common.helper.UserIdMdcHelper;
import org.safehaus.subutai.server.ui.MainUI;
import org.safehaus.subutai.server.ui.util.HelpManager;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


/**
 * Created by timur on 12/8/14.
 */
public class LoginView extends VerticalLayout implements View

{
    private Label error;
    private HelpManager helpManager;
    private MainUI mainUI;


    public LoginView( MainUI mainUI, HelpManager helpManager )
    {
        this.mainUI = mainUI;
        this.helpManager = helpManager;
        buildView();
    }


    private void buildView()
    {
        helpManager.closeAll();

        addStyleName( "login" );

        setSizeFull();
        addStyleName( "login-layout" );

        final CssLayout loginPanel = new CssLayout();
        loginPanel.addStyleName( "login-panel" );

        HorizontalLayout labels = new HorizontalLayout();
        labels.setWidth( "100%" );
        labels.setMargin( true );
        labels.addStyleName( "labels" );
        loginPanel.addComponent( labels );

        Label welcome = new Label( "Welcome" );
        welcome.setSizeUndefined();
        welcome.addStyleName( "h4" );
        labels.addComponent( welcome );
        labels.setComponentAlignment( welcome, Alignment.MIDDLE_LEFT );

        Label title = new Label( "Subutai" );
        title.setSizeUndefined();
        title.addStyleName( "h2" );
        title.addStyleName( "light" );
        labels.addComponent( title );
        labels.setComponentAlignment( title, Alignment.MIDDLE_RIGHT );

        HorizontalLayout fields = new HorizontalLayout();
        fields.setSpacing( true );
        fields.setMargin( true );
        fields.addStyleName( "fields" );

        final TextField username = new TextField( "Username" );
        username.setValue( "admin" );
        username.focus();
        fields.addComponent( username );

        final PasswordField password = new PasswordField( "Password" );
        password.setValue( "secret" );
        fields.addComponent( password );

        final Button signin = new Button( "Sign In" );
        signin.addStyleName( "default" );
        fields.addComponent( signin );
        fields.setComponentAlignment( signin, Alignment.BOTTOM_LEFT );

        error = new Label( "", ContentMode.HTML );
        error.addStyleName( "error" );
        error.setSizeUndefined();
        error.addStyleName( "light" );
        // Add animation
        error.addStyleName( "v-animate-reveal" );
        loginPanel.addComponent( error );

        final ShortcutListener enter = new ShortcutListener( "Sign In", ShortcutAction.KeyCode.ENTER, null )
        {
            @Override
            public void handleAction( Object sender, Object target )
            {
                signin.click();
            }
        };

        signin.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {

                Subject currentUser = SecurityUtils.getSubject();

                UsernamePasswordToken usernamePasswordToken =
                        new UsernamePasswordToken( username.getValue(), password.getValue() );
                try
                {
                    currentUser.login( usernamePasswordToken );
//                    UserIdMdcHelper.set( currentUser );
                    mainUI.getUsername().setValue( username.getValue() );
                    VaadinService.reinitializeSession( VaadinService.getCurrentRequest() );
                    getUI().getNavigator().navigateTo( "/core" );
                }
                catch ( Exception e )
                {
                    error.setValue( "Wrong username or password. <span>Hint: admin:secret</span>" );
                    username.focus();
                }
                username.setValue( "" );
                password.setValue( "" );
            }
        } );

        signin.addShortcutListener( enter );

        loginPanel.addComponent( fields );

        addComponent( loginPanel );
        setComponentAlignment( loginPanel, Alignment.MIDDLE_CENTER );
    }


    @Override
    public void enter( final ViewChangeListener.ViewChangeEvent viewChangeEvent )
    {

    }
}
