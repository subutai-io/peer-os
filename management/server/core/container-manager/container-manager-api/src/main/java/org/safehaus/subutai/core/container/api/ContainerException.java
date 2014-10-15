/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.container.api;


import org.safehaus.subutai.common.exception.SubutaiException;


/**
 * Exception which can be thrown while destroying containers
 */
public class ContainerException extends SubutaiException
{

    public ContainerException( String message )
    {
        super( message );
    }
}
