package org.safehaus.subutai.common.settings;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;


/**
 * Created by nisakov on 3/9/15.
 */
public class CLISettings
{
    public static String[] COMMANDS = {
            "command/exec-sync",

            "env/grow-local", "env/list", "env/view", "env/destroy", "env/destroy-container", "env/remove",

            "git/diff-branches", "git/diff-file", "git/get-current-branch", "git/list-branches", "git/commit-all",
            "git/commit-files", "git/add-files", "git/add-all", "git/delete-files", "git/clone", "git/checkout",
            "git/push", "git/delete-branch", "git/pull", "git/merge", "git/init", "git/undo-soft", "git/undo-hard",
            "git/revert-commit", "git/stash", "git/unstash", "git/list-stashes",

            "host/list", "host/container-host", "host/resource-host",

            "km/gen", "km/list", "km/export-key", "km/gen-subkey",

            "quota/list-quota", "quota/get-quota", "quota/set-quota",

            "metric/resource-host-metrics", "metric/container-host-metrics",

            "net/setup-n2n", "net/remove-n2n", "net/setup-tunnel", "net/remove-tunnel", "net/set-container-ip",
            "net/remove-container-ip",


            "peer/message", "peer/get-quota", "peer/register", "peer/ls", "peer/unregister", "peer/id",
            "peer/import-template", "peer/hosts", "peer/stop-container", "peer/start-container", "peer/clean",
            "peer/tag-container", "peer/get-reserved-vni", "peer/jetty",

            "repo/add", "repo/remove", "repo/extract", "repo/extract-files", "repo/list", "repo/info",

            "ssl-context/ssl-context",

            "registry/get-template", "registry/register-template", "registry/get-child-templates",
            "registry/get-parent-template", "registry/unregister-template", "registry/list-template-tree",
            "registry/list-templates", "registry/get-parent-templates",


            ""
    };
    public static Map<String, Set<String>> CLI_CMD_MAP = new HashMap<String, Set<String>>()
    {
        {
            put( "command", Sets.newHashSet( "exec-async", "exec-sync" ) );

            put( "env", Sets.newHashSet( "build-local", "destroy-container", "destroy", "grow-local", "list", "remove",
                    "view" ) );

            put( "git", Sets.newHashSet( "add-all", "add-files", "checkout", "clone", "commit-all", "commit-files",
                    "delete-branch", "delete-files", "diff-branches", "diff-file", "get-current-branch", "init",
                    "list-branches", "list-stashes", "merge", "pull", "push", "revert-commit", "stash", "undo-hard",
                    "undo-soft", "unstash" ) );

            put( "host", Sets.newHashSet( "container-host", "resource-host", "list" ) );

            put( "km", Sets.newHashSet( "export-key", "gen", "gen-subkey", "list" ) );

            put( "quota", Sets.newHashSet( "get-quota", "list-quota", "set-quota" ) );

            put( "metric", Sets.newHashSet( "resource-host-metrics", "container-host-metrics" ) );

            put( "net", Sets.newHashSet( "setup-n2n", "remove-n2n", "setup-tunnel", "remove-tunnel", "set-container-ip",
                    "remove-container-ip" ) );

            put( "peer",
                    Sets.newHashSet( "message", "get-quota", "register", "ls", "unregister", "id", "import-template",
                            "hosts", "stop-container", "start-container", "clean", "tag-container", "get-reserved-vni",
                            "jetty" ) );

            put( "repo", Sets.newHashSet( "add", "remove", "extract", "extract-files", "list", "info" ) );

            put( "ssl-context", Sets.newHashSet( "ssl-context" ) );

            put( "test", Sets.newHashSet( "exec" ) );

            put( "registry",
                    Sets.newHashSet( "get-template", "register-template", "get-child-templates", "get-parent-template",
                            "unregister-template", "list-template-tree", "list-templates", "get-parent-templates" ) );
        }
    };
}
