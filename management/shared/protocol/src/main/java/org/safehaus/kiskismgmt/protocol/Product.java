package org.safehaus.kiskismgmt.protocol;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA. User: aralbaev Date: 10/21/13 Time: 6:03 PM To
 * change this template use File | Settings | File Templates.
 */
public class Product implements Serializable {

    private String productName;
    private Enum commands;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Enum getCommands() {
        return commands;
    }

    public void setCommands(Enum commands) {
        this.commands = commands;
    }
}
