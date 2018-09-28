'use strict';

angular.module('subutai.console.controller', [])
	.controller('ConsoleViewCtrl', ConsoleViewCtrl)
	.config(['terminalConfigurationProvider', function (terminalConfigurationProvider) {

		terminalConfigurationProvider.config('modern').allowTypingWriteDisplaying = false;
		terminalConfigurationProvider.config('modern').outputDelay = 80;
	}]);

ConsoleViewCtrl.$inject = ['$scope', 'consoleService', '$stateParams', 'ngDialog', 'cfpLoadingBar'];

function ConsoleViewCtrl($scope, consoleService, $stateParams, ngDialog, cfpLoadingBar) {

	var vm = this;

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	vm.currentType = 'environments';
	vm.admin = false,
	vm.activeConsole = false;
	vm.hosts = [];
	vm.environments = [];
	vm.containers = [];
	vm.currentTab = '';
	vm.daemon = false;
	vm.timeOut = 30;
	vm.selectedEnvironment = '';
	vm.selectedNodeType = '';
	vm.selectedContainer = '';
	vm.selectedPeer = '';

	if($stateParams.containerId !== undefined && $stateParams.containerId.length > 0) {
		setCurrentType('environments');
		vm.activeConsole = $stateParams.containerId;
		vm.selectedContainer = $stateParams.containerId;
		vm.selectedEnvironment = $stateParams.environmentId;
	}

	consoleService.getResourceHosts().success(function (data) {
		vm.hosts = data;
		for(var i = 0; i < vm.hosts.length; i++) {
			if(vm.hosts[i].hostname == 'management') {
				var temp = angular.copy(vm.hosts[0]);
				vm.hosts[0] = angular.copy(vm.hosts[i]);
				vm.hosts[i] = temp;
			}
		}
	});

	consoleService.getEnvironments().success(function (data) {
		vm.environments = data;
		if(vm.selectedContainer.length > 0) {
			showContainers(vm.selectedEnvironment);
		}
	});

	if( localStorage.getItem("currentUserPermissions") )
		for( var i = 0; i < localStorage.getItem("currentUserPermissions").length; i++ ) {
			if (localStorage.getItem("currentUserPermissions")[i] == 2) {
				vm.activeTab = "peer";
				vm.admin = true;
			}
		}

	//Console UI
	$scope.theme = 'modern';
	setTimeout(function () {
		$scope.$broadcast('terminal-output', {
			output: true,
			text: [
				'Subutai',
			],
			breakLine: true
		});
		$scope.prompt.path('/');

		if(vm.activeConsole) {
			$scope.prompt.user(vm.activeConsole);
		}

		$scope.$apply();
		$('.terminal-viewport').perfectScrollbar();
	}, 100);

	$scope.session = {
		commands: [],
		output: []
	};

	$scope.$watchCollection(function () { return $scope.session.commands; }, function (n) {
		for (var i = 0; i < n.length; i++) {
			$scope.$broadcast('terminal-command', n[i]);
		}
		$scope.session.commands.splice(0, $scope.session.commands.length);
		$scope.$$phase || $scope.$apply();
	});

	$scope.$watchCollection(function () { return $scope.session.output; }, function (n) {
		for (var i = 0; i < n.length; i++) {
			$scope.$broadcast('terminal-output', n[i]);
		}
		$scope.session.output.splice(0, $scope.session.output.length);
		$scope.$$phase || $scope.$apply();
	});

	$scope.$on('terminal-input', function (e, consoleInput) {
		var output = [];
		$scope.outputDelay = 0;

		$scope.showPrompt = false;

		if(!vm.activeConsole) {
			output.push('Select peer or environment container');
			$scope.session.output.push(
				{ output: true, text: output, breakLine: true }
			);
			return;
		}

		var cmd = consoleInput[0];

		try {
			if (cmd.command =='clear') {
				$scope.results.splice(0, $scope.results.length);
				$scope.$$phase || $scope.$apply();
				return;
			}

			var workingDir = $scope.prompt.path().replace("~", "root")

			consoleService.sendCommand(cmd.command, vm.activeConsole, workingDir, vm.daemon, vm.timeOut, vm.selectedEnvironment).success(function(data){
				output = [];
				if(data.stdOut.length > 0) {
					output = data.stdOut.split('\r');
				}
				for(var i = 0; i < output.length; i++){
				    output[i] = escapeHTML(output[i])
				}
				if(data.stdErr.length > 0) {
					var errors = data.stdErr.split('\r');
					for(var i = 0; i < errors.length; i++) {
						errors[i] = '<span style="color: #ff0000;">' + errors[i] + '</span>';
					}
					output = output.concat( errors );
				}

				var checkCommand = cmd.command.split(' ');
				if (checkCommand[0] == 'cd' && data.status == 'SUCCEEDED') {
					var currentPath = $scope.prompt.path();
					if(checkCommand[1].substring(0, 1) == '/') {
						currentPath = '';
					}
					var pathArray = (currentPath + checkCommand[1]).split('/');
					var totalPath = [];
					for(var i = 0; i < pathArray.length; i++) {
						if(pathArray[i].length > 0 && pathArray[i] != '&&') {
							if(pathArray[i] == '..') {
								totalPath.pop();
							} else if(pathArray[i] != '.') {
								totalPath.push(pathArray[i]);
							}
						}
					}

					var pathString = '/';
					for(var j = 0; j < totalPath.length; j++) {
						pathString += totalPath[j] + '/';
					}

					$scope.prompt.path(pathString);
				}

				$scope.session.output.push(
					{ output: true, text: output, breakLine: true }
				);
			}).error(function (data) {
				$scope.session.output.push({ output: true, breakLine: true, text: [data.ERROR] });
			});

		} catch (err) {
			$scope.session.output.push({ output: true, breakLine: true, text: [err.message] });
		}
	});

	function escapeHTML(html) {
        return document.createElement('div').appendChild(document.createTextNode(html)).parentNode.innerHTML;
    }
	//END Console UI

	//functions
	vm.setCurrentType = setCurrentType;
	vm.setConsole = setConsole;
	vm.showContainers = showContainers;
	vm.showSSH = showSSH;
	vm.getBaseUrl = getBaseUrl;

	function setConsole(node, nodeType) {
		if(nodeType === undefined || nodeType === null) nodeType = 'host';
		vm.selectedNodeType = nodeType;
		vm.activeConsole = node;
		$scope.results.splice(0, $scope.results.length);
		$scope.$$phase || $scope.$apply();
		$scope.prompt.user(node);
		/*if(vm.activeConsole.indexOf(node) == -1) {
			vm.activeConsole.push(node);
		}*/
	}

	function setCurrentType(type) {
		vm.containers = [];
		vm.selectedEnvironment = '';
		vm.selectedPeer = '';
		vm.selectedContainer = '';
		vm.selectedEnvironment = '';
		vm.selectedNodeType = '';
		vm.showSSHCommand = '';
		vm.currentType = type;
	}

	function showSSH() {
		if(vm.activeConsole.length > 0) {
			LOADING_SCREEN();
			consoleService.getSSH(vm.selectedEnvironment, vm.activeConsole).success(function (data) {
				vm.showSSHCommand = data;
				ngDialog.open({
					template: 'subutai-app/console/partials/sshPopup.html',
					scope: $scope
				});
				LOADING_SCREEN('none');
			}).error(function(error){
				console.log(error);
				SweetAlert.swal("ERROR!", error, "error");
				LOADING_SCREEN('none');
			});
		}
	}

	function showContainers(environmentId) {
		vm.containers = [];
		for(var i in vm.environments) {
			if(environmentId == vm.environments[i].id) {
				vm.containers = vm.environments[i].containers;
				break;
			}
		}
	}

	function getBaseUrl() {
		var pathArray = location.href.split( '/' );
		var protocol = pathArray[0];
		var hostWithPort = pathArray[2].split(':');
		var host = hostWithPort[0];
		//var url = protocol + '//' + host;
		var url = host;
		return url;
	}

}

