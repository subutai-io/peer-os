'use strict';

angular.module('subutai.console.controller', [])
	.controller('ConsoleViewCtrl', ConsoleViewCtrl)
	.config(['terminalConfigurationProvider', function (terminalConfigurationProvider) {

		terminalConfigurationProvider.config('modern').allowTypingWriteDisplaying = false;
		terminalConfigurationProvider.config('modern').outputDelay = 80;
		//terminalConfigurationProvider.config('vintage').typeSoundUrl ='example/content/type.wav';
		//terminalConfigurationProvider.config('vintage').startSoundUrl ='example/content/start.wav';
	}]);

ConsoleViewCtrl.$inject = ['$scope', 'consoleService', 'peerRegistrationService', '$stateParams', 'ngDialog'];

function ConsoleViewCtrl($scope, consoleService, peerRegistrationService, $stateParams, ngDialog) {

	var vm = this;	
	vm.currentType = 'peer';
	vm.activeConsole = false;
	vm.hosts = [];
	vm.environments = [];
	vm.containers = [];
	vm.currentTab = '';
	vm.daemon = false;
	vm.timeOut = 0;
	vm.selectedEnvironment = '';

	if($stateParams.containerId !== undefined && $stateParams.containerId.length > 0) {
		vm.activeConsole = $stateParams.containerId;
	}

	peerRegistrationService.getResourceHosts().success(function (data) {
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
	});

	//Console UI
	$scope.theme = 'modern';
	setTimeout(function () {
		$scope.$broadcast('terminal-output', {
			output: true,
			text: [
				'Subutai Social',
			],
			breakLine: true
		});
		$scope.prompt.path('/');

		if(vm.activeConsole) {
			$scope.prompt.user(vm.activeConsole);
		}

		$scope.$apply();
	}, 100);

	$scope.session = {
		commands: [],
		output: [],
		$scope:$scope
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

			consoleService.sendCommand(cmd.command, vm.activeConsole, $scope.prompt.path(), vm.daemon, vm.timeOut, vm.selectedEnvironment).success(function(data){
				if(data.stdErr.length > 0) {
					output = data.stdErr.split('\r');
				} else {
					output = data.stdOut.split('\r');
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
				$scope.session.output.push({ output: true, breakLine: true, text: [data] });
			});

		} catch (err) {
			$scope.session.output.push({ output: true, breakLine: true, text: [err.message] });
		}
	});
	//END Console UI

	//functions
	vm.setCurrentType = setCurrentType;
	vm.setConsole = setConsole;
	vm.showContainers = showContainers;
	vm.showSSH = showSSH;

	function setConsole(node) {
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

}

