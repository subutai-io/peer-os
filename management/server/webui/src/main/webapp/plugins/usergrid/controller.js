'use strict';

angular.module('subutai.plugins.usergrid.controller', [])
        .controller('UsergridCtrl', UsergridCtrl)
		.directive('mSelect', initMSelect);

UsergridCtrl.$inject = ['usergridSrv', 'SweetAlert', '$scope', 'ngDialog'];


function UsergridCtrl (usergridSrv, SweetAlert, $scope, ngDialog) {
	var vm = this;
	vm.config = {userDomain : ""};
	vm.nodes = [];
	vm.console = "";
	vm.activeTab = "install";
	vm.currentEnvironment = {};
	vm.environments = [];
	vm.currentCluster = {};
	vm.clusters = [];
	vm.cassandras = [];
	vm.elastics = [];

	function getContainers() {
		// TODO: get ip of master if usergrid is already built
		usergridSrv.getEnvironments().success (function (data) {

            vm.environments = [];
            vm.nodes = [];
			vm.cassandras = [];
			vm.elastics = [];

			for (var i = 0; i < data.length; ++i)
			{
				for (var j = 0; j < data[i].containers.length; ++j) {
					if (data[i].containers[j].templateName === "tomcat") {
						vm.environments.push (data[i]);
						break;
					}
				}
			}
			usergridSrv.listClusters().success (function (data) {

				vm.clusters = data;
				vm.currentCluster = vm.clusters[0];
				var temp = [];
				var check = true;
				for (var i = 0; i < vm.environments.length; ++i) {
					for (var j = 0; j < vm.clusters.length; ++j) {
						if (vm.environments[i].id === vm.clusters[j].environmentId) {
							check = false;
							break;
						}
					}
					if (check) {
						temp.push (vm.environments[i]);
					}
				}
				vm.environments = temp;

				if (vm.environments.length === 0) {
					SweetAlert.swal("ERROR!", 'No free environment. Create a new one', "error");
				}
				else {
					vm.currentEnvironment = vm.environments[0];
					for (var i = 0; i < vm.currentEnvironment.containers.length; ++i) {
						if (vm.currentEnvironment.containers[i].templateName === "tomcat") {
							vm.nodes.push (vm.currentEnvironment.containers [i]);
						}

						if (vm.currentEnvironment.containers[i].templateName === "cassandra") {
							vm.cassandras.push (vm.currentEnvironment.containers [i].hostname);
						}

						if (vm.currentEnvironment.containers[i].templateName.indexOf( "elastic" ) >= 0 ) {
							vm.elastics.push (vm.currentEnvironment.containers [i].hostname);
						}
					}
					vm.config.master = vm.nodes[0];
					vm.config.cassandra = [];
					vm.config.elastic = [];
					vm.config.environment = vm.currentEnvironment;
				}
			});
		});
	}


	getContainers();
    vm.changeNodes = changeNodes;
	function changeNodes() {
		vm.nodes = [];
		for (var i = 0; i < vm.currentEnvironment.containers.length; ++i) {
			if (vm.currentEnvironment.containers[i].templateName === "tomcat") {
				vm.nodes.push (vm.currentEnvironment.containers[i]);
			}

			if (vm.currentEnvironment.containers[i].templateName === "cassandra") {
				vm.cassandras.push (vm.currentEnvironment.containers[i].hostname);
			}

			if (vm.currentEnvironment.containers[i].templateName.indexOf( "elastic" ) >= 0) {
				vm.elastics.push (vm.currentEnvironment.containers[i].hostname);
			}
		}
		vm.config.master = vm.nodes[0];
		vm.config.cassandra = [];
		vm.config.elastic = [];
		vm.config.environment = vm.currentEnvironment;
	}

	function listClusters() {
		usergridSrv.listClusters().success (function (data) {
			vm.clusters = data;
			vm.currentCluster = vm.clusters[0];
			var temp = [];
			var check = true;
			for (var i = 0; i < vm.environments.length; ++i) {
				for (var j = 0; j < vm.clusters.length; ++j) {
					if (vm.environments[i].id === vm.clusters[j].environmentId) {
						check = false;
						break;
					}
				}
				if (check) {
					temp.push (vm.environments[i]);
				}
			}
			vm.environments = temp;
			if (vm.environments.length === 0) {
				SweetAlert.swal("ERROR!", 'No free environment. Create a new one', "error");
			}
			else {
				vm.currentEnvironment = vm.environments[0];
				for (var i = 0; i < vm.currentEnvironment.containers.length; ++i) {
					if (vm.currentEnvironment.containers[i].templateName === "tomcat") {
						vm.nodes.push (vm.currentEnvironment.containers [i]);
					}

					if (vm.currentEnvironment.containers[i].templateName === "cassandra") {
						vm.cassandras.push (vm.currentEnvironment.containers [i].hostname);
					}

					if (vm.currentEnvironment.containers[i].templateName.indexOf( "elastic" ) >= 0 ) {
						vm.elastics.push (vm.currentEnvironment.containers [i].hostname);
					}
				}
				vm.config.master = vm.nodes[0];
				vm.config.cassandra = [];
				vm.config.elastic = [];
				vm.config.environment = vm.currentEnvironment;
			}
		});
	}


	function wrongDomain() {
		if (vm.config.userDomain.match (/([a-z]+)\.([a-z][a-z]+)/) === null) {
			return true;
		}
		else {
			return false;
		}
	}

    vm.build = build;
	function build() {
		if (vm.config.userDomain === "") {
			SweetAlert.swal ("ERROR!", 'Please enter domain', "error");
		}
		else if (wrongDomain()) {
			SweetAlert.swal ("ERROR!", 'Wrong domain format', "error");
		}
		else {
			LOADING_SCREEN();
			usergridSrv.build (vm.config).success (function (data) {
				LOADING_SCREEN ('none');
				SweetAlert.swal ("Success!", "Your usergrid cluster was created.", "success");
				listClusters();
			}).error (function (error) {
				LOADING_SCREEN ('none');
				SweetAlert.swal ("ERROR!", 'usergrid build error: ' + error.replace(/\\n/g, ' '), "error");
			});
		}
	}


	vm.getClustersInfo = getClustersInfo;
	function getClustersInfo (selectedCluster) {
		LOADING_SCREEN();
		usergridSrv.getClusterInfo(selectedCluster).success(function (data) {
			LOADING_SCREEN ('none');
			vm.currentCluster = data;
		});
	}

	vm.uninstallCluster = uninstallCluster;
	function uninstallCluster() {
		LOADING_SCREEN();
		console.log (vm.currentCluster);
		usergridSrv.uninstallCluster (vm.currentCluster).success (function (data) {
			LOADING_SCREEN ('none');
			SweetAlert.swal ("Success!", "Your usergrid cluster is being deleted.", "success");
			listClusters();
		}).error (function (error) {
			LOADING_SCREEN ('none');
			SweetAlert.swal ("ERROR!", 'usergrid delete error: ' + error.replace(/\\n/g, ' '), "error");
		});
	}
}

function initMSelect()
{
	var controller = ['$scope', function ($scope) {

		$scope.selected = [];

		$scope.select = function( id )
		{
			$scope.selected.push( id );
		};

		$scope.deselect = function( id )
		{
			var idx = $scope.selected.indexOf(id);
			if( idx > -1 )
			{
				$scope.selected.splice(idx, 1);
			}
		};

		$scope.selectAll = function( )
		{
			$scope.selected = $scope.selected.concat( $scope.items );
		};

		$scope.deselectAll = function( )
		{
			$scope.selected = [];
		};
		}];

	return {
		restrict: 'E',
		scope: {
			items: '=',
			selected: '='
		},
		templateUrl : 'plugins/usergrid/directives/m-select.html',
		controller : controller
	}
}
