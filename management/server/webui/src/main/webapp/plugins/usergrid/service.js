'use strict';

angular.module('subutai.plugins.usergrid.service', [])
	.factory('usergridSrv', usergridSrv);

usergridSrv.$inject = ['$http', 'environmentService'];


function usergridSrv ($http, environmentService) {
	var BASE_URL = SERVER_URL + 'rest/usergrid/';
	var usergridSrv = {
		getEnvironments: getEnvironments,
		build: build,
		listClusters: listClusters,
		getClusterInfo: getClusterInfo,
		uninstallCluster: uninstallCluster
	};
	return usergridSrv;

	function getEnvironments() {
		return environmentService.getEnvironments();
	}



	function build (config) {
		var postData = 'clusterName=' + config.master.hostname + "&userDomain=" + config.userDomain
			+ '&cassandraCSV=' + config.cassandra.join(",")
			+ "&elasticSearchCSV=" + config.elastic.join(",") + "&environmentId=" + config.environment.id;
                console.log (postData);
                return $http.post(
			BASE_URL + 'configure_environment',
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}


	function listClusters() {
		return $http.get (BASE_URL + "clusterList", {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getClusterInfo (cluster) {
		return $http.get (BASE_URL + "clusters/" + cluster.clusterName, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function uninstallCluster (cluster) {
		console.log (cluster);
		return $http.delete (BASE_URL + "clusters/" + cluster.clusterName);
	}
}