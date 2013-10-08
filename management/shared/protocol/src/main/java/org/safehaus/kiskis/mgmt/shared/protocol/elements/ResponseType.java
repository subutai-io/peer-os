/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol.elements;

/**
 * @author dilshat
 */
public enum ResponseType {

    EXECUTE_RESPONSE(1, "EXECUTE-RESPONSE"), EXECUTE_RESPONSE_DONE(2, "EXECUTE-RESPONSE-DONE"), REGISTRATION_REQUEST(3, "REGISTRATION-REQUEST"), HEARTBEAT_RESPONSE(4, "HEARTBEAT-RESPONSE");
    int id;
    String typeValue;

    private ResponseType(int id, String typeValue) {
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
