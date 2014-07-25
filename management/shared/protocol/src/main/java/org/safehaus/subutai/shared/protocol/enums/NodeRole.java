package org.safehaus.subutai.shared.protocol.enums;


/**
 * Created by dilshat on 7/25/14.
 */
public enum NodeRole implements NodeRoleInterface {
    DEFAULT_NODE( "DEFAULT" );

    private String roleName;


    NodeRole( final String roleName ) {
        this.roleName = roleName;
    }


    @Override
    public String getRoleName() {
        return null;
    }
}
