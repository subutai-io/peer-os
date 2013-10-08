/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol.elements;

/**
 * @author Dilshat
 */
public enum RequestType {

    EXECUTE_REQUEST(1, "EXECUTE-REQUEST"), REGISTRATION_REQUEST_DONE(2, "REGISTRATION-REQUEST-DONE"), HEARTBEAT_REQUEST(3, "HEARTBEAT-REQUEST");
    int id;
    String typeValue;

    private RequestType(int id, String typeValue) {
        this.id = id;
        this.typeValue = typeValue;
    }

    public int getId() {
        return id;
    }

    public String getTypeValue() {
        return typeValue;
    }
}
