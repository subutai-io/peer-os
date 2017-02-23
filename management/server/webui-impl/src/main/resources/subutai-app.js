var app = angular.module('subutai-app', [
    'ui.router',
    'ngCookies',
    'ngResource',
    'oc.lazyLoad',
    'oitozero.ngSweetAlert',
    'ngDialog',
    'datatables',
    '720kb.tooltips',
    'ngTagsInput',
    'nvd3',
    'cfp.loadingBar',
    'uiSwitch',
    'ngFileUpload',
    'subutai.nav-menu'
])
    .config(routesConf)

    .controller('SubutaiController', SubutaiController)
    .controller('CurrentUserCtrl', CurrentUserCtrl)

    .controller('AccountCtrl', AccountCtrl)
    .factory('identitySrv', identitySrv)

    .factory('trackerSrv', trackerSrv)

    .run(startup);

CurrentUserCtrl.$inject = ['$location', '$scope', '$rootScope', '$http', 'SweetAlert', 'ngDialog', 'trackerSrv', 'identitySrv'];
routesConf.$inject = ['$httpProvider', '$stateProvider', '$urlRouterProvider', '$ocLazyLoadProvider'];
startup.$inject = ['$rootScope', '$state', '$location', '$http', 'SweetAlert', 'ngDialog'];

function CurrentUserCtrl($location, $scope, $rootScope, $http, SweetAlert, ngDialog, trackerSrv, identitySrv) {

    var vm = this;
    vm.currentUser = localStorage.getItem('currentUser');
    vm.hubStatus = false;
    vm.userId = "";
    vm.userEmail = "";
    vm.notifications = [];
    vm.notificationsCount = 0;
    vm.notificationNew = false;
    vm.notificationsLogs = [];
    vm.currentLogTitle = '';
    vm.currentLog = [];
    vm.currentUserRoles = [];
    $rootScope.notifications = {};
    vm.hubRegisterError = false;
    vm.isRegistrationFormVisible = false;
    vm.peerNameValue = false;

    function updateHubIp() {
        $http.get(SERVER_URL + 'rest/v1/system/hub_ip', {
            withCredentials: true,
            headers: {'Content-Type': 'application/json'}
        }).success(function (data) {
            vm.hubIp = data;
        }).error(function (error) {
            vm.hubIp = 'hub.subut.ai';
        });
    }

    updateHubIp();

    setInterval(updateHubIp, 30000);

    vm.getRegistrationFormVisibilityStatus = function () {
        return vm.isRegistrationFormVisible;
    };

    if ((localStorage.getItem('currentUser') == undefined || localStorage.getItem('currentUser') == null
        || localStorage.getItem('currentUserToken') != getCookie('sptoken')) && getCookie('sptoken')) {
        console.log("get login details");
        $http.get(SERVER_URL + "rest/ui/identity/user", {
            withCredentials: true,
            headers: {'Content-Type': 'application/json'}
        }).success(function (data) {

            localStorage.removeItem('currentUser');
            localStorage.removeItem('currentUserPermissions');
            localStorage.removeItem('currentUserToken');

            localStorage.setItem('currentUser', data.userName);
            localStorage.setItem('currentUserToken', getCookie('sptoken'));

            var perms = [];
            for (var i = 0; i < data.roles.length; i++) {
                for (var j = 0; j < data.roles[i].permissions.length; j++) {
                    perms.push(data.roles[i].permissions[j].object);
                }
            }

            localStorage.setItem('currentUserPermissions', perms);
            vm.currentUser = localStorage.getItem('currentUser');

            location.reload();
        });
    } else {
        vm.currentUser = localStorage.getItem('currentUser');
    }

    function checkIfRegistered(afterRegistration) {
        if (afterRegistration === undefined || afterRegistration === null) afterRegistration = false;
        $http.get(SERVER_URL + "rest/v1/hub/registration_state", {
            withCredentials: true,
            headers: {'Content-Type': 'application/json'}
        }).success(function (data) {

            vm.hubStatus = data.isRegisteredToHub;
            vm.userId = data.ownerId;
            vm.userEmail = data.currentUserEmail;

            if (vm.hubStatus != "true" && vm.hubStatus != true) {
                vm.hubStatus = false;
            } else {
                vm.hubStatus = true;
				vm.peerNameValue = data.peerName;
            }
			hubRegisterStatus = vm.hubStatus;

            if (afterRegistration) {
                hubPopupLoadScreen();
                ngDialog.open({
                    template: 'subutai-app/common/partials/hubSuccessMessage.html',
                    scope: $scope
                });
            }
        });
    }

    checkIfRegistered();

    setInterval(function () {
        checkIfRegistered();
    }, 120000);


    vm.hub = {
        login: "",
        password: "",
        peerName: "",
    };


    //function
    vm.logout = logout;
    vm.hubRegister = hubRegister;
    vm.hubUnregister = hubUnregister;
    vm.hubHeartbeat = hubHeartbeat;
    vm.clearLogs = clearLogs;
    vm.viewLogs = viewLogs;


    function hubPopupLoadScreen(show) {
        if (show == undefined || show == null) show = false;
        if (show) {
            $('.js-hub-screen').show();
        } else {
            $('.js-hub-screen').hide();
        }
    }


    function hubRegister() {
        vm.hubRegisterError = false;
        hubPopupLoadScreen(true);

        var postData = 'email=' + vm.hub.login + '&peerName=' + vm.hub.peerName + '&password=' + encodeURIComponent(vm.hub.password);
        $http.post(SERVER_URL + 'rest/v1/hub/register', postData, {
            withCredentials: true,
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        })
            .success(function () {

                checkIfRegistered(true);

                $http.post(SERVER_URL + 'rest/v1/hub/send-heartbeat', {
                    withCredentials: true,
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'}
                })
                    .success(function () {
                    }).error(function (error) {
                    console.log('hub/register error: ', error);
                    vm.hubRegisterError = error;
                });

                $http.post(SERVER_URL + 'rest/v1/hub/send-rh-configurations', {
                    withCredentials: true,
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'}
                })
                    .success(function () {
                    }).error(function (error) {
                    console.log('hub/register error: ', error);
                    vm.hubRegisterError = error;
                });

				vm.peerNameValue = vm.hub.peerName;

            }).error(function (error) {
            console.log('hub/register error: ', error);
            vm.hubRegisterError = error;
            hubPopupLoadScreen();
        });
    }


    function hubUnregister() {
        hubPopupLoadScreen(true);
        $http.delete(SERVER_URL + 'rest/v1/hub/unregister')
            .success(function () {
                hubPopupLoadScreen();
                vm.hubStatus = false;
				vm.peerNameValue = false;
                //SweetAlert.swal ("Success!", "Your peer was unregistered from Hub.", "success");
            }).error(function (error) {
            hubPopupLoadScreen();
            SweetAlert.swal("ERROR!", error, "error");
        });
    }


    function hubHeartbeat() {
        //should be rest/v1/hub no need to change
        hubPopupLoadScreen(true);
        $http.post(SERVER_URL + 'rest/v1/hub/send-heartbeat', {withCredentials: true})
            .success(function () {
                hubPopupLoadScreen();
                vm.hubStatus = true;
                SweetAlert.swal("Success!", "Heartbeat sent successfully.", "success");
            }).error(function (error) {
            hubPopupLoadScreen();
            SweetAlert.swal("ERROR!", "Error performing heartbeat: " + error.replace(/\\n/g, " "), "error");
        });
    }

    function logout() {
        removeCookie('sptoken');
        localStorage.removeItem('kurjunToken');
        localStorage.removeItem('currentUserToken');
        $location.path('login');
    }

    $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
        if (localStorage.getItem('currentUser') !== undefined) {
            vm.currentUser = localStorage.getItem('currentUser');
        } else if ($rootScope.currentUser !== undefined) {
            vm.currentUser = $rootScope.currentUser;
        }
    });

    function getNotificationsFromServer() {
        vm.notificationsLogs = [];
        trackerSrv.getNotifications().success(function (data) {
            for (var i = 0; i < data.length; i++) {
                var log = data[i];
                var notification = {
                    "message": log.description,
                    "date": moment(log.timestamp).format('HH:mm:ss'),
                    "type": log.state,
                    "logId": log.id
                };
                addNewNotification(notification);
                vm.notificationsLogs[log.id] = log;
            }
        }).error(function (error) {
            console.log(error);
        });
    }

    getNotificationsFromServer();

    $rootScope.$watch('notificationsUpdate', function () {
        getNotificationsFromServer();
    });

    function viewLogs(logId) {
        vm.currentLog = [];
        vm.currentLogTitle = '';

        if (vm.notificationsLogs[logId].log.length > 0) {
            var logsArray = vm.notificationsLogs[logId].log.split(/(?:\r\n|\r|\n)/g);
            vm.currentLogTitle = vm.notificationsLogs[logId].description;
            var logs = [];
            for (var i = 0; i < logsArray.length; i++) {
                var currentLog = JSON.parse(logsArray[i].substring(0, logsArray[i].length - 1));
                currentLog.date = moment(currentLog.date).format('HH:mm:ss');
                logs.push(currentLog);
            }
            vm.currentLog = logs;

            ngDialog.open({
                template: 'subutai-app/common/popups/logsPopup.html',
                scope: $scope
            });
        }
    }

    $rootScope.$watch('notifications', function () {
        addNewNotification($rootScope.notifications);
    });

    setInterval(function () {
        getNotificationsFromServer();
    }, 15000);

    function addNewNotification(notification) {
        var notifications = sessionStorage.getItem('notifications');
        if (
            notifications == null ||
            notifications == undefined ||
            notifications == 'null' ||
            notifications.length <= 0
        ) {
            notifications = [];
            sessionStorage.setItem('notifications', notifications);
        } else {
            notifications = JSON.parse(notifications);
            vm.notificationsCount = notifications.length;
        }

        if (notification.message) {
            if (!sessionStorage.getItem('notifications').includes(JSON.stringify(notification.message))) {
                notifications.push(notification);
                vm.notificationsCount++;
                sessionStorage.setItem('notifications', JSON.stringify(notifications));
            } else {
                for (var i = 0; i < notifications.length; i++) {
                    if (notifications[i].message == notification.message && notification.type !== undefined) {
                        notifications[i].type = notification.type;
                        break;
                    }
                }
            }
            vm.notificationNew = true;
        }
        vm.notifications = notifications;
    }

    function clearLogs() {
        vm.notifications = [];
        vm.notificationsCount = 0;
        sessionStorage.removeItem('notifications');

        trackerSrv.deleteAllNotifications().success(function (data) {
        }).error(function (error) {
            console.log(error);
        });
    }


    function checkSum() {
        $http.get(SERVER_URL + "rest/v1/bazaar/products/checksum", {
            withCredentials: true,
            headers: {'Content-Type': 'application/json'}
        }).success(function (data) {
            if (localStorage.getItem("bazaarMD5") === null) {
                localStorage.setItem("bazaarMD5", data);
                bazaarUpdate = true;
            }
            else {
                if (localStorage.getItem("bazaarMD5") !== data) {
                    localStorage.setItem("bazaarMD5", data);
                    bazaarUpdate = true;
                }
            }
        });
    }

    checkSum();

}


function SubutaiController($rootScope, $http, $state) {
    var vm = this;
    vm.bodyClass = '';
    vm.activeState = '';
    vm.adminMenus = false;
    vm.isTenantManager = false;
    vm.peerName = "Subutai Social";

	$http.get(SERVER_URL + 'rest/ui/identity/is-admin', {
		withCredentials: true,
		headers: {'Content-Type': 'application/json'}
	}).success(function (data) {
		if (data == true || data == 'true') {
			localStorage.setItem('isAdmin', true);
		} else {
			localStorage.setItem('isAdmin', false);
		}
	});

    $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
        vm.layoutType = 'subutai-app/common/layouts/' + toState.data.layout + '.html';
        if (angular.isDefined(toState.data.bodyClass)) {
            vm.bodyClass = toState.data.bodyClass;
            vm.activeState = toState.name;

            vm.adminMenus = false;
            if (localStorage.getItem("currentUserPermissions"))
                for (var i = 0; i < localStorage.getItem("currentUserPermissions").length; i++) {
                    if (localStorage.getItem("currentUserPermissions")[i] == 6) {
                        vm.adminMenus = true;
                    }
                }

            $http.get(SERVER_URL + "rest/ui/identity/is-tenant-manager", {
                withCredentials: true,
                headers: {'Content-Type': 'application/json'}
            }).success(function (data) {
                if (data == true || data == 'true') {
                    vm.isTenantManager = true;
                }
            });

            /*$http.get(SERVER_URL + "rest/v1/hub/registration_state", {
                withCredentials: true,
                headers: {'Content-Type': 'application/json'}
            }).success(function (data) {
                if (data.isRegisteredToHub == true) {
                    vm.peerName = data.peerName + " | Subutai Social";
                }else{
                    vm.peerName = "Subutai Social";
                }
            });*/
            return;
        }

        vm.bodyClass = '';
    });

	$rootScope.$on('$stateChangeError', function(e, toState, toParams, fromState, fromParams, error){
		console.log(error);
		if(error === "notAdmin"){
			$state.go('404');
		}
	});
}

