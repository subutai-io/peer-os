'use strict';

angular.module('subutai.monitoring.controller', [])
	.controller('MonitoringCtrl', MonitoringCtrl);

MonitoringCtrl.$inject = ['$scope', 'monitoringSrv'];

function MonitoringCtrl($scope, monitoringSrv) {

	var vm = this;
	vm.currentType = 'peer';
	vm.charts = [{}, {}, {}, {}];
	vm.environments = [];
	vm.containers = [];
	vm.hosts = [];
	vm.selectedEnvironment = '';
	vm.currentHost = '';
	vm.period = 1;

	//functions
	vm.showContainers = showContainers;
	vm.setCurrentType = setCurrentType;
	vm.getServerData = getServerData;

	monitoringSrv.getEnvironments().success(function (data) {
		vm.environments = data;
	});

	monitoringSrv.getResourceHosts().success(function (data) {
		vm.hosts = data;
		for(var i = 0; i < vm.hosts.length; i++) {
			if(vm.hosts[i].hostname == 'management') {
				var temp = angular.copy(vm.hosts[0]);
				vm.hosts[0] = angular.copy(vm.hosts[i]);
				vm.currentHost = vm.hosts[i].id;
				vm.hosts[i] = temp;
			}
		}
	});

	function setCurrentType(type) {
		vm.containers = [];
		vm.selectedEnvironment = '';
		vm.currentType = type;
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

	function getServerData() {
		if(vm.period > 0) {
			LOADING_SCREEN();
			monitoringSrv.getInfo(vm.selectedEnvironment, vm.currentHost, vm.period).success(function (data) {
				vm.charts = [];
				for(var i = 0; i < data.metrics.length; i++) {
					vm.charts.push(getChartData(data.metrics[i]));
				}
				//getServerData();
				LOADING_SCREEN('none');
			}).error(function(error){
				console.log(error);
				LOADING_SCREEN('none');
			});
		}
	}
	getServerData();

	vm.onClick = function (points, evt) {
		console.log(points, evt);
	};

	function getChartData(obj) {
		//console.log(obj);
		var result = {};
		result.values = [];
		result.labels = [];
		result.series = [];
		result.options = {};
		result.name = '';

		var setLabels = true;
		if(obj.series !== undefined) {

			result.name = obj.series[0].name;
			if(obj.series[0].name.indexOf('cpu') > -1) {
				result.options = { pointDot: false, scaleShowVerticalLines: false, scaleOverride: true, scaleStartValue: 0, scaleStepWidth: 20, scaleSteps: 5, showXLabelseByEveryMinutes: 10, legendTemplate: '', animation: false };
			} else {
				result.options = { pointDot: false, scaleShowVerticalLines: false, showXLabelseByEveryMinutes: 10, legendTemplate: '', animation: false };
			}

			for(var i = 0; i < obj.series.length; i++) {
				var currentValues = [];
				for(var j = 0; j < obj.series[i].values.length; j++) {
					if(setLabels) {
						//var label = obj.series[i].values[j][0];
						var label = moment(obj.series[i].values[j][0]).format('H:mm');
						result.labels.push(label);
					}
					//var value = Math.round(obj.series[i].values[j][1]).toFixed(2);
					currentValues.push(obj.series[i].values[j][1]);
				}
				setLabels = false;
				result.values.push(currentValues);
				result.series.push(obj.series[i].tags.type);
			}
		}
		return result;
	}
};

