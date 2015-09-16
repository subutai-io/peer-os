package io.subutai.common.settings;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;


/**
 * Class contains list of  cli commands in Subutai.
 */
public class CLISettings
{
    public static Map<String, Set<String>> CLI_CMD_MAP = new HashMap<String, Set<String>>()
    {
        {
            put( "command", Sets.newHashSet( "exec-async", "exec-sync" ) );

            put( "env", Sets.newHashSet( "build-local", "destroy-container", "destroy", "grow-local", "list", "remove",
                    "view" ) );

            put( "environment", Sets.newHashSet( "build-local", "destroy-container", "destroy", "grow-local", "list", "remove",
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

            put( "ssl-context", Sets.newHashSet( "ssl-context", "jetty" ) );

            put( "test", Sets.newHashSet( "exec" ) );

            put( "node", Sets.newHashSet( "approve", "reject", "list", "generate-token", "verify" ) );

            put( "registry",
                    Sets.newHashSet( "get-template", "register-template", "get-child-templates", "get-parent-template",
                            "unregister-template", "list-template-tree", "list-templates", "get-parent-templates" ) );
        }
    };
}