var $stateProviderRef = null;
function routesConf($httpProvider, $stateProvider, $urlRouterProvider, $ocLazyLoadProvider) {

    $urlRouterProvider.otherwise('/404');

    $ocLazyLoadProvider.config({
        debug: false
    });

    //$locationProvider.html5Mode(true);
    $urlRouterProvider.when('', '/');

    $stateProvider
        .state('login', {
            url: '/login',
            templateUrl: 'subutai-app/login/partials/view.html',
            data: {
                bodyClass: 'b-body',
                layout: 'fullpage'
            },
            resolve: {
                loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            name: 'subutai.login',
                            files: [
                                'subutai-app/login/login.js',
                                'subutai-app/login/controller.js',
                                'subutai-app/login/service.js'
                            ]
                        }
                    ])
                }]
            }
        })
		.state('identity-user', {
			url: '/identity-user',
			templateUrl: 'subutai-app/identityUser/partials/view.html',
			data: {
				bodyClass: '',
				layout: 'default'
			},
			resolve: {
				security: ['$q', function($q){
					if(!localStorage.getItem('isAdmin')){
						return $q.reject("notAdmin");
					}
				}],
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
						{
							name: 'subutai.identity-user',
							files: [
								'subutai-app/identityUser/identityUser.js',
								'subutai-app/identityUser/controller.js',
								'subutai-app/identity/service.js'
							]
						}
					]);
				}]
			}
		})
		.state('identity-role', {
			url: '/identity-role',
			templateUrl: 'subutai-app/identityRole/partials/view.html',
			data: {
				bodyClass: '',
				layout: 'default'
			},
			resolve: {
				security: ['$q', function($q){
					if(!localStorage.getItem('isAdmin')){
						return $q.reject("notAdmin");
					}
				}],
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
						{
							name: 'subutai.identity-role',
							files: [
								'subutai-app/identityRole/identityRole.js',
								'subutai-app/identityRole/controller.js',
								'subutai-app/identity/service.js'
							]
						}
					]);
				}]
			}
		})
		.state('peer-registration', {
			url: '/peer-registration',
			templateUrl: 'subutai-app/peerRegistration/partials/view.html',
			data: {
				bodyClass: '',
				layout: 'default'
			},
			resolve: {
				security: ['$q', function($q){
					if(!localStorage.getItem('isAdmin')){
						return $q.reject("notAdmin");
					}
				}],
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
						{
							name: 'subutai.peer-registration',
							files: [
								'subutai-app/peerRegistration/peerRegistration.js',
								'subutai-app/peerRegistration/controller.js',
								'subutai-app/peerRegistration/service.js'
							]
						}
					]);
				}]
			}
		})
		.state('nodeReg', {
			url: '/nodeReg',
			templateUrl: 'subutai-app/nodeReg/partials/view.html',
			data: {
				bodyClass: '',
				layout: 'default'
			},
			resolve: {
				security: ['$q', function($q){
					if(!localStorage.getItem('isAdmin')){
						return $q.reject("notAdmin");
					}
				}],
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load(
						{
							name: 'subutai.nodeReg',
							files: [
								'subutai-app/nodeReg/nodeReg.js',
								'subutai-app/nodeReg/controller.js',
								'subutai-app/nodeReg/service.js',
								'subutai-app/environment/service.js'
							]
						});
				}]
			}
		})
		.state('settings-network', {
			url: '/settings-network',
			templateUrl: 'subutai-app/settingsNetwork/partials/view.html',
			data: {
				bodyClass: '',
				layout: 'default'
			},
			resolve: {
				security: ['$q', function($q){
					if(!localStorage.getItem('isAdmin')){
						return $q.reject("notAdmin");
					}
				}],
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
						{
							name: 'subutai.settings-network',
							files: [
								'subutai-app/settingsNetwork/settingsNetwork.js',
								'subutai-app/settingsNetwork/controller.js',
								'subutai-app/settingsNetwork/service.js'
							]
						}
					]);
				}]
			}
		})
		.state('settings-advanced', {
			url: '/settings-advanced',
			templateUrl: 'subutai-app/settingsAdvanced/partials/view.html',
			data: {
				bodyClass: '',
				layout: 'default'
			},
			resolve: {
				security: ['$q', function($q){
					if(!localStorage.getItem('isAdmin')){
						return $q.reject("notAdmin");
					}
				}],
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
						{
							files: ['scripts/libs/FileSaver.min.js']
						},
						{
							name: 'vtortola.ng-terminal'
						},
						{
							name: 'subutai.settings-advanced',
							files: [
								'subutai-app/settingsAdvanced/settingsAdvanced.js',
								'subutai-app/settingsAdvanced/controller.js',
								'subutai-app/settingsAdvanced/service.js'
							]
						}
					]);
				}]
			}
		})
		.state('settings-security', {
			url: '/settings-security',
			templateUrl: 'subutai-app/settingsSecurity/partials/view.html',
			data: {
				bodyClass: '',
				layout: 'default'
			},
			resolve: {
				security: ['$q', function($q){
					if(!localStorage.getItem('isAdmin')){
						return $q.reject("notAdmin");
					}
				}],
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
						{
							name: 'subutai.settings-security',
							files: [
								'subutai-app/settingsSecurity/settingsSecurity.js',
								'subutai-app/settingsSecurity/controller.js',
								'subutai-app/settingsSecurity/service.js'
							]
						}
					]);
				}]
			}
		})
		.state('about', {
			url: '/about',
			templateUrl: 'subutai-app/about/partials/view.html',
			data: {
				bodyClass: '',
				layout: 'default'
			},
			resolve: {
				security: ['$q', function($q){
					if(!localStorage.getItem('isAdmin')){
						return $q.reject("notAdmin");
					}
				}],
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
						{
							name: 'subutai.about',
							files: [
								'subutai-app/about/about.js',
								'subutai-app/about/controller.js',
							]
						}
					])
				}]
			}
		})
		.state('settings-peer', {
			url: '/settings-peer',
			templateUrl: 'subutai-app/settingsPeer/partials/view.html',
			data: {
				bodyClass: '',
				layout: 'default'
			},
			resolve: {
				security: ['$q', function($q){
					if(!localStorage.getItem('isAdmin')){
						return $q.reject("notAdmin");
					}
				}],
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
						{
							name: 'subutai.settings-peer',
							files: [
								'subutai-app/settingsPeer/settingsPeer.js',
								'subutai-app/settingsPeer/controller.js',
								'subutai-app/settingsPeer/service.js'
							]
						}
					]);
				}]
			}
		})
		.state('settings-kurjun', {
			url: '/settings-kurjun',
			templateUrl: 'subutai-app/settingsKurjun/partials/view.html',
			data: {
				bodyClass: '',
				layout: 'default'
			},
			resolve: {
				security: ['$q', function($q){
					if(!localStorage.getItem('isAdmin')){
						return $q.reject("notAdmin");
					}
				}],
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
						{
							name: 'subutai.settings-kurjun',
							files: [
								'subutai-app/settingsKurjun/settingsKurjun.js',
								'subutai-app/settingsKurjun/controller.js',
								'subutai-app/settingsKurjun/service.js'
							]
						}
					]);
				}]
			}
		})
		.state('settings-updates', {
			url: '/settings-updates',
			templateUrl: 'subutai-app/settingsUpdates/partials/view.html',
			data: {
				bodyClass: '',
				layout: 'default'
			},
			resolve: {
				security: ['$q', function($q){
					if(!localStorage.getItem('isAdmin')){
						return $q.reject("notAdmin");
					}
				}],
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
						{
							name: 'subutai.settings-updates',
							files: [
								'subutai-app/settingsUpdates/settingsUpdates.js',
								'subutai-app/settingsUpdates/controller.js',
								'subutai-app/settingsUpdates/service.js'
							]
						}
					]);
				}]
			}
		})
        .state('change-pass', {
            url: '/change-pass',
            templateUrl: 'subutai-app/login/partials/change-pass.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
                loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            name: 'subutai.login',
                            files: [
                                'subutai-app/login/login.js',
                                'subutai-app/login/controller.js',
                                'subutai-app/login/service.js'
                            ]
                        }
                    ])
                }]
            }
        })
        .state('home', {
            url: '/',
            templateUrl: 'subutai-app/monitoring/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
                loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            name: 'subutai.monitoring',
                            files: [
                                'subutai-app/monitoring/monitoring.js',
                                'subutai-app/monitoring/controller.js',
                                'subutai-app/monitoring/service.js',
                                'subutai-app/environment/service.js',
                                'subutai-app/peerRegistration/service.js'
                            ]
                        }
                    ]);
                }]
            }
        })
        .state('environments', {
            url: '/environments/{activeTab}',
            templateUrl: 'subutai-app/environment/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
                loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            name: 'subutai.environment',
                            files: [
                                'subutai-app/environment/environment.js',
                                'subutai-app/environment/controller.js',
                                'subutai-app/environment/simple-controller.js',
                                'subutai-app/environment/advanced-controller.js',
                                'subutai-app/environment/service.js',
                                'subutai-app/peerRegistration/service.js',
                                'subutai-app/tracker/service.js',
                                'subutai-app/identity/service.js'
                            ]
                        }
                    ]);
                }]
            }
        })
        .state('containers', {
            url: '/containers/{environmentId}',
            templateUrl: 'subutai-app/containers/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
                loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            name: 'subutai.containers',
                            files: [
                                'subutai-app/containers/containers.js',
                                'subutai-app/containers/controller.js',
                                'subutai-app/environment/service.js'
                            ]
                        }
                    ]);
                }]
            }
        })
        .state('tenants', {
            url: '/tenants',
            templateUrl: 'subutai-app/tenants/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
                loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            name: 'subutai.tenants',
                            files: [
                                'subutai-app/tenants/tenants.js',
                                'subutai-app/tenants/controller.js',
                                'subutai-app/environment/service.js'
                            ]
                        }
                    ]);
                }]
            }
        })
        .state('kurjun', {
            url: '/kurjun',
            templateUrl: 'subutai-app/kurjun/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
                loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            name: 'subutai.kurjun',
                            files: [
                                'subutai-app/kurjun/kurjun.js',
                                'subutai-app/kurjun/controller.js',
                                'subutai-app/kurjun/service.js',
                                'subutai-app/identity/service.js',
                                'subutai-app/settingsKurjun/service.js'
                            ]
                        }
                    ]);
                }]
            }
        })
        .state('tracker', {
            url: '/tracker',
            templateUrl: 'subutai-app/tracker/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
                loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            name: 'subutai.tracker',
                            files: [
                                'subutai-app/tracker/tracker.js',
                                'subutai-app/tracker/controller.js',
                                'subutai-app/tracker/service.js'
                            ]
                        }
                    ]);
                }]
            }
        })
        .state('tokens', {
            url: '/tokens',
            templateUrl: 'subutai-app/tokens/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
                loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            name: 'subutai.tokens',
                            files: [
                                'subutai-app/tokens/tokens.js',
                                'subutai-app/tokens/controller.js',
                                'subutai-app/identity/service.js'
                            ]
                        }
                    ]);
                }]
            }
        })
        .state('account-settings', {
            url: '/account-settings',
            templateUrl: 'subutai-app/accountSettings/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
                loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            name: 'subutai.accountSettings',
                            files: [
                                'subutai-app/accountSettings/accountSettings.js',
                                'subutai-app/accountSettings/controller.js',
                                'subutai-app/identity/service.js'
                            ]
                        }
                    ]);
                }]
            }
        })
        .state('console', {
            url: '/console:environmentId?containerId',
            templateUrl: 'subutai-app/console/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
                loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            name: 'vtortola.ng-terminal'
                        },
                        {
                            name: 'subutai.console',
                            files: [
                                'subutai-app/console/console.js',
                                'subutai-app/console/controller.js',
                                'subutai-app/console/service.js',
                                'subutai-app/environment/service.js',
                                'subutai-app/peerRegistration/service.js'
                            ]
                        }
                    ]);
                }]
            }
        })
        .state('bazaar', {
            url: '/bazaar',
            templateUrl: 'subutai-app/bazaar/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
                loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
                    return $ocLazyLoad.load([
                        {
                            name: 'subutai.bazaar',
                            files: [
                                'subutai-app/bazaar/bazaar.js',
                                'subutai-app/bazaar/controller.js',
                                'subutai-app/bazaar/service.js',
                                'subutai-app/identity/service.js',
                                'subutai-app/bazaar/partials/css/demo.css',
                                'subutai-app/bazaar/partials/css/component.css'
                            ]
                        }
                    ]);
                }]
            }
        })
        .state('404', {
            url: '/404',
            templateUrl: 'subutai-app/common/partials/404.html',
            data: {
                bodyClass: 'b-body',
                layout: 'fullpage'
            }
        })
        .state();

    $stateProviderRef = $stateProvider;

    $httpProvider.interceptors.push(function ($q, $location) {
        return {
            'responseError': function (rejection) {
                if (rejection.status == 401 && $.inArray($location.path(), ['/login']) === -1) {
                    $location.path('/login');
                }
                return $q.reject(rejection);
            }
        };
    });
}

