package org.safehaus.kiskis.mgmt.shared.protocol.products;

/**
 * Created with IntelliJ IDEA.
 * User: aralbaev
 * Date: 10/21/13
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public enum HadoopCommandEnum {
    INSTALL_HADOOP("apt-get install hadoop") ,
    UNINSTALL_HADOOP("apt-get remove hadoop");
    String command;

    private HadoopCommandEnum(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
