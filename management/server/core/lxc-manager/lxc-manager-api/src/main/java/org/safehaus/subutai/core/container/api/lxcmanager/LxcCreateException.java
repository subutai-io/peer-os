/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.container.api.lxcmanager;


/**
 * Exception which can be thrown while creating lxcs
 */
public class LxcCreateException extends Exception
{

    public LxcCreateException( String message )
    {
        super( message );
    }
}