function startup($rootScope, $state, $location, $http, SweetAlert, ngDialog) {

    $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
        LOADING_SCREEN('none');
        ngDialog.closeAll();
        $('.sweet-overlay').remove();
        $('.sweet-alert').remove();

        var restrictedPage = $.inArray($location.path(), ['/login']) === -1;
        if (restrictedPage && !getCookie('sptoken')) {
            localStorage.removeItem('currentUser');
            $location.path('/login');
        }
    });

    $rootScope.$on('reloadPluginsStates', function (event) {
        location.reload();
        /*var state = {
         url: '/console/{containerId}',
         templateUrl: 'subutai-app/console/partials/view.html',
         data: {
         bodyClass: '',
         layout: 'default'
         },
         resolve: {
         loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
         return $ocLazyLoad.load([
         {
         name: 'vtortola.ng-terminal'
         },
         {
         name: 'subutai.console',
         files: [
         'subutai-app/console/console.js',
         'subutai-app/console/controller.js',
         'subutai-app/console/service.js',
         'subutai-app/environment/service.js',
         'subutai-app/peerRegistration/service.js'
         ]
         }
         ]);
         }]
         }
         };
         $stateProviderRef.state('console', state);*/
    });

    $rootScope.$state = $state;
}

function getCookie(cname) {
    var name = cname + '=';
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1);
        if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
    }
    return false;
}

