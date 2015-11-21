'use strict';

angular.module('subutai.plugins.cassandra.service',[])
	.factory('cassandraSrv', cassandraSrv);

cassandraSrv.$inject = ['$http'];

function cassandraSrv($http) {

	var baseURL = serverUrl + 'cassandra/';
	var cassandraCreateURL = baseURL + 'clusters/create';
	var environmentsURL = serverUrl + 'environments_ui/';	

	var cassandraSrv = {
		getCassandra: getCassandra,
		createCassandra: createCassandra,
		getEnvironments: getEnvironments
	};

	return cassandraSrv;

	function getEnvironments() {
		return $http.get(environmentsURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getCassandra() {
		return $http.get(cassandraUrl);
	}

	function createCassandra(cassandraJson) {
		var postData = 'clusterConfJson=' + cassandraJson;
		return $http.post(
			cassandraCreateURL, 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
