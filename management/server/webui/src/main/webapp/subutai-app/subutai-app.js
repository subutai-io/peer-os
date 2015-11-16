var app = angular.module("subutai-app", [
	'ui.router',
	'ngResource',
	'oc.lazyLoad',
	'oitozero.ngSweetAlert',
	'ngDialog',
	'datatables',
])
.config(routesConf)
	.run(startup);

//Global
var serverUrl = 'http://172.16.131.205:8181/rest/';
quotaColors = [];
quotaColors['CUSTOM'] = 'blue';
quotaColors['HUGE'] = 'bark-red';
quotaColors['LARGE'] = 'red';
quotaColors['MEDIUME'] = 'orange';
quotaColors['SMALL'] = 'yellow';
quotaColors['TINY'] = 'green';

routesConf.$inject = ['$stateProvider', '$urlRouterProvider', '$ocLazyLoadProvider'];
startup.$inject = ['$rootScope', '$state'];

function routesConf($stateProvider, $urlRouterProvider, $ocLazyLoadProvider) {

	$urlRouterProvider.otherwise("/404");

	$ocLazyLoadProvider.config({
		debug: false
	});

	$stateProvider
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
}

function startup($rootScope, $state) {
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

