"use strict";

angular.module ("subutai.settings-peer.service",[])
	.factory ("SettingsPeerSrv", SettingsPeerSrv);

SettingsPeerSrv.$inject = ["$http"];

function SettingsPeerSrv ($http) {
	var SettingsPeerSrv = {
		getRelations: getRelations
	};

	function getRelations() {
		return $http.get (SERVER_URL + "", {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	return SettingsPeerSrv;
}