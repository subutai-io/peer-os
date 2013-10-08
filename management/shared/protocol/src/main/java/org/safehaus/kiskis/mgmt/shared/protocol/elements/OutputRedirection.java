/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol.elements;

/**
 * @author Dilshat
 */
public enum OutputRedirection {

    CAPTURE(1, "CAPTURE"), CAPTURE_AND_RETURN(2, "CAPTURE-AND-RETURN"), RETURN(3, "RETURN"), NOTHING(4, "");
    int id;
    String argumentValue;

    private OutputRedirection(int id, String argumentValue) {
        this.id = id;
        this.argumentValue = argumentValue;
    }

    public int getId() {
        return id;
    }

    public String getArgumentValue() {
        return argumentValue;
    }
}
