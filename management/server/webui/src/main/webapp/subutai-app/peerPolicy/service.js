"use strict";

angular.module ("subutai.peer-policy.service",[])
	.factory ("PeerPolicySrv", PeerPolicySrv);

PeerPolicySrv.$inject = ["$http"];

function PeerPolicySrv ($http) {
	var PeerPolicySrv = {
		getConfig: getConfig,
		updateConfig: updateConfig
	};

	function getConfig() {
		return $http.get (SERVER_URL + "rest/v1/system/peer_policy", {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function updateConfig (config) {
		var postData = "peerId=" + config.peerId + "&diskUsageLimit=" + config.diskUsageLimit + "&cpuUsageLimit=" + config.cpuUsageLimit + "&memoryUsageLimit=" + config.memoryUsageLimit /*+ "&networkUsageLimit=" + config.networkUsageLimit*/ + "&environmentLimit=" + config.environmentLimit + "&containerLimit=" + config.containerLimit;
		return $http.post(
			SERVER_URL + "",
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	return PeerPolicySrv;
}