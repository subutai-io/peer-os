var app = angular.module("subutai-app", [
	'ui.router',
	'ngResource',
	'oc.lazyLoad',
	'ui.bootstrap',
	'ngAnimate',
	'mc.resizer',
	'pascalprecht.translate',
	'ui.tree',
	'localytics.directives',
	'datatables',
	'jsTree.directive',
	'oitozero.ngSweetAlert',
	'subutai.wol'
	//'app.notifyGrowl'
])
.config(routesConf)
	.run(startup);

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
		.state("console", {
			url: "/console",
			templateUrl: "subutai-app/console/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.console',
								files: [
									'subutai-app/console/console.js',
									'subutai-app/console/controller.js',
									'subutai-app/console/service.js'
								]
							},
							{
								name: 'vtortola.ng-terminal',
								files: [
									'assets-angular/js/plugins/vtortola.ng-terminal.js',
								]
							}
					]);
				}]
			}
		})
		.state("identity", {
			url: "/identity",
			templateUrl: "subutai-app/identity/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.identity',
								files: [
									'subutai-app/identity/identity.js',
									'subutai-app/identity/controller.js',
									'subutai-app/identity/service.js'
								]
							}
					]);
				}]
			}
		})
		.state('channel-manager', {
			url: '/channel-manager',
			templateUrl: 'subutai-app/channelManager/partials/view.html',
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.channel-manager',
								files: [
									'subutai-app/channelManager/channelManager.js',
									'subutai-app/channelManager/controller.js',
									'subutai-app/channelManager/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("metrics", {
			url: "/metrics",
			templateUrl: "subutai-app/metrics/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.metrics',
								files: [
									'subutai-app/metrics/metrics.js',
									'subutai-app/metrics/controller.js',
									'subutai-app/metrics/service.js'
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
					])
				}]
			}
		})
		.state("core", {
			url: "/core",
			templateUrl: "subutai-app/core/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.core',
								files: [
									'subutai-app/core/core.js',
									'subutai-app/core/controller.js',
									'subutai-app/core/service.js'
								]
							}
					])
				}]
			}
		})
		.state("environment", {
			url: "/environment",
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
		.state("registry", {
			url: "/registry",
			templateUrl: "subutai-app/registry/partials/view.html",
			data: {pageTitle: 'Template Registry'},
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								files: ['assets-angular/js/plugins/diff_match_patch/javascript/diff_match_patch.js']
							},
							{
								name: 'diff-match-patch',
								files: ['assets-angular/js/plugins/angular-diff-match-patch/angular-diff-match-patch.js']
							},
							{
								name: 'ui.nested.combobox',
								files: ['assets-angular/js/plugins/angular-nested-combobox/ng-nested-combobox.js']
							},
							{
								name: 'subutai.registry',
								files: [
									'subutai-app/registry/registry.js',
									'subutai-app/registry/controller.js',
									'subutai-app/registry/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("peers", {
			url: "/peers",
			templateUrl: "subutai-app/peer/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.peer',
								files: [
									'subutai-app/peer/peer.js',
									'subutai-app/peer/controller.js',
									'subutai-app/peer/service.js'
								]
							},
							{
								name: 'localytics.directives',
								files: [
									'assets-angular/js/plugins/chosen/chosen.js',
									'assets-angular/js/plugins/chosen/chosen.jquery.js'
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
		.state("404", {
			url: "/404",
			template: "Not found"
		}
		)
			.state("pluginsPage", {
				url: "/pluginsPage",
				templateUrl: "subutai-app/plugins/partials/sky.html",
			})
		.state("cassandra", {
			url: "/pluginsPage/cassandra",
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
		.state("elastic-search", {
			url: "/pluginsPage/elastic-search",
			templateUrl: "subutai-app/plugins/elastic-search/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.elastic-search',
								files: [
									'subutai-app/plugins/elastic-search/elastic-search.js',
									'subutai-app/plugins/elastic-search/controller.js',
									'subutai-app/plugins/elastic-search/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("flume", {
			url: "/pluginsPage/flume",
			templateUrl: "subutai-app/plugins/flume/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.flume',
								files: [
									'subutai-app/plugins/flume/flume.js',
									'subutai-app/plugins/flume/controller.js',
									'subutai-app/plugins/flume/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("hadoop", {
			url: "/pluginsPage/hadoop",
			templateUrl: "subutai-app/plugins/hadoop/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.hadoop',
								files: [
									'subutai-app/plugins/hadoop/hadoop.js',
									'subutai-app/plugins/hadoop/controller.js',
									'subutai-app/plugins/hadoop/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("hive", {
			url: "/pluginsPage/hive",
			templateUrl: "subutai-app/plugins/hive/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.hive',
								files: [
									'subutai-app/plugins/hive/hive.js',
									'subutai-app/plugins/hive/controller.js',
									'subutai-app/plugins/hive/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("hipi", {
			url: "/pluginsPage/hipi",
			templateUrl: "subutai-app/plugins/hipi/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.hipi',
								files: [
									'subutai-app/plugins/hipi/hipi.js',
									'subutai-app/plugins/hipi/controller.js',
									'subutai-app/plugins/hipi/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("mahout", {
			url: "/pluginsPage/mahout",
			templateUrl: "subutai-app/plugins/mahout/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.mahout',
								files: [
									'subutai-app/plugins/mahout/mahout.js',
									'subutai-app/plugins/mahout/controller.js',
									'subutai-app/plugins/mahout/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("mongo", {
			url: "/pluginsPage/mongo",
			templateUrl: "subutai-app/plugins/mongo/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.mongo',
								files: [
									'subutai-app/plugins/mongo/mongo.js',
									'subutai-app/plugins/mongo/controller.js',
									'subutai-app/plugins/mongo/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("nutch", {
			url: "/pluginsPage/nutch",
			templateUrl: "subutai-app/plugins/nutch/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.nutch',
								files: [
									'subutai-app/plugins/nutch/nutch.js',
									'subutai-app/plugins/nutch/controller.js',
									'subutai-app/plugins/nutch/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("oozie", {
			url: "/pluginsPage/oozie",
			templateUrl: "subutai-app/plugins/oozie/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.oozie',
								files: [
									'subutai-app/plugins/oozie/oozie.js',
									'subutai-app/plugins/oozie/controller.js',
									'subutai-app/plugins/oozie/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("pig", {
			url: "/pluginsPage/pig",
			templateUrl: "subutai-app/plugins/pig/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.pig',
								files: [
									'subutai-app/plugins/pig/pig.js',
									'subutai-app/plugins/pig/controller.js',
									'subutai-app/plugins/pig/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("presto", {
			url: "/pluginsPage/presto",
			templateUrl: "subutai-app/plugins/presto/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.presto',
								files: [
									'subutai-app/plugins/presto/presto.js',
									'subutai-app/plugins/presto/controller.js',
									'subutai-app/plugins/presto/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("shark", {
			url: "/pluginsPage/shark",
			templateUrl: "subutai-app/plugins/shark/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.shark',
								files: [
									'subutai-app/plugins/shark/shark.js',
									'subutai-app/plugins/shark/controller.js',
									'subutai-app/plugins/shark/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("solr", {
			url: "/pluginsPage/solr",
			templateUrl: "subutai-app/plugins/solr/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.solr',
								files: [
									'subutai-app/plugins/solr/solr.js',
									'subutai-app/plugins/solr/controller.js',
									'subutai-app/plugins/solr/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("spark", {
			url: "/pluginsPage/spark",
			templateUrl: "subutai-app/plugins/spark/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.spark',
								files: [
									'subutai-app/plugins/spark/spark.js',
									'subutai-app/plugins/spark/controller.js',
									'subutai-app/plugins/spark/service.js'
								]
							}
					]);
				}]
			}
		})
		.state("zookeeper", {
			url: "/pluginsPage/zookeeper",
			templateUrl: "subutai-app/plugins/zookeeper/partials/view.html",
			resolve: {
				loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {
					return $ocLazyLoad.load([
							{
								name: 'subutai.plugins.zookeeper',
								files: [
									'subutai-app/plugins/zookeeper/zookeeper.js',
									'subutai-app/plugins/zookeeper/controller.js',
									'subutai-app/plugins/zookeeper/service.js'
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
