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
        'ngFileUpload'
    ])
    .config(routesConf)

    .controller('SubutaiController', SubutaiController)
    .controller('CurrentUserCtrl', CurrentUserCtrl)

    .controller('AccountCtrl', AccountCtrl)
    .factory('identitySrv', identitySrv)

    .run(startup);

CurrentUserCtrl.$inject = ['$location', '$scope', '$rootScope', '$http', 'SweetAlert', 'ngDialog'];
routesConf.$inject = ['$httpProvider', '$stateProvider', '$urlRouterProvider', '$ocLazyLoadProvider'];
startup.$inject = ['$rootScope', '$state', '$location', '$http', 'SweetAlert', 'ngDialog'];

function CurrentUserCtrl($location, $scope, $rootScope, $http, SweetAlert, ngDialog) {
    var vm = this;
    vm.currentUser = localStorage.getItem('currentUser');
    vm.hubStatus = false;
    vm.userId = "";
    vm.notifications = [];
    vm.notificationsCount = 0;
    vm.notificationNew = false;
    vm.currentUserRoles = [];
    $rootScope.notifications = {};
    vm.hubRegisterError = false;
    vm.isRegistrationFormVisible = false;

    vm.getRegistrationFormVisibilityStatus = function () {
        return vm.isRegistrationFormVisible;
    };


    function checkIfRegistered() {
        $http.get(SERVER_URL + "rest/v1/hub/registration_state", {
            withCredentials: true,
            headers: {'Content-Type': 'application/json'}
        }).success(function (data) {
            console.log(data);
            vm.hubStatus = data.isRegisteredToHub;
            vm.userId = data.ownerId;
            console.log(vm.userId);
            if (vm.hubStatus != "true" && vm.hubStatus != true) {
                console.log("wrong check");
                vm.hubStatus = false;
            }
            else {
                console.log("something else");
                vm.hubStatus = true;
            }
        });
    }

    checkIfRegistered();
    vm.hub = {
        login: "",
        password: ""
    };


    //function
    vm.logout = logout;
    vm.hubRegister = hubRegister;
    vm.hubUnregister = hubUnregister;
    vm.hubHeartbeat = hubHeartbeat;
    vm.clearLogs = clearLogs;


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
        var postData = 'hubIp=hub.subut.ai&email=' + vm.hub.login + '&password=' + vm.hub.password;
        $http.post(SERVER_URL + 'rest/v1/hub/register', postData, {
                withCredentials: true,
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            })
            .success(function () {
                localStorage.setItem('hubRegistered', true);
                vm.hubStatus = true;
                hubPopupLoadScreen();

                ngDialog.open({
                    template: 'subutai-app/common/partials/hubSuccessMessage.html',
                    scope: $scope
                });

                $http.post(SERVER_URL + 'rest/v1/hub/send-heartbeat?hubIp=hub.subut.ai', {
                        withCredentials: true,
                        headers: {'Content-Type': 'application/x-www-form-urlencoded'}
                    })
                    .success(function () {
                        //localStorage.setItem('hubRegistered', true);
                        //vm.hubStatus = true;
                        //hubPopupLoadScreen();
                        //SweetAlert.swal ("Success!", "Your peer was registered to Hub.", "success");
                    }).error(function (error) {
                    console.log('hub/register error: ', error);
                    vm.hubRegisterError = error;
                    //hubPopupLoadScreen();
                });

                $http.post(SERVER_URL + 'rest/v1/hub/send-rh-configurations?hubIp=hub.subut.ai', {
                        withCredentials: true,
                        headers: {'Content-Type': 'application/x-www-form-urlencoded'}
                    })
                    .success(function () {
                        //localStorage.setItem('hubRegistered', true);
                        //vm.hubStatus = true;
                        //hubPopupLoadScreen();
                        //SweetAlert.swal ("Success!", "Your peer was registered to Hub.", "success");
                    }).error(function (error) {
                    console.log('hub/register error: ', error);
                    vm.hubRegisterError = error;
                    //hubPopupLoadScreen();
                });


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
                localStorage.removeItem('hubRegistered');
                vm.hubStatus = false;
                //SweetAlert.swal ("Success!", "Your peer was unregistered from Hub.", "success");
            }).error(function (error) {
            hubPopupLoadScreen();
            SweetAlert.swal("ERROR!", "Error while registering to Hub.\nPlease check your credentials and try again.", "error");
        });
    }


    function hubHeartbeat() {
        //should be rest/v1/hub no need to change
        hubPopupLoadScreen(true);
        $http.post(SERVER_URL + 'rest/v1/hub/send-heartbeat', {withCredentials: true})
            .success(function () {
                hubPopupLoadScreen();
                localStorage.setItem('hubRegistered', true);
                vm.hubStatus = true;
                SweetAlert.swal("Success!", "Heartbeat sent successfully.", "success");
            }).error(function (error) {
            hubPopupLoadScreen();
            SweetAlert.swal("ERROR!", "Hub heartbeat error: " + error.replace(/\\n/g, " "), "error");
        });
    }

    function logout() {
        removeCookie('sptoken');
        localStorage.removeItem('currentUser');
        $location.path('login');
    }

    $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
        if (localStorage.getItem('currentUser') !== undefined) {
            vm.currentUser = localStorage.getItem('currentUser');
        } else if ($rootScope.currentUser !== undefined) {
            vm.currentUser = $rootScope.currentUser;
        }
    });

    //localStorage.removeItem('notifications');
    $rootScope.$watch('notifications', function () {

        var notifications = localStorage.getItem('notifications');
        console.log(notifications);
        if (
            notifications == null ||
            notifications == undefined ||
            notifications == 'null' ||
            notifications.length <= 0
        ) {
            notifications = [];
            localStorage.setItem('notifications', notifications);
        } else {
            notifications = JSON.parse(notifications);
            vm.notificationsCount = notifications.length;
        }

        if ($rootScope.notifications.message) {
            if (!localStorage.getItem('notifications').includes(JSON.stringify($rootScope.notifications.message))) {
                notifications.push($rootScope.notifications);
                vm.notificationsCount++;
                localStorage.setItem('notifications', JSON.stringify(notifications));
            }
            vm.notificationNew = true;
        }
        vm.notifications = notifications;
    });

    function clearLogs() {
        vm.notifications = [];
        vm.notificationsCount = 0;
        localStorage.removeItem('notifications');
    }
}