function removeCookie(name) {
    document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}

app.directive('checkbox-list-dropdown', function () {
    return {
        restrict: 'A',
        link: function (scope, element, attr) {
            $('.b-form-input_dropdown').click(function () {
                $(this).toggleClass('is-active');
            });

            $('.b-form-input-dropdown-list').click(function (e) {
                e.stopPropagation();
            });
        }
    }
});

app.directive('focusInput', function ($timeout, $parse) {
    return {
        link: function (scope, element, attrs) {
            var model = $parse(attrs.focusInput);
            scope.$watch(model, function (value) {
                if (value === true) {
                    $timeout(function () {
                        element[0].focus();
                    });
                }
            });
            //element.bind('blur', function () {});
        }
    };
});

//Global variables

var bazaarUpdate = false;
var hubRegisterStatus = false;

var SERVER_URL = '/';
var GLOBAL_KURJUN_URL = '';

var STATUS_UNDER_MODIFICATION = 'UNDER_MODIFICATION';
var VARS_TOOLTIP_TIMEOUT = 1600;

function LOADING_SCREEN(displayStatus) {
    if (displayStatus === undefined || displayStatus === null) displayStatus = 'block';
    var loadScreen = document.getElementsByClassName('js-loading-screen')[0];
    if (loadScreen && loadScreen.style.display != displayStatus) {
        loadScreen.style.display = displayStatus;
    }
}

