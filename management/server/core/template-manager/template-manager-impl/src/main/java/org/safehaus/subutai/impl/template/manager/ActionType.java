package org.safehaus.subutai.impl.template.manager;

enum ActionType {

    SETUP("subutai-setup"),
    CLONE("subutai-clone"),
    CLONE_DESTROY("subutai-clone-destroy"),
    EXPORT("subutai-export"),
    IMPORT("subutai-import"),
    TEMPLATE("subutai-template"),
    INSTALL_TEMPLATE("apt-get install", true),
    LIST_TEMPLATES("subutai list -t"),
    LIST_CONTAINERS("subutai list -c"),
    LIST_CONT_TEMP("subutai list");

    private static final String PARENT_DIR = "/usr/bin/";
    private final String script;
    private boolean standAloneCommand = false;

    private ActionType(String script) {
        this.script = script;
    }

    private ActionType(String script, boolean standAlone) {
        this.script = script;
        this.standAloneCommand = standAlone;
    }

    String buildCommand(String... args) {
        StringBuilder sb = new StringBuilder();
        if(!standAloneCommand) sb.append(PARENT_DIR);
        sb.append(this.script);
        if(args != null)
            for(String arg : args) sb.append(" ").append(arg);
        return sb.toString();
    }

}
