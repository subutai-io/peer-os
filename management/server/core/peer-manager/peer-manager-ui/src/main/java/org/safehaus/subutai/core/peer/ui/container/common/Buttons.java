/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.peer.ui.container.common;


/**
 *
 */
public enum Buttons
{

    INFO( "Info" ), START( "Start" ), STOP( "Stop" ), DESTROY( "Destroy" ), START_ALL( "Start All" ),
    STOP_ALL( "Stop All" ), DESTROY_ALL( "Destroy All" );

    private final String buttonLabel;


    private Buttons( String buttonLabel )
    {
        this.buttonLabel = buttonLabel;
    }


    public String getButtonLabel()
    {
        return buttonLabel;
    }

}
