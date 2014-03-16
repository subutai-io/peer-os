/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.api.mongodb;

/**
 *
 * @author dilshat
 */
public interface Mongo {

    /**
     * Returns list of configurations of installed clusters in form of json
     * array string
     *
     * @return - json array of existing clusters e.g. [ {clusterName: 'test',
     * replicaSetName:'repl' domain:'intra.lan', dbPath: '/data/db', configFile:
     * '/etc/mongodb.conf', logFile: '/var/log/mongodb/mongodb.log',
     * configServerPort:27019, routerPort:27018, dataNodePort:27017,
     * configServers:['py151503651-lxc-mongo1','py151503651-lxc-mongo2','py151503651-lxc-mongo3'],
     * routers: ['py151503651-lxc-mongo4','py151503651-lxc-mongo5'],
     * dataNodes:['py151503651-lxc-mongo6','py151503651-lxc-mongo7','py151503651-lxc-mongo8']
     * } ]
     *
     */
    public String getClusters();

    /**
     * Installs cluster according to specified configuration in json format
     *
     * @param config - specifies cluster configuration e.g. {
     * clusterName:'test', replicaSetName:'repl', numberOfConfigServers:3,
     * numberOfRouters:2, numberOfDataNodes:5, domain:'intra.lan', dbPath:
     * '/data/db', configFile: '/etc/mongodb.conf', logFile:
     * '/var/log/mongodb/mongodb.log', configServerPort:27019, routerPort:27018,
     * dataNodePort:27017 }
     *
     * NOTICE: this will be used when we have dynamic lxc placement feature, for
     * now the config will look like the getClusters' config of one cluster
     *
     * @return - status of installation e.g. {clusterName:'test', status:
     * 'SUCCESS|FAILURE', error:'Cluster with name "test" already exists'}
     *
     */
    public String installCluster(String config);

    /**
     * Uninstalls the specified cluster
     *
     * @param config - specifies cluster configuration e.g. { clusterName:'test'
     * }
     *
     * @return - status of uninstallation e.g. {clusterName:'test', status:
     * 'SUCCESS|FAILURE', error:'Cluster not found'}
     *
     */
    public String uninstallCluster(String config);

    /**
     * Starts the specified node
     *
     * @param config - specifies cluster and node to perform status check on
     * e.g. { clusterName:'test', nodeHostname: 'py151503651-lxc-mongo2' }
     *
     * @return - json string specifying status of the node e.g.
     * {clusterName:'test', nodeHostname: 'py151503651-lxc-mongo2', status:
     * 'STARTED|STOPPED|UNKNOWN', error:'mongo not found' }
     *
     */
    public String startNode(String config);

    /**
     * Stops the specified node
     *
     * @param config - specifies cluster and node to perform status check on
     * e.g. { clusterName:'test', nodeHostname: 'py151503651-lxc-mongo2' }
     *
     * @return - json string specifying status of the node e.g.
     * {clusterName:'test', nodeHostname: 'py151503651-lxc-mongo2', status:
     * 'STARTED|STOPPED|UNKNOWN', error:'mongo not found' }
     *
     */
    public String stopNode(String config);

    /**
     * Checks status of the specified node
     *
     * @param config - specifies cluster and node to perform status check on
     * e.g. { clusterName:'test', nodeHostname: 'py151503651-lxc-mongo2' }
     *
     * @return - json string specifying status of the node e.g.
     * {clusterName:'test', nodeHostname: 'py151503651-lxc-mongo2', status:
     * 'STARTED|STOPPED|UNKNOWN', error:'mongo not found' }
     *
     */
    public String checkNode(String config);
}
