"use strict";

angular.module("subutai.settings-kurjun.controller", [])
    .controller("SettingsKurjunCtrl", SettingsKurjunCtrl);


SettingsKurjunCtrl.$inject = ['$scope', 'SettingsKurjunSrv', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile'];
function SettingsKurjunCtrl($scope, SettingsKurjunSrv, SweetAlert, DTOptionsBuilder, DTColumnBuilder, $resource, $compile) {
    var vm = this;
    vm.config = {globalKurjunUrls: [""]};
	vm.activeTab = "urls";
	vm.uid = '';
	vm.currentUrl = '';
	vm.currentType = '';

	vm.urlsType = {
		1: "Local",
		2: "Global",
		3: "Custom"
	};

	//functions
	vm.approveUrl = approveUrl;
    vm.updateConfigQuotas = updateConfigQuotas;
    vm.updateConfigUrls = updateConfigUrls;
    vm.addGlobalUrl = addGlobalUrl;
    vm.removeGlobalUrl = removeGlobalUrl;
    vm.removeLocalUrl = removeLocalUrl;
    vm.autoSign = autoSign;

    function getConfig() {
        SettingsKurjunSrv.getConfig().success(function (data) {
            vm.config = data;
    	});
    }
    getConfig();


	vm.dtInstance = {};
	vm.dtOptions = DTOptionsBuilder
		.fromFnPromise(function() {
			return $resource( SettingsKurjunSrv.getUrlsListUrl() ).query().$promise;
		})
		.withPaginationType('full_numbers')
		.withOption('stateSave', true)
		.withOption('order', [[ 0, "desc" ]])
		.withOption('createdRow', createdRow);

	vm.dtColumns = [
		DTColumnBuilder.newColumn('id').withTitle('ID'),
		DTColumnBuilder.newColumn('url').withTitle('URL'),
		DTColumnBuilder.newColumn('type').withTitle('Type').renderWith(getUrlType),
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionApprove)
	];

	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

	function getUrlType(type) {
		return vm.urlsType[type];
	}

	function actionApprove(data, type, full, meta) {
		var approveButton = '<span class="b-tags b-tags_green">Registered</span>';
		if(data.state == false) {
			approveButton = '<a href class="b-btn b-btn_green" ng-click="settingsKurjunCtrl.approveUrl(\'' + data.url + '\', \'' + data.type + '\')">Register</a>';
		}
		return approveButton;
	}

	function approveUrl(url, type) {
		vm.currentUrl = url;
		vm.currentType = type;		
		LOADING_SCREEN();
		SettingsKurjunSrv.registerUrl(url, type).success(function (data) {
			vm.uid = data;
			if (vm.uid) {
				//$('#js-uid-sign-area').addClass('bp-sign-target');
				var textarea = angular.element('<textarea class="bp-sign-target" ng-model="settingsKurjunCtrl.uid" ng-change="settingsKurjunCtrl.autoSign()"></textarea>');
				$('#js-uid-sign-area').append(textarea);
				$compile(textarea)($scope);
			} else {
				SweetAlert.swal("ERROR!", "Register URL error: UID is empty", "error");
				LOADING_SCREEN('none');
			}
		}).error(function(error) {
			SweetAlert.swal("ERROR!", "Register URL error: " + error.replace(/\\n/g, " "), "error");
			LOADING_SCREEN('none');
		});
	}

	function autoSign() {
		LOADING_SCREEN();
		SettingsKurjunSrv.signedMsg(vm.currentUrl, vm.currentType, vm.uid).success(function (data) {
			if(Object.keys(vm.dtInstance).length !== 0) {
				vm.dtInstance.reloadData(null, false);
			}
			LOADING_SCREEN('none');
			SweetAlert.swal("Success!", "URL was successfully authorized.", "success");
		}).error(function(error) {
			SweetAlert.swal("ERROR!", "Register URL error: " + error.replace(/\\n/g, " "), "error");
			LOADING_SCREEN('none');
			console.log(error);
		});
		resetSignField();
	}

	function resetSignField() {
		vm.uid = '';
		vm.currentUrl = '';
		vm.currentType = '';
		$('#js-uid-sign-area').find('textarea').remove();
	}

    function updateConfigQuotas() {
        SettingsKurjunSrv.updateConfigQuotas(vm.config).success(function (data) {
            SweetAlert.swal("Success!", "Your settings were saved.", "success");
        }).error(function (error) {
            SweetAlert.swal("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
        });
    }

    function updateConfigUrls() {
        SettingsKurjunSrv.updateConfigUrls(vm.config).success(function (data) {
            SweetAlert.swal("Success!", "Your settings were saved.", "success");
        }).error(function (error) {
            SweetAlert.swal("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
        });
    }



    function addGlobalUrl() {
        vm.config.globalKurjunUrls.push("");
    }

    vm.addLocalUrl = addLocalUrl;
    function addLocalUrl() {
        vm.config.localKurjunUrls.push("");
    }


    function removeGlobalUrl(index) {
        if (vm.config.globalKurjunUrls.length !== 1) {
            vm.config.globalKurjunUrls.splice(index, 1);
        }
        else {
            vm.config.globalKurjunUrls[0] = "";
        }
    }

    function removeLocalUrl(index) {
        if (vm.config.localKurjunUrls.length !== 1) {
            vm.config.localKurjunUrls.splice(index, 1);
        }
        else {
            vm.config.localKurjunUrls[0] = "";
        }
    }



}
