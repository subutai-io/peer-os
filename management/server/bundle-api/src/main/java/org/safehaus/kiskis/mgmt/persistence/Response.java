/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.persistence;

/**
 *
 * @author dilshat
 */
public interface Response {

    public Long getId();
    
    public String getSource();

    public String getUuid();

    public Long getRequestSequenceNumber();

    public Long getResponseSequenceNumber();

    public String getBody();
}
