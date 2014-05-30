/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.server.ui;

import java.io.Serializable;

/**
 *
 * @author dilshat
 */
public interface ConfirmationDialogCallback extends Serializable {

    void response(boolean ok);

}
