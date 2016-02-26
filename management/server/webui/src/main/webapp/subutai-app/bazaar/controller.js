'use strict';

angular.module('subutai.bazaar.controller', [])
	.controller('BazaarCtrl', BazaarCtrl)
	.directive('fileModel', fileModel)

fileModel.$inject = ["$parse"];

var karUploader = {};
BazaarCtrl.$inject = ['$scope', '$rootScope', 'BazaarSrv', 'ngDialog', 'SweetAlert', '$location', 'cfpLoadingBar'];
function BazaarCtrl($scope, $rootScope, BazaarSrv, ngDialog, SweetAlert, $location, cfpLoadingBar) {

	var vm = this;

/*	vm.plugins = [{name: "test", version: "BETA", description: "Some desc...", installed: true, img: "http://twimgs.com/informationweek/galleries/automated/723/01_Hadoop_full.jpg"}, {name: "test2", version: "ALPHA", description: "Some desc...", installed: false, img: "https://flume.apache.org/_static/flume-logo.png"}];*/
	vm.plugins = [];
	vm.activeTab = "hub";
	vm.changeTab = changeTab;
	function changeTab (tab) {
		vm.activeTab = tab;
		getHubPlugins();
	}
	vm.installedPlugins = [];
	vm.installedHubPlugins = [];
	vm.notRegistered = true;
	function getHubPlugins() {
		BazaarSrv.checkRegistration().success (function (data) {
			console.log (data);
			if (data.isRegisteredToHub) {
				vm.notRegistered = false;
				BazaarSrv.getHubPlugins().success (function (data) {
					vm.plugins = data.productsDto;
					if (vm.plugins === undefined || vm.plugins === "") {
						vm.plugins = [];
					}
					console.log (vm.plugins);
					BazaarSrv.getInstalledHubPlugins().success (function (data) {
						vm.installedHubPlugins = data;
						for (var i = 0; i < vm.plugins.length; ++i) {
							vm.plugins[i].img = "https://s3-eu-west-1.amazonaws.com/subutai-hub/products/" + vm.plugins[i].id + "/logo/logo.png";
							vm.plugins[i].installed = false;
							for (var j = 0; j < vm.installedHubPlugins.length; ++j) {
								if (vm.plugins[i].name === vm.installedHubPlugins[j].name) {
									vm.plugins[i].installed = true;
									vm.plugins[i].launch = true;
									vm.plugins[i].hubId = vm.installedHubPlugins[j].id;
									vm.plugins[i].url = vm.installedHubPlugins[j].url;
									break;
								}
							}
						}
						$scope.$applyAsync (function() {
							var toScroll = document.getElementById (localStorage.getItem ("bazaarScroll"));
							if (toScroll !== null) {
								toScroll.scrollIntoView();
							}
							localStorage.removeItem ("bazaarScroll");
							var index = 0;
							var counter = 0;
							[].slice.call (document.querySelectorAll (".progress-button")).forEach (function (bttn, pos) {
								var prog = new UIProgressButton (bttn, {
									callback: function (instance) {
									}
								});
								if (counter === 0) {
									vm.plugins[index].installButton = prog;
								}
								else {
									vm.plugins[index].uninstallButton = prog;
								}
								counter = (counter + 1) % 2;
								if (counter === 0) {
									++index;
								}
							});
						});
					});
				});
			}
			else {
				vm.notRegistered = true;
			}
		});
	}
	getHubPlugins();

/*	vm.buttonCheck = buttonCheck;
	function buttonCheck (s) {
		s.$applyAsync (function() {
			var index = 0;
			var counter = 0;
			[].slice.call (document.querySelectorAll (".progress-button")).forEach (function (bttn, pos) {
				var prog = new UIProgressButton (bttn, {
					callback: function (instance) {
					}
				});
				if (counter === 0) {
					vm.plugins[index].installButton = prog;
				}
				else {
					vm.plugins[index].uninstallButton = prog;
				}
				counter = (counter + 1) % 2;
				if (counter === 0) {
					++index;
				}
			});
		});
	}*/

	function getInstalledHubPlugins() {
		BazaarSrv.getInstalledHubPlugins().success (function (data) {
			vm.installedHubPlugins = data;
			for (var i = 0; i < vm.plugins.length; ++i) {
				vm.plugins[i].installed = false;
				for (var j = 0; j < vm.installedHubPlugins.length; ++j) {
					if (vm.plugins[i].name === vm.installedHubPlugins[j].name) {
						vm.plugins[i].installed = true;
						vm.plugins[i].launch = true;
						vm.plugins[i].hubId = vm.installedHubPlugins[j].id;
						break;
					}
				}
			}
			$scope.$applyAsync (function() {
				var index = 0;
				var counter = 0;
				[].slice.call (document.querySelectorAll (".progress-button")).forEach (function (bttn, pos) {
					var prog = new UIProgressButton (bttn, {
						callback: function (instance) {
						}
					});
					if (counter === 0) {
						vm.plugins[index].installButton = prog;
					}
					else {
						vm.plugins[index].uninstallButton = prog;
					}
					counter = (counter + 1) % 2;
					if (counter === 0) {
						++index;
					}
				});
			});
		});
	}

	vm.currentHubPlugin = {};
	vm.showPluginInfo = showPluginInfo;
	function showPluginInfo (plugin) {
		vm.currentHubPlugin = plugin;
		ngDialog.open ({
			template: "subutai-app/bazaar/partials/pluginInfo.html",
			scope: $scope
		});
	}





	vm.newPlugin = {};
	vm.currentPlugin = {};
	vm.isNew = false;
	vm.step = "upload";
	vm.permissions = [
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
		}
	];
	vm.permissions2Add = [];


	vm.installedPlugins = [];
	vm.uploadPluginWindow = uploadPluginWindow;
	vm.getInstalledPlugins = getInstalledPlugins;
	vm.deletePlugin = deletePlugin;
	vm.editPermissionsWindow = editPermissionsWindow;
	vm.addPermission2Stack = addPermission2Stack;
	vm.removePermissionFromStack = removePermissionFromStack;
	vm.editPermissions = editPermissions;
	vm.uploadPlugin = uploadPlugin;



	function addPermission2Stack(permission) {
		vm.permissions2Add.push(angular.copy(permission));
		for (var i = 0; i < vm.permissions.length; ++i) {
			if (vm.permissions[i].name === permission.name) {
				vm.permissions.splice (i, 1);
				break;
			}
		}
	}

	function removePermissionFromStack(key) {
		vm.permissions.push (vm.permissions2Add[key]);
		vm.permissions2Add.splice(key, 1);
	}

	function editPermissions() {
		console.log (vm.permissions2Add);
		var postData = 'pluginId=' + vm.currentPlugin.id;

		if(vm.permissions2Add.length > 0) {
			postData += '&permission=' + JSON.stringify (vm.permissions2Add);
		}

		BazaarSrv.editPermissions (postData).success(function (data) {
			ngDialog.closeAll();
			getInstalledPlugins();
			SweetAlert.swal ("Success!", "Your permissions were updated.", "success");
		}).error (function (error) {
			SweetAlert.swal ("ERROR!", "Permission update error: " + error.replace(/\\n/g, " "), "error");
		});
	}

	function uploadPlugin() {
		ngDialog.closeAll();
		BazaarSrv.uploadPlugin (vm.newPlugin.name, vm.newPlugin.version, karUploader, JSON.stringify(vm.permissions2Add)).success (function (data) {
			SweetAlert.swal ("Success!", "Your plugin was installed.", "success");
			vm.newPlugin = {};
			vm.permissions2Add = {};
			karUploader = {};
			getInstalledPlugins();
			ngDialog.closeAll();
		}).error (function (error) {
			SweetAlert.swal ("ERROR!", "Plugin install error: " + error.replace(/\\n/g, " "), "error");
		});
	}






	function getInstalledPlugins() {
		vm.installedPlugins = [];
		BazaarSrv.getInstalledPlugins().success (function (data) {
			vm.installedPlugins = data;
		});
	}
	getInstalledPlugins();


	function uploadPluginWindow() {
		vm.step = "upload";
		ngDialog.closeAll();
		ngDialog.open ({
			template: "subutai-app/bazaar/partials/uploadPlugin.html",
			scope: $scope
		});
	}

	function editPermissionsWindow (plugin, isNew) {
		vm.permissions = [
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
			}
		];
		if (isNew) {
			for (var i = 0; i < vm.installedPlugins.length; ++i) {
				if (vm.installedPlugins[i].name === plugin.name) {
					SweetAlert.swal ("ERROR!", "Plugin with such name already exists", "error");
					return;
				}
			}
			vm.permissions2Add = [];
		}
		else {
			vm.currentPlugin = plugin;
			BazaarSrv.getPermissions (vm.currentPlugin.id).success (function (data) {
				vm.permissions2Add = data.permissions;
				for(var i = 0; i < vm.permissions2Add.length; i++) {
					for(var j = 0; j < vm.permissions.length; j++) {
						if(vm.permissions[j].object === vm.permissions2Add[i].object) {
							vm.permissions2Add[i].name = vm.permissions[j].name;
							vm.permissions.splice (j, 1);
							--j
							break;
						}
					}
				}
				ngDialog.open ({
					template: "subutai-app/bazaar/partials/uploadPlugin.html",
					scope: $scope
				});
			});
		}
		vm.isNew = isNew;
		vm.step = "perms";
	}


	function deletePlugin (plugin) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Your will not be able to recover this plugin!",
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
				BazaarSrv.deletePlugin (plugin.id).success (function (data) {
					SweetAlert.swal ("Success!", "Your plugin was deleted.", "success");
					getInstalledPlugins();
					ngDialog.closeAll();
				}).error (function (error) {
					SweetAlert.swal ("ERROR!", "Plugin delete error: " + error.replace(/\\n/g, " "), "error");
				});
			}
		});
	}





	vm.installPlugin = installPlugin;
	function installPlugin (plugin) {
		plugin.installButton.options.callback = function (instance) {
			if (plugin.dependencies.length > 0) {
				SweetAlert.swal({
					title: "Additional dependencies",
					text: "It seems that there are dependencies that need to be installed. Are you sure you want to continue?",
					type: "warning",
					showCancelButton: true,
					confirmButtonColor: "#ff3f3c",
					confirmButtonText: "Install",
					cancelButtonText: "Cancel",
					closeOnConfirm: true,
					closeOnCancel: true,
					showLoaderOnConfirm: false
				},
				function (isConfirm) {
					if (isConfirm) {
						var progress = 0,
							interval = setInterval (function() {
								progress = Math.min (progress + Math.random() * 0.1, 0.99);
								instance.setProgress (progress);
			/*					if( progress === 0.99 ) {
									progress = 1;
									instance.stop(  -1 );
									clearInterval( interval );
								}*/
							}, 150);
						var installPluginDependencies = function (dependencies, callback) {
							console.log (dependencies);
							console.log (callback);
							for (var i = 0; i < dependencies.length; ++i) {
								for (var j = 0; j < vm.plugins.length; ++j) {
									if (dependencies[i] === vm.plugins[j].id) {
										installPluginDependencies (vm.plugins[j].dependencies, function() {
											return;
										});
										BazaarSrv.installHubPlugin (vm.plugins[j]).success (function (data) {;
											callback();
										});
									}
								}
							}
						}
						installPluginDependencies (plugin.dependencies, function() {
							BazaarSrv.installHubPlugin (plugin).success (function (data) {
								setTimeout (function() {
									progress = 1;
									instance.stop (1);
									clearInterval (interval);
									setTimeout (function() {
										localStorage.setItem ("bazaarScroll", plugin.id);
										$rootScope.$emit('reloadPluginsStates');
									}, 2000);
								}, 2000);
							}).error (function (error) {
								instance.stop (-1);
								clearInterval (interval);
							});
						});
					}
					else {
						instance.stop (-1);
					}
				});
			}
			else {
				var progress = 0,
					interval = setInterval (function() {
						progress = Math.min (progress + Math.random() * 0.1, 0.99);
						instance.setProgress (progress);
	/*					if( progress === 0.99 ) {
							progress = 1;
							instance.stop(  1 );
							clearInterval( interval );
						}*/
					}, 150);
				BazaarSrv.installHubPlugin (plugin).success (function (data) {
					setTimeout (function() {
						progress = 1;
						instance.stop (1);
						clearInterval (interval);
						setTimeout (function() {
							localStorage.setItem ("bazaarScroll", plugin.id);
							$rootScope.$emit('reloadPluginsStates');
						}, 2000);
					}, 2000);
				}).error (function (error) {
					instance.stop (-1);
					clearInterval (interval);
				});
			}
		};
	}


	vm.uninstallPlugin = uninstallPlugin;
	function uninstallPlugin (plugin) {
		plugin.launch = false;
		plugin.uninstallButton.options.callback = function (instance) {
			var progress = 0,
				interval = setInterval (function() {
					progress = Math.min (progress + Math.random() * 0.1, 0.99);
					instance.setProgress (progress);
/*					if( progress === 0.99 ) {
						progress = 1;
						instance.stop(  1 );
						clearInterval( interval );
					}*/
				}, 150);
			BazaarSrv.uninstallHubPlugin (plugin).success (function (data) {
				setTimeout (function() {
					progress = 1;
					instance.stop (1);
					clearInterval (interval);
					setTimeout (function() {
						localStorage.setItem ("bazaarScroll", plugin.id);
						$rootScope.$emit('reloadPluginsStates');
					}, 2000);
				}, 2000);
			}).error (function (error) {
				instance.stop (-1);
				clearInterval (interval);
			});
		};
	}

	vm.redirectToPlugin = redirectToPlugin;
	function redirectToPlugin (url) {
		console.log (url);
		$location.path ("/plugins/" + url);
	}


	vm.registerPeer = registerPeer;
	function registerPeer() {
		BazaarSrv.registerPeer().success (function (data) {
			SweetAlert.swal ("Success!", "Your peer was registered.", "success");
		}).error (function (error) {
			SweetAlert.swal ("ERROR!", "Peer registration failed. Please, check the procedure of registering peer and come back again.", "error");
		});
	}


	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	vm.refOldPlugins = [];

	function getRefOldPlugins() {
		console.log ("here");
		try {
			BazaarSrv.getRefOldPlugins().success(function(data) {
				vm.refOldPlugins = data;
			});
		} catch(e) {}
	}
	getRefOldPlugins();



}


function fileModel($parse) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;
			element.bind('change', function(){
				scope.$apply(function(){
					modelSetter(scope, element[0].files[0]);
					if (element[0].files[0].name.substring (element[0].files[0].name.length - 4, element[0].files[0].name.length) !== ".kar") {
						document.getElementById ("filename").value = "Wrong file type";
						document.getElementById ("filename").style.color = "red";
					}
					else {
						karUploader = element[0].files[0];
						console.log (karUploader);
						document.getElementById ("filename").value = karUploader.name;
						document.getElementById ("filename").style.color = "#04346E";
					}
				});
			});
		}
	};
}
