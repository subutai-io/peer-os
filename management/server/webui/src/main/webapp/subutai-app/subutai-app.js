var app = angular.module("subutai-app", [
	'ui.router',
	'ngCookies',
	'ngResource',
	'oc.lazyLoad',
	'oitozero.ngSweetAlert',
	'ngDialog',
	'datatables',
])
.config(routesConf)
	.run(startup);

routesConf.$inject = ['$stateProvider', '$urlRouterProvider', '$ocLazyLoadProvider'];
startup.$inject = ['$rootScope', '$state', '$cookieStore', '$location', '$http'];

function routesConf($stateProvider, $urlRouterProvider, $ocLazyLoadProvider) {

	$urlRouterProvider.otherwise("/404");

	$ocLazyLoadProvider.config({
		debug: false
	});

	$stateProvider
	.state("login", {
		url: "/login",
		templateUrl: "subutai-app/login/partials/view.html",
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
	.state("home", {
		url: "",
		templateUrl: "subutai-app/home/partials/view.html"
	})
	.state("blueprints", {
		url: "/blueprints",
		templateUrl: "subutai-app/blueprints/partials/view.html",
		resolve: {
			loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
				return $ocLazyLoad.load([
						{
							name: 'subutai.blueprints',
							files: [
								'subutai-app/blueprints/blueprints.js',
								'subutai-app/blueprints/controller.js',
								'subutai-app/environment/service.js'
							]
						}
				]);
			}]
		}
	})
	.state("blueprintsActions", {
		url: "/blueprints/{blueprintId}/{action}/",
		templateUrl: "subutai-app/blueprintsBuild/partials/view.html",
		resolve: {
			loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
				return $ocLazyLoad.load([
						{
							name: 'ya.nouislider',
							files: [
								'scripts/libs/wNumb.js',
								'scripts/libs/nouislider.min.js',
								'assets/js/plugins/nouislider.min.js'
							]
						},
						{
							name: 'subutai.blueprints-build',
							files: [
								'subutai-app/blueprintsBuild/blueprintsBuild.js',
								'subutai-app/blueprintsBuild/controller.js',
								'subutai-app/environment/service.js'
							]
						}
				]);
			}]
		}
	})
	.state("environments", {
		url: "/environments",
		templateUrl: "subutai-app/environment/partials/view.html",
		resolve: {
			loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
				return $ocLazyLoad.load([
						{
							name: 'subutai.environment',
							files: [
								'subutai-app/environment/environment.js',
								'subutai-app/environment/controller.js',
								'subutai-app/environment/service.js'
							]
						}
				]);
			}]
		}
	})
	.state("containers", {
		url: "/containers/{environmentId}",
		templateUrl: "subutai-app/containers/partials/view.html",
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
	.state("identity-user", {
		url: "/identity-user",
		templateUrl: "subutai-app/identityUser/partials/view.html",
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
	.state("identity-role", {
		url: "/identity-role",
		templateUrl: "subutai-app/identityRole/partials/view.html",
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
	.state("tracker", {
		url: "/tracker",
		templateUrl: "subutai-app/tracker/partials/view.html",
		resolve: {
			loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
				return $ocLazyLoad.load([
						{
							name: 'subutai.tracker',
							files: [
								'subutai-app/tracker/tracker.js',
								'subutai-app/tracker/controller.js',
								'subutai-app/tracker/service.js',
									'subutai-app/tracker/filter.js'
							]
						}
				]);
			}]
		}
	})
	.state('peer-registration', {
		url: '/peer-registration',
		templateUrl: 'subutai-app/peerRegistration/partials/view.html',
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
	.state("tokens", {
		url: "/tokens",
		templateUrl: "subutai-app/tokens/partials/view.html",
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
	.state("console", {
		url: "/console",
		templateUrl: "subutai-app/console/partials/view.html",
		resolve: {
			loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
				return $ocLazyLoad.load([
					{
						name: 'vtortola.ng-terminal',
						files: [
							'assets/js/plugins/vtortola.ng-terminal.js'
						]
					},
					{
						name: 'subutai.console',
						files: [
							'subutai-app/console/console.js',
							'subutai-app/console/controller.js',
							'subutai-app/console/service.js'
						]
					}
				]);
			}]
		}
	})
	.state("plugins", {
		url: "/plugins",
		templateUrl: "subutai-app/plugins/partials/view.html",
		resolve: {
			loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
				return $ocLazyLoad.load([
					{
						name: 'subutai.plugins',
						files: [
							'subutai-app/plugins/plugins.js',
							'subutai-app/plugins/controller.js',
							'subutai-app/plugins/service.js'
						]
					}
				]);
			}]
		}
	})
	.state("cassandra", {
		url: "/plugins/cassandra",
		templateUrl: "subutai-app/plugins/cassandra/partials/view.html",
		resolve: {
			loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
				return $ocLazyLoad.load([
						{
							name: 'subutai.plugins.cassandra',
							files: [
								'subutai-app/plugins/cassandra/cassandra.js',
								'subutai-app/plugins/cassandra/controller.js',
								'subutai-app/plugins/cassandra/service.js'
							]
						}
				]);
			}]
		}
	})
	.state("404", {
		url: "/404",
		template: "Not found"
	})
}

function startup($rootScope, $state, $cookieStore, $location, $http) {
	$rootScope.sptoken = $cookieStore.get('sptoken') || false;
	//$rootScope.sptoken = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MjQ4ZGJhMi1iODc3LTQyYTAtOTBiMi0wMDA1YWUzNWZmMmIiLCJpc3MiOiJpby5zdWJ1dGFpIn0.nevrOmYGmLFGoPces9x1y9ZboCzfKZPssa0JiWjN6Kw';
	var restrictedPage = $.inArray($location.path(), ['/login']) === -1;
	if (restrictedPage && !$rootScope.sptoken) {
		$location.path('login');
	}

	$rootScope.$on('$stateChangeStart',
		function(event, toState, toParams, fromState, fromParams){
			if( $cookieStore.get('sptoken') )
			{
				$http.defaults.headers.common['sptoken'] = $cookieStore.get('sptoken');
			}
			else
			{
				$location.path('login');
			}
		});

	$rootScope.$state = $state;
}

app.directive('dropdownMenu', function() {
	return {
		restrict: 'A',
		link: function(scope, element, attr) {
			function colEqualHeight() {
				if( $('.b-nav').height() > $('.b-workspace').height() ) {
					$('.b-workspace').height( $('.b-nav').height() );
				}else if( $('.b-nav').height() < $('.b-workspace').height() ) {
					$('.b-nav').height( $('.b-workspace').height() );
				}
			}
			//colEqualHeight();

			$('.b-nav-menu-link').on('click', function(){
				if($(this).next('.b-nav-menu__sub').length > 0) {
					if($(this).parent().hasClass('b-nav-menu_active')) {
						$(this).parent().removeClass('b-nav-menu_active');
						$(this).next('.b-nav-menu__sub').slideUp(300, function(){
							//colEqualHeight();
						});
					} else {
						$(this).parent().addClass('b-nav-menu_active');
						$(this).next('.b-nav-menu__sub').slideDown(300, function(){
							//colEqualHeight();
						});
					}
					return false;
				}
			});			
		}
	}
});

//Global variables
var serverUrl = '/rest/';
quotaColors = [];
quotaColors['CUSTOM'] = 'blue';
quotaColors['HUGE'] = 'bark-red';
quotaColors['LARGE'] = 'red';
quotaColors['MEDIUME'] = 'orange';
quotaColors['SMALL'] = 'yellow';
quotaColors['TINY'] = 'green';

var permissionsDefault = [
	{
		"object": 1,
		"name": "Identity-Management",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	},
	{
		"object": 2,
		"name": "Peer-Management",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	},
	{
		"object": 3,
		"name": "Environment-Management",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	},
	{
		"object": 4,
		"name": "Resource-Management",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	},
	{
		"object": 5,
		"name": "Template-Management",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	},
	{
		"object": 6,
		"name": "Karaf-Server-Administration",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	},
	{
		"object": 7,
		"name": "Karaf-Server-Management",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	}
];

