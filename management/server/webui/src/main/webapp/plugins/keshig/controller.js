'use strict';

angular.module('subutai.plugins.keshig.controller', [])
    .controller('KeshigCtrl', KeshigCtrl)
	.directive('checkboxListDropdown', checkboxListDropdown);

KeshigCtrl.$inject = ['$scope', 'keshigSrv', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile', 'SweetAlert', 'peerRegistrationService', 'ngDialog'];
function KeshigCtrl($scope, keshigSrv, DTOptionsBuilder, DTColumnBuilder, $resource, $compile, SweetAlert, peerRegistrationService, ngDialog) {
    var vm = this;

	vm.activeTab = 'servers';
	vm.optionType = "CLONE";

	vm.servers = {};
	vm.server2Add = {};
	vm.serverFormUpdate = false;

	vm.serverTypes = [];
	vm.resourceHosts = [];
	vm.servers = {};

	vm.optionsType = [];
	vm.optionsDeployBuilds = [];
	vm.option2Add = {};
	vm.option2Run = '';
	vm.options = {};
	vm.optionFormUpdate = false;

	vm.profiles2Add = {};
	vm.profilesFormUpdate = false;

	vm.serversByType = {};	
	vm.optionsByType = [];
	vm.playbooks = [];

	//functions
	vm.updateOption = updateOption;
	vm.updateServer = updateServer;
	vm.updateProfile = updateProfile;

	vm.deleteServer = deleteServer;
	vm.deleteOption = deleteOption;
	vm.deleteProfile = deleteProfile;

	vm.changeTab = changeTab;
	vm.changeOptionsType = changeOptionsType;
	vm.addServer2From = addServer2From;
	vm.addOption2From = addOption2From;
	vm.runOption = runOption;
	vm.pushPlaybook = pushPlaybook;
	vm.runProfile = runProfile;
	vm.runOptionForm = runOptionForm;

	keshigSrv.getServerTypes().success(function (data) {
		vm.serverTypes = data;
	});

	keshigSrv.getOptionTypes().success(function (data) {
		vm.optionsType = data;
	});

	keshigSrv.getBuilds().success(function (data) {
		for(var i = 0; i < data.length; i++) {
			data[i].dateFormated = dateToFormat(data[i].date);
		}
		vm.optionsDeployBuilds = data;
	});

	keshigSrv.getPlaybooks().success(function (data) {
		vm.playbooks = data;
	});

	function getProfileValues() {
		vm.serversByType = [];
		keshigSrv.getServers().success(function (data) {
			for(var i = 0; i < data.length; i++) {
				if(vm.serversByType[data[i].type] === undefined) vm.serversByType[data[i].type] = [];
				vm.serversByType[data[i].type].push(data[i]);
			}
		});

		keshigSrv.getAllOptions().success(function (data) {
			vm.optionsByType = data;
		});
	}

	peerRegistrationService.getResourceHosts().success(function (data) {
		vm.resourceHosts = [];
		for(var i = 0; i < data.length; i++) {
			if(data[i].hostname != 'management') {
				vm.resourceHosts.push(data[i]);
			}
		}
	});

	function pushPlaybook(playbook) {
		if(vm.option2Add.playbooks === undefined) vm.option2Add.playbooks = [];
		if(vm.option2Add.playbooks.indexOf(playbook) >= 0) {
			vm.option2Add.playbooks.splice(vm.nodes2Action.indexOf(playbook), 1);
		} else {
			vm.option2Add.playbooks.push(playbook);
		}
	}

	function changeTab(tab) {
		vm.activeTab = tab;
		if(vm.activeTab == 'servers') {
			serversTable();
		} else if(vm.activeTab == 'options') {
			getProfileValues();
			changeOptionsType();
		} else if(vm.activeTab == 'profiles') {
			getProfileValues();
			profilesTable();
		} else if(vm.activeTab == 'history') {
			historyTable();
		}
	}

	function changeOptionsType() {
		vm.option2Add = {};

		if(vm.optionType == 'DEPLOY') {
			vm.option2Add.buildName = 'LATEST';
		}

		vm.dtInstance = {};
		vm.dtOptions = DTOptionsBuilder
			.fromFnPromise(function() {
				return $resource( keshigSrv.getOptionsUrl() + 'type/' + vm.optionType ).query().$promise;
			})
			.withPaginationType('full_numbers')
			.withOption('stateSave', true)
			.withOption('order', [[ 1, "asc" ]])
			.withOption('createdRow', createdRow);

		vm.dtColumns = getOptionTableCol();
	}

	function serversTable() {
		vm.dtInstance = {};
		vm.dtOptions = DTOptionsBuilder
			.fromFnPromise(function() {
				return $resource(keshigSrv.getServersUrl()).query().$promise;
			})
			.withPaginationType('full_numbers')
			.withOption('stateSave', true)
			.withOption('order', [[ 1, "asc" ]])
			.withOption('createdRow', createdRow);

		vm.dtColumns = [
			DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionEditServer),
			DTColumnBuilder.newColumn('serverName').withTitle('Name'),
			DTColumnBuilder.newColumn('type').withTitle('Type'),
			DTColumnBuilder.newColumn('serverAddress').withTitle('Address'),
			DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(deleteAction)
		];
	}
	serversTable();

	function profilesTable() {
		vm.dtInstance = {};
		vm.dtOptions = DTOptionsBuilder
			.fromFnPromise(function() {
				return $resource(keshigSrv.getProfilesUrl()).query().$promise;
			})
			.withPaginationType('full_numbers')
			.withOption('stateSave', true)
			.withOption('order', [[ 0, "asc" ]])
			.withOption('createdRow', createdRow);

		vm.dtColumns = [
			//DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionEditProfile),
			DTColumnBuilder.newColumn('name').withTitle('Name'),
			DTColumnBuilder.newColumn('cloneOption').withTitle('Clone').notSortable().renderWith(profileCloneButton),
			DTColumnBuilder.newColumn('buildOption').withTitle('Build').notSortable().renderWith(profileBuildButton),
			DTColumnBuilder.newColumn('testOption').withTitle('Test').notSortable().renderWith(profileTestButton),
			DTColumnBuilder.newColumn('deployOption').withTitle('Deploy').notSortable().renderWith(profileDeployButton),
			DTColumnBuilder.newColumn('name').withTitle('').notSortable().renderWith(runProfileButton),
			DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(deleteAction)
		];
	}

	function historyTable() {
		vm.dtInstance = {};
		vm.dtOptions = DTOptionsBuilder
			.fromFnPromise(function() {
				return $resource(keshigSrv.getHistoryUrl()).query().$promise;
			})
			.withPaginationType('full_numbers')
			.withOption('stateSave', true)
			.withOption('order', [[ 2, "desc" ]])
			.withOption('createdRow', createdRow);

		vm.dtColumns = [
			DTColumnBuilder.newColumn('type').withTitle('Type'),
			DTColumnBuilder.newColumn('server').withTitle('Server'),
			DTColumnBuilder.newColumn('startTime').withTitle('Start time').renderWith(dateToFormat),
			DTColumnBuilder.newColumn('endTime').withTitle('End time').renderWith(dateToFormat),
			DTColumnBuilder.newColumn(null).withTitle('Results').renderWith(renderHistoryOutput)
		];
	}

	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

	function actionEditServer(data, type, full, meta) {
		vm.servers[data.serverName] = data;
		return '<a href class="b-icon b-icon_edit" ng-click="keshigCtrl.addServer2From(keshigCtrl.servers[\'' + data.serverName + '\'])"></a>';
	}

	function actionEditOption(data, type, full, meta) {
		vm.options[data.name] = data;
		return '<a href class="b-icon b-icon_edit" ng-click="keshigCtrl.addOption2From(keshigCtrl.options[\'' + data.name + '\'])"></a>';
	}

	function actionEditProfile(data, type, full, meta) {
		vm.profiles[data.name] = data;
		return '<a href class="b-icon b-icon_edit" ng-click="keshigCtrl.addProfile2From(keshigCtrl.options[\'' + data.name + '\'])"></a>';
	}

	function renderHistoryOutput(data, type, full, meta) {
		var contentOutput = '';
		if(data.stdOut === undefined || data.stdOut == null || data.stdOut.length < 1) {
			contentOutput = data.stdErr;
		} else {
			if(data.type == 'TEST') {
				contentOutput = '<a href="' + getBaseUrl() + ':80' + data.stdOut + '" target="_blank">' + data.stdOut + '</a>';
			} else {
				contentOutput = data.stdOut;
			}
		}
		return contentOutput;
	}

	function dateToFormat(data, type, full, meta) {
		return dateToFormat(data);
	}

	function playbooksTags(data, type, full, meta) {
		var playbooksHTML = '';
		if(data !== undefined && data !== null && data.length > 0) {
			for(var i = 0; i < data.length; i++) {
				playbooksHTML += '<span class="b-tags">' + data[i] + '</span>';
			}
		}
		return playbooksHTML;
	}

	function optionStatusIcon(data, type, full, meta) {
		return '<div style="width: 100%; text-align: center;"><div class="b-status-icon b-status-icon_' + data + '" tooltips tooltip-title="' + data + '"></div></div>';
	}

	function runOptionButton(data, type, full, meta) {
		return '<a href class="b-btn b-btn_green" ng-click="keshigCtrl.runOptionForm(\'' + data.name + '\')">Run</a>';
	}

	function profileBuildButton(data, type, full, meta) {
		if(data !== undefined && data !== null) {
			return '<a href class="b-btn b-btn_blue" ng-click="keshigCtrl.runOption(\'' + data + '\', \'build\')">Build</a>';
		} else {
			return 'Empty';
		}
	}

	function profileTestButton(data, type, full, meta) {
		if(data !== undefined && data !== null) {
			return '<a href class="b-btn b-btn_blue" ng-click="keshigCtrl.runOption(\'' + data + '\', \'test\')">Test</a>';
		} else {
			return 'Empty';
		}
	}

	function profileDeployButton(data, type, full, meta) {
		if(data !== undefined && data !== null) {
			return '<a href class="b-btn b-btn_blue" ng-click="keshigCtrl.runOption(\'' + data + '\', \'deploy\')">Deploy</a>';
		} else {
			return 'Empty';
		}
	}

	function profileCloneButton(data, type, full, meta) {
		if(data !== undefined && data !== null) {
			return '<a href class="b-btn b-btn_blue" ng-click="keshigCtrl.runOption(\'' + data + '\', \'clone\')">Clone</a>';
		} else {
			return 'Empty';
		}
	}

	function runProfileButton(data, type, full, meta) {
		return '<a href class="b-btn b-btn_green" ng-click="keshigCtrl.runProfile(\'' + data + '\')">Run</a>';
	}

	function deleteAction(data, type, full, meta) {

		var action = 'deleteServer';
		var deleteId = data.serverName;
		if(vm.activeTab == 'options') {
			action = 'deleteOption';
			deleteId = data.name;
		} else if(vm.activeTab == 'profiles') {
			action = 'deleteProfile';
			deleteId = data.name;
		}

		return '<a href class="b-icon b-icon_remove" ng-click="keshigCtrl.' + action + '(\'' + deleteId + '\')"></a>';
	}

	function runProfile(profileName) {
		keshigSrv.startProfile( profileName ).success(function(data){
			SweetAlert.swal("Success!", '"' + profileName + '" profile start running.', "success");
		}).error(function (data) {
			SweetAlert.swal("ERROR!", '"' + profileName + '" profile run error. Error: ' + data.ERROR, 'error');
		});
	}

	function addServer2From(server) {
		vm.server2Add = server;
		vm.server2Add.serverType = server.type;
		vm.serverFormUpdate = true;
	}

	function addOption2From(option) {
		vm.option2Add = option;
		vm.optionFormUpdate = true;
	}

	function addProfile2From(profile) {
		vm.profiles2Add = profile;
		vm.profilesFormUpdate = true;
	}

	function runOptionForm(optionName) {
		vm.servers2Test = [];		
		if(vm.optionType == 'DEPLOY') {
			vm.servers2Test = vm.serversByType['DEPLOY_SERVER'];
		} else if(vm.optionType == 'TEST') {
			vm.servers2Test = vm.serversByType['TEST_SERVER'];
		} else {
			vm.servers2Test = vm.serversByType['BUILD_SERVER'];
		}
		vm.option2Run = optionName;

		ngDialog.open({
			template: 'plugins/keshig/partials/options/optionServerSelectPopup.html',
			scope: $scope
		});
	}

	function runOption(optionName, customOptionType, server) {
		if(customOptionType === undefined || customOptionType === null) customOptionType = vm.optionType;
		if(server === undefined || server === null) server = '';
		keshigSrv.startOption( customOptionType, optionName, server ).success(function(data){
			SweetAlert.swal("Success!", '"' + optionName + '" option start running.', "success");
			//vm.dtInstance.reloadData(null, false);
		}).error(function (data) {
			SweetAlert.swal("ERROR!", '"' + optionName + '" option run error. Error: ' + data.ERROR, 'error');
		});
		ngDialog.closeAll();
	}

	function updateOption() {

		if(vm.optionType == 'TEST') {
			if(vm.option2Add.latest === true) {
				vm.option2Add.targetIps = ['LATEST'];
			} else if(vm.option2Add.targetIps !== undefined) {
				var targetIps = vm.option2Add.targetIps.split(',');
				vm.option2Add.targetIps = targetIps;
			}
		}

		if( vm.optionFormUpdate ) {
			vm.optionFormUpdate = false;
			keshigSrv.updateOption( vm.optionType, JSON.stringify(vm.option2Add) ).success(function(data){
				SweetAlert.swal("Success!", vm.optionType + " option successfully updated.", "success");
				vm.dtInstance.reloadData(null, false);
			}).error(function (data) {
				SweetAlert.swal("ERROR!", vm.optionType + " option update error. Error: " + data.ERROR, "error");
			});
		} else {
			keshigSrv.addOption( vm.optionType, JSON.stringify(vm.option2Add) ).success(function(data){
				SweetAlert.swal("Success!", vm.optionType + " option successfully added.", "success");
				vm.dtInstance.reloadData(null, false);
			}).error(function (data) {
				SweetAlert.swal("ERROR!", vm.optionType + " option add error. Error: " + data.ERROR, "error");
			});
		}
		vm.option2Add = {};
	}

	function deleteProfile(profileName) {
		if(profileName === undefined) return;
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Delete profile " + profileName + "!",
			type: "warning",
			showCancelButton: true,
			confirmButtonColor: "#ff3f3c",
			confirmButtonText: "Delete",
			cancelButtonText: "Cancel",
			closeOnConfirm: false,
			closeOnCancel: true,
			showLoaderOnConfirm: true
		},
		function (isConfirm) {
			if (isConfirm) {
				keshigSrv.removeProfile(profileName).success(function (data) {
					SweetAlert.swal("Deleted!", "Your profile has been deleted.", "success");
					vm.dtInstance.reloadData(null, false);
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Your profile is safe. Error: " + data.ERROR, "error");
				});
			}
		});
	}

	function deleteOption( optionName )	{
		if(optionName === undefined) return;
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Delete option " + optionName + "!",
			type: "warning",
			showCancelButton: true,
			confirmButtonColor: "#ff3f3c",
			confirmButtonText: "Delete",
			cancelButtonText: "Cancel",
			closeOnConfirm: false,
			closeOnCancel: true,
			showLoaderOnConfirm: true
		},
		function (isConfirm) {
			if (isConfirm) {
				keshigSrv.deleteOption(vm.optionType, optionName).success(function (data) {
					SweetAlert.swal("Deleted!", "Your option has been deleted.", "success");
					vm.dtInstance.reloadData(null, false);
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Your option is safe. Error: " + data.ERROR, "error");
				});
			}
		});
	}

	function deleteServer(serverName)	{
		if(serverName === undefined) return;
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Delete server " + serverName + "!",
			type: "warning",
			showCancelButton: true,
			confirmButtonColor: "#ff3f3c",
			confirmButtonText: "Delete",
			cancelButtonText: "Cancel",
			closeOnConfirm: false,
			closeOnCancel: true,
			showLoaderOnConfirm: true
		},
		function (isConfirm) {
			if (isConfirm) {
				keshigSrv.removeServer(serverName).success(function (data) {
					SweetAlert.swal("Deleted!", "Your server has been deleted.", "success");
					vm.dtInstance.reloadData(null, false);
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Your server is safe. Error: " + data.ERROR, "error");
				});
			}
		});
	}

	function updateServer( id )	{
		if(vm.server2Add.serverName === undefined) return;
		if(vm.server2Add.serverId === undefined) return;
		if(vm.server2Add.serverType === undefined) return;

		if( vm.serverFormUpdate ) {
			vm.serverFormUpdate = false;
			keshigSrv.updateServer( vm.server2Add ).success(function(data){
				SweetAlert.swal("Success!", "Keshig server successfully updated.", "success");
				vm.dtInstance.reloadData(null, false);
			}).error(function (data) {
				SweetAlert.swal("ERROR!", "Keshig server update error. Error: " + data.ERROR, "error");
			});
		} else {
			keshigSrv.addServer( vm.server2Add ).success(function(data){
				SweetAlert.swal("Success!", "Keshig server successfully added.", "success");
				vm.dtInstance.reloadData(null, false);
			}).error(function (data) {
				SweetAlert.swal("ERROR!", "Keshig server add error. Error: " + data.ERROR, "error");
			});
		}
		vm.server2Add = {};
	}

	function updateProfile() {
		console.log(vm.profiles2Add);
		if( vm.profilesFormUpdate ) {
			vm.profilesFormUpdate = false;
			keshigSrv.updateProfile( JSON.stringify(vm.profiles2Add) ).success(function(data){
				SweetAlert.swal("Success!", vm.profiles2Add.name + " profile successfully updated.", "success");
				vm.dtInstance.reloadData(null, false);
			}).error(function (data) {
				SweetAlert.swal("ERROR!", vm.profiles2Add.name + " profile update error. Error: " + data.ERROR, "error");
			});
		} else {
			keshigSrv.addProfile( JSON.stringify(vm.profiles2Add) ).success(function(data){
				SweetAlert.swal("Success!", vm.profiles2Add.name + " profile successfully added.", "success");
				vm.dtInstance.reloadData(null, false);
			}).error(function (data) {
				SweetAlert.swal("ERROR!", vm.profiles2Add.name + " profile add error. Error: " + data.ERROR, "error");
			});
		}
		//vm.profiles2Add = {};
	}

	function getOptionTableCol() {
		switch(vm.optionType) {
			case 'CLONE':
				return [
					DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionEditOption),
					DTColumnBuilder.newColumn('name').withTitle('Name'),
					DTColumnBuilder.newColumn('url').withTitle('URL'),
					DTColumnBuilder.newColumn('branch').withTitle('Branch'),
					DTColumnBuilder.newColumn('timeOut').withTitle('TimeOut'),
					DTColumnBuilder.newColumn('active').withTitle('Active').renderWith(optionStatusIcon),
					DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(runOptionButton),
					DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(deleteAction)
				];
			case 'DEPLOY':
				return [
					DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionEditOption),
					DTColumnBuilder.newColumn('name').withTitle('Name'),
					DTColumnBuilder.newColumn('timeOut').withTitle('TimeOut'),
					DTColumnBuilder.newColumn('active').withTitle('Active').renderWith(optionStatusIcon),
					DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(runOptionButton),
					DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(deleteAction)
				];
			case 'TEST':
				return [
					DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionEditOption),
					DTColumnBuilder.newColumn('name').withTitle('Name'),
					DTColumnBuilder.newColumn('timeOut').withTitle('TimeOut'),
					DTColumnBuilder.newColumn('playbooks').withTitle('Playbooks').renderWith(playbooksTags),
					DTColumnBuilder.newColumn('active').withTitle('Active').renderWith(optionStatusIcon),
					DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(runOptionButton),
					DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(deleteAction)
				];
			case 'BUILD':
				return [
					DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionEditOption),
					DTColumnBuilder.newColumn('name').withTitle('Name'),
					DTColumnBuilder.newColumn('timeOut').withTitle('TimeOut'),
					DTColumnBuilder.newColumn('runTests').withTitle('Run Tests').renderWith(optionStatusIcon),
					DTColumnBuilder.newColumn('cleanInstall').withTitle('Clean Install').renderWith(optionStatusIcon),
					DTColumnBuilder.newColumn('active').withTitle('Active').renderWith(optionStatusIcon),
					DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(runOptionButton),
					DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(deleteAction)
				];
		}
	}

	function dateToFormat(date) {
		if(date === undefined || date === null) return 'In progress';
		var dateFormat = new Date(date);
		return dateFormat.getMonth() + '/' 
			+ dateFormat.getDate() + '/' 
			+ dateFormat.getFullYear() + ' ' 
			+ dateFormat.getHours() + ':' + dateFormat.getMinutes() + ':' + dateFormat.getSeconds();
	}

	function getBaseUrl() {
		var pathArray = location.href.split( '/' );
		//var protocol = pathArray[0];
		var protocol = 'http:';
		var hostWithPort = pathArray[2].split(':');
		var host = hostWithPort[0];
		var url = protocol + '//' + host;
		return url;
	}
}

function checkboxListDropdown() {
	return {
		restrict: 'A',
		link: function(scope, element, attr) {
			$(".b-form-input_dropdown").click(function () {
				$(this).toggleClass("is-active");
			});

			$(".b-form-input-dropdown-list").click(function(e) {
				e.stopPropagation();
			});
		}
	}
};

