package org.safehaus.subutai.impl.template.manager;

enum ActionType {

    CLONE("subutai-clone"),
    CLONE_DESTROY("subutai-clone-destroy"),
    EXPORT("subutai-export"),
    IMPORT("subutai-import"),
    TEMPLATE("subutai-template");

    private static final String PARENT_DIR = "/usr/bin/";
    private final String script;

    private ActionType(String script) {
        this.script = script;
    }

    String buildCommand(String... args) {
        StringBuilder sb = new StringBuilder(PARENT_DIR);
        sb.append(this.script);
        if(args != null)
            for(String arg : args) sb.append(" ").append(arg);
        return sb.toString();
    }

}
