"use strict";

angular.module("subutai.settings-kurjun.controller", [])
    .controller("SettingsKurjunCtrl", SettingsKurjunCtrl);

SettingsKurjunCtrl.$inject = ['$scope', 'SettingsKurjunSrv', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile', 'ngDialog'];


function SettingsKurjunCtrl($scope, SettingsKurjunSrv, SweetAlert, DTOptionsBuilder, DTColumnBuilder, $resource, $compile, ngDialog) {
    var vm = this;
    vm.config = {globalKurjunUrls: [""]};
    vm.activeTab = "urls";
    vm.uid = '';
    vm.currentUrl = '';
    vm.currentType = '';
    vm.currentId = '';

    vm.urlsType = {
        1: "Local",
        2: "Global",
        3: "Custom"
    };

    //functions
    vm.urlFrom = urlFrom;
    vm.editUrl = editUrl;
    vm.approveUrl = approveUrl;
    vm.updateConfigQuotas = updateConfigQuotas;
    vm.updateConfigUrls = updateConfigUrls;
    vm.addGlobalUrl = addGlobalUrl;
    vm.removeGlobalUrl = removeGlobalUrl;
    vm.removeLocalUrl = removeLocalUrl;
    vm.autoSign = autoSign;
    vm.addUrl = addUrl;
    vm.currentUrlObject = {};
    vm.previousName = "";
    vm.updateUrl = updateUrl;
    vm.urlList = [];

    function getConfig() {
        SettingsKurjunSrv.getConfig().success(function (data) {
            vm.config = data;
        });
    }

    getConfig();


    vm.dtInstance = {};
    vm.dtOptions = DTOptionsBuilder
        .fromFnPromise(function () {
            return $resource(SettingsKurjunSrv.getUrlsListUrl()).query().$promise;
        })
        .withPaginationType('full_numbers')
        .withOption('stateSave', true)
        .withOption('order', [[0, "desc"]])
        .withOption('createdRow', createdRow);

    vm.dtColumns = [
        DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionEdit),
        DTColumnBuilder.newColumn('id').withTitle('ID'),
        DTColumnBuilder.newColumn('url').withTitle('URL'),
        DTColumnBuilder.newColumn('type').withTitle('Type').renderWith(getUrlType),
        DTColumnBuilder.newColumn('state').withTitle('State').renderWith(getState),
        DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionApprove)
    ];

    function createdRow(row, data, dataIndex) {
        $compile(angular.element(row).contents())($scope);
    }

    function getUrlType(type) {
        return vm.urlsType[type];
    }

    function getState(state) {
        var result = '<span class="b-tags b-tags_green">Registered</span>';
        if (state == false) {
            result = '<span class="b-tags b-tags_red">Not registered</span>';
        }
        return result;
    }

    function actionEdit(data, type, full, meta) {
        var result = '<span ></span>';
        if (data.state == false) {
            result = '<a href class="b-icon b-icon_edit" ng-click="settingsKurjunCtrl.editUrl( ' + data + ' )"></a>';
        }

        return result;
    }


    function actionApprove(data, type, full, meta) {
        var approveButton = '<span ></span>';
        if (data.state == false) {
            approveButton = '<a href class="b-btn b-btn_green" ng-click="settingsKurjunCtrl.approveUrl(\'' + data.id + '\', \'' + data.type + '\', \'' + data.id + '\')">Register</a>';
        }
        return approveButton;
    }

    function editUrl(data) {
        vm.currentUrl = url;
        vm.currentType = type;
        vm.currentId = id;
        ngDialog.open({
            template: "subutai-app/settingsKurjun/partials/editUrl.html",
            scope: $scope
        });
    }


    function approveUrl(url, type, id) {
        vm.currentUrl = url;
        vm.currentType = type;
        vm.currentId = id;
        LOADING_SCREEN();
        SettingsKurjunSrv.registerUrl(id).success(function (data) {
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
        }).error(function (error) {
            SweetAlert.swal("ERROR!", "Register URL error: " + error.replace(/\\n/g, " "), "error");
            LOADING_SCREEN('none');
        });
    }

    function autoSign() {
        LOADING_SCREEN();
        SettingsKurjunSrv.signedMsg(vm.currentId, vm.uid).success(function (data) {
            if (Object.keys(vm.dtInstance).length !== 0) {
                vm.dtInstance.reloadData(null, false);
            }
            LOADING_SCREEN('none');
            SweetAlert.swal("Success!", "URL was successfully registered.", "success");
        }).error(function (error) {
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


    function urlFrom() {
        ngDialog.open({
            template: 'subutai-app/settingsKurjun/partials/urlForm.html',
            scope: $scope
        });
    }

    function updateUrl() {
        if (vm.previousName !== vm.currentUrlObject.name) {
            // if (checkIfExists (vm.currentUrlObject)) {
            //     SweetAlert.swal ("ERROR!", "Operation already exists", "error");
            //     return;
            // }
        }
        if (vm.currentUrlObject.name === "" || vm.currentUrlObject.name === undefined) {
            SweetAlert.swal("ERROR!", "Please enter operation name", "error");
        }
        genericSrv.updateOperation(vm.currentOperation).success(function (data) {
            SweetAlert.swal("Success!", "Your operation was updated.", "success");
            vm.getOperations();
            ngDialog.closeAll();
        }).error(function (error) {
            SweetAlert.swal("ERROR!", "Operation update error: " + error.replace(/\\n/g, " "), "error");
        });
    }

    // function checkIfExists(urlObject) {
    //     var arr = [];
    //     for (var i = 0; i < vm.operations.length; ++i) {
    //         arr.push (vm.operations.operationName);
    //     }
    //     if (arr.indexOf (operation.operationName) > -1) {
    //         return true;
    //     }
    //     return false;
    // }


    function addUrl(newUrl) {
        var postData = 'url=' + newUrl.name + '&type=' + newUrl.type;
        LOADING_SCREEN();
        ngDialog.closeAll();
        SettingsKurjunSrv.addUrl(postData).success(function (data) {
            LOADING_SCREEN('none');
            if (Object.keys(vm.dtInstance).length !== 0) {
                vm.dtInstance.reloadData(null, false);
            }
        }).error(function (error) {
            LOADING_SCREEN('none');
            SweetAlert.swal("ERROR!", "Error in saving URL: " + error, "error");
        });
    }
}