function VARS_MODAL_CONFIRMATION(object, title, text, func) {
    object.swal({
            title: title,
            text: text,
            type: "warning",
            showCancelButton: true,
            confirmButtonColor: "#ff3f3c",
            confirmButtonText: "Delete",
            cancelButtonText: "Cancel",
            closeOnConfirm: false,
            closeOnCancel: true,
            showLoaderOnConfirm: true
        },
        func
    );
}

function VARS_MODAL_ERROR(object, text) {
    object.swal({
        title: "ERROR!",
        text: text,
        type: "error",
        confirmButtonColor: "#ff3f3c"
    });
}

quotaColors = [];
quotaColors['CUSTOM'] = '#000000';
quotaColors['HUGE'] = '#0071bc';
quotaColors['LARGE'] = '#22b573';
quotaColors['MEDIUM'] = '#c1272d';
quotaColors['SMALL'] = '#fbb03b';
quotaColors['TINY'] = '#d9e021';
quotaColors['INACTIVE'] = '#b3b3b3';

var permissionsDefault = [
    {
        'object': 1,
        'name': 'Identity-Management',
        'scope': 1,
        'read': true,
        'write': true,
        'update': true,
        'delete': true,
    },
    {
        'object': 2,
        'name': 'Peer-Management',
        'scope': 1,
        'read': true,
        'write': true,
        'update': true,
        'delete': true,
    },
    {
        'object': 3,
        'name': 'Environment-Management',
        'scope': 1,
        'read': true,
        'write': true,
        'update': true,
        'delete': true,
    },
    {
        'object': 4,
        'name': 'Resource-Management',
        'scope': 1,
        'read': true,
        'write': true,
        'update': true,
        'delete': true,
    },
    {
        'object': 5,
        'name': 'Template-Management',
        'scope': 1,
        'read': true,
        'write': true,
        'update': true,
        'delete': true,
    },
    {
        'object': 6,
        'name': 'Karaf-Server-Administration',
        'scope': 1,
        'read': true,
        'write': true,
        'update': true,
        'delete': true,
    },
    {
        'object': 7,
        'name': 'Karaf-Server-Management',
        'scope': 1,
        'read': true,
        'write': true,
        'update': true,
        'delete': true,
    },
    {
        'object': 8,
        'name': 'Tenant-Management',
        'scope': 1,
        'read': true,
        'write': true,
        'update': true,
        'delete': true,
    }
];


function toggle(source, name) {
    checkboxes = document.getElementsByName(name);
    for (var i = 0; i < checkboxes.length; i++) {
        checkboxes[i].checked = source.checked;
    }
}

function hasPGPplugin() {
    if ($('#bp-plugin-version').val().length > 0) {
        return $('#bp-plugin-version').val();
    } else {
        return false;
    }
}

