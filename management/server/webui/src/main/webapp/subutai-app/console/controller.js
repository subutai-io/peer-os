'use strict';

angular.module('subutai.console.controller', [])
	.controller('ConsoleViewCtrl', ConsoleViewCtrl)
	.config(['terminalConfigurationProvider', function (terminalConfigurationProvider) {

		terminalConfigurationProvider.config('modern').outputDelay = 80;
		terminalConfigurationProvider.config('modern').allowTypingWriteDisplaying = false;
		//terminalConfigurationProvider.config('vintage').typeSoundUrl ='example/content/type.wav';
		//terminalConfigurationProvider.config('vintage').startSoundUrl ='example/content/start.wav';
	}]);

ConsoleViewCtrl.$inject = ['$scope', 'consoleService'];

function ConsoleViewCtrl($scope, consoleService) {

	//Console UI
	$scope.theme = 'modern';
	setTimeout(function () {
		$scope.$broadcast('terminal-output', {
			output: true,
			text: [
				'Wake up, Neo...',
			],
			breakLine: true
		});
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
		var cmd = consoleInput[0];

		try {
			console.log(cmd);
		} catch (err) {
			$scope.session.output.push({ output: true, breakLine: true, text: [err.message] });
		}
	});
	//END Console UI


	var vm = this;	
	vm.currentType = 'resourceHosts';
	vm.activeConsole = [];

	vm.resourceHosts = [];

	//functions
	vm.getSubMenuTree = getSubMenuTree;
	vm.setConsole = setConsole;

	function setConsole(node) {
		vm.activeConsole.push(node);
	}

	function getResourceHosts() {
		consoleService.getResourceHosts().success(function (data) {
			for(var i = 0; i < data.length; i++) {
				var currebtNode = createJsTreeNode(data[i]);
				vm.resourceHosts.push(currebtNode);
			}
			return;
		});
	}

	function getEnvironments() {
		consoleService.getEnvironments().success(function (data) {
			for(var i = 0; i < data.length; i++) {
				var currebtNode = createJsTreeNode(data[i]);
				vm.resourceHosts.push(currebtNode);
			}
			return;
		});
	}

	function getSubMenuTree() {
		vm.resourceHosts = [];
		if(vm.currentType == 'resourceHosts') {
			getResourceHosts();
		} else {
			getEnvironments();
		}
	}

	getSubMenuTree();

	function createJsTreeNode(json) {
		var jsTreeNode = {};
		if(json.hostname !== undefined) {
			jsTreeNode.title = json.hostname;
		}
		if(json.name !== undefined) {
			jsTreeNode.title = json.name;
		}		
		if(json.id !== undefined) {
			jsTreeNode.id = json.id;
		}

		if(vm.currentType != 'resourceHosts' && json.containers !== undefined && json.containers.length > 0) {
			jsTreeNode.nodes = [];
			for(var i = 0; i < json.containers.length; i++) {
				var childNode = createJsTreeNode(json.containers[i]);
				jsTreeNode.nodes.push(childNode);
			}
		}
		return jsTreeNode;
	}

}
