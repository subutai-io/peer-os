'use strict';

angular.module('subutai.console.controller', [])
	.controller('ConsoleViewCtrl', ConsoleViewCtrl)
	.config(['terminalConfigurationProvider', function (terminalConfigurationProvider) {

		terminalConfigurationProvider.config('modern').allowTypingWriteDisplaying = false;
		terminalConfigurationProvider.config('modern').outputDelay = 80;
		//terminalConfigurationProvider.config('vintage').typeSoundUrl ='example/content/type.wav';
		//terminalConfigurationProvider.config('vintage').startSoundUrl ='example/content/start.wav';
	}]);

ConsoleViewCtrl.$inject = ['$scope', 'consoleService', 'peerRegistrationService'];

function ConsoleViewCtrl($scope, consoleService, peerRegistrationService) {

	var vm = this;	
	vm.currentType = 'peer';
	vm.activeConsole = false;
	vm.hosts = [];
	vm.environments = [];
	vm.containers = [];
	vm.currentTab = '';

	peerRegistrationService.getResourceHosts().success(function (data) {
		vm.hosts = data;
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
		console.log($scope);

		if(!vm.activeConsole) {
			output.push('Select peer or environment container');
			$scope.session.output.push(
				{ output: true, text: output, breakLine: true }
			);
			return;
		}

		var cmd = consoleInput[0];

		try {
			console.log(cmd);
			if (cmd.command =='clear') {
				$scope.results.splice(0, $scope.results.length);
				$scope.$$phase || $scope.$apply();
				return;
			}

			consoleService.sendCommand(cmd.command, vm.activeConsole, $scope.prompt.path()).success(function(data){
				console.log(data);
				if(data.stdErr.length > 0) {
					output = data.stdErr.split('\r');
				} else {
					output = data.stdOut.split('\r');
				}

				var checkCommand = cmd.command.split(' ');
				if (checkCommand[0] == 'cd' && data.status == 'SUCCEEDED') {
					var pathArray = ($scope.prompt.path() + checkCommand[1]).split('/');
					var totalPath = [];
					console.log(pathArray);
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
		vm.currentType = type;
	}

	function showContainers(environmentId) {
		vm.containers = [];
		for(var i in vm.environments) {
			if(environmentId == vm.environments[i].id) {
				vm.containers = vm.environments[i].containers;
				console.log(vm.containers);
				break;
			}
		}
	}

}
