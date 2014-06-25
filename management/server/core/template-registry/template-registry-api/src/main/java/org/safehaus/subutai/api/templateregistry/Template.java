package org.safehaus.subutai.api.templateregistry;


/**
 * Created by dilshat on 6/25/14.
 */
public class Template {

    private String templateName;
    private String parentTemplateName;


    /*
    # Template used to create this container: /usr/share/lxc/templates/lxc-ubuntu
    # Parameters passed to the template: -u subutai -S /root/.ssh/id_dsa.pub
    # For additional config options, please look at lxc.conf(5)

    # Common configuration
    lxc.include = /usr/share/lxc/config/ubuntu.common.conf

    # Container specific configuration
    lxc.rootfs = /var/lib/lxc/foo/rootfs
    lxc.mount = /var/lib/lxc/foo/fstab
    lxc.utsname = foo
    lxc.arch = amd64

    # Network configuration
    lxc.network.type = veth
    lxc.network.flags = up
    lxc.network.link = lxcbr0
    lxc.network.hwaddr = 00:16:3e:f7:b1:87
    subutai.config.path = /etc
    subutai.app.data.path = /var
    lxc.hook.pre-start = /etc/subutai/pre_start_hook
    subutai.parent = master
    subutai.git.branch = foo
    lxc.mount.entry = /lxc/foo-opt opt none bind,rw 0 0
    lxc.mount.entry = /lxc-data/foo-home home none bind,rw 0 0
    lxc.mount.entry = /lxc-data/foo-var var none bind,rw 0 0
    subutai.git.uuid = 7375fea345dc326cdc8a54a62d9b6cc96251a2ad


    + packages manifest

     */


    public String getTemplateName() {
        return templateName;
    }


    public String getParentTemplateName() {
        return parentTemplateName;
    }
}
