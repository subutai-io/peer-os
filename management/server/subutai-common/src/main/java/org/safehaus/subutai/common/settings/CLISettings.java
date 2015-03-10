package org.safehaus.subutai.common.settings;


/**
 * Created by nisakov on 3/9/15.
 */
public class CLISettings
{
    public static String[] COMMANDS =
    {
            "command/exec-sync",

            "env/grow-local",
            "env/list",
            "env/view",
            "env/destroy",
            "env/destroy-container",
            "env/remove",

            "git/diff-branches",
            "git/diff-file",
            "git/get-current-branch",
            "git/list-branches",
            "git/commit-all",
            "git/commit-files",
            "git/add-files",
            "git/add-all",
            "git/delete-files",
            "git/clone",
            "git/checkout",
            "git/push",
            "git/delete-branch",
            "git/pull",
            "git/merge",
            "git/init",
            "git/undo-soft",
            "git/undo-hard",
            "git/revert-commit",
            "git/stash",
            "git/unstash",
            "git/list-stashes",
            "host/list",
            "host/container-host",
            "host/resource-host",

            "km/gen",
            "km/list",
            "km/export-key",
            "km/gen-subkey",

            "quota/list-quota",
            "quota/get-quota",
            "quota/set-quota",

            "metric/resource-host-metrics",
            "metric/container-host-metrics",

            "net/setup-n2n",
            "net/remove-n2n",
            "net/setup-tunnel",
            "net/remove-tunnel",
            "net/set-container-ip",
            "net/remove-container-ip",

            
            "peer/message",
            "peer/get-quota",
            "peer/register",
            "peer/ls",
            "peer/unregister",
            "peer/id",
            "peer/import-template",
            "peer/hosts",
            "peer/stop-container",
            "peer/start-container",
            "peer/clean",
            "peer/tag-container",
            "peer/get-reserved-vni",
            "peer/jetty",

            "repo/add",
            "repo/remove",
            "repo/extract",
            "repo/extract-files",
            "repo/list",
            "repo/info",

            "ssl-context/ssl-context",

            "registry/get-template",
            "registry/register-template",
            "registry/get-child-templates",
            "registry/get-parent-template",
            "registry/unregister-template",
            "registry/list-template-tree",
            "registry/list-templates",
            "registry/get-parent-templates",
    

        ""
    };
  
}