function SubutaiController($rootScope) {
    var vm = this;
    vm.bodyClass = '';
    vm.activeState = '';

    $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
        vm.layoutType = 'subutai-app/common/layouts/' + toState.data.layout + '.html';
        if (angular.isDefined(toState.data.bodyClass)) {
            vm.bodyClass = toState.data.bodyClass;
            vm.activeState = toState.name;
            return;
        }

        vm.bodyClass = '';
    });
}

var $stateProviderRef = null;
function routesConf($httpProvider, $stateProvider, $urlRouterProvider, $ocLazyLoadProvider) {

    $urlRouterProvider.otherwise('/404');

    $ocLazyLoadProvider.config({
        debug: false
    });

    //$locationProvider.html5Mode(true);

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
                                'subutai-app/identity/service.js'
                            ]
                        }
                    ]);
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
        .state('nodeReg', {
            url: '/nodeReg',
            templateUrl: 'subutai-app/nodeReg/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
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
        .state('peer-registration', {
            url: '/peer-registration',
            templateUrl: 'subutai-app/peerRegistration/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
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
        })
        .state('about', {
            url: '/about',
            templateUrl: 'subutai-app/about/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
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
        .state('settings-network', {
            url: '/settings-network',
            templateUrl: 'subutai-app/settingsNetwork/partials/view.html',
            data: {
                bodyClass: '',
                layout: 'default'
            },
            resolve: {
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

        $http.get(SERVER_URL + 'rest/v1/hub/registration_state', {withCredentials: true})
            .success(function (data) {
                localStorage.setItem('hubRegistered', data.isRegisteredToHub);
            }).error(function (error) {
            console.log("peer_settings error: ", error);
        });

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

app.directive('dropdownMenu', function () {
    return {
        restrict: 'A',
        link: function (scope, element, attr) {
            function colEqualHeight() {
                if ($('.b-nav').height() > $('.b-workspace').height()) {
                    $('.b-workspace').height($('.b-nav').height());
                } else if ($('.b-nav').height() < $('.b-workspace').height()) {
                    $('.b-nav').height($('.b-workspace').height());
                }
            }

            //colEqualHeight();

            $('.b-nav-menu-link').on('click', function () {
                $('.b-nav-menu__sub').slideUp(300);
                if ($(this).next('.b-nav-menu__sub').length > 0) {
                    if ($(this).parent().hasClass('b-nav-menu_active')) {
                        $(this).parent().removeClass('b-nav-menu_active');
                        $(this).next('.b-nav-menu__sub').slideUp(300);
                    } else {
                        $('.b-nav-menu_active').removeClass('b-nav-menu_active')
                        $(this).parent().addClass('b-nav-menu_active');
                        $(this).next('.b-nav-menu__sub').slideDown(300);
                    }
                    return false;
                } else {
                    $('.b-nav-menu__sub').slideUp(300);
                    $('.b-nav-menu_active').removeClass('b-nav-menu_active');
                }
            });
        }
    }
});

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

var SERVER_URL = '/';

var STATUS_UNDER_MODIFICATION = 'UNDER_MODIFICATION';
var VARS_TOOLTIP_TIMEOUT = 1600;

function LOADING_SCREEN(displayStatus) {
    if (displayStatus === undefined || displayStatus === null) displayStatus = 'block';
    var loadScreen = document.getElementsByClassName('js-loading-screen')[0];
    if (loadScreen) {
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
        return true;
    } else {
        return false;
    }
}

