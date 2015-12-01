'use strict';

angular.module('subutai.monitoring.controller', [])
	.controller('MonitoringCtrl', MonitoringCtrl);

MonitoringCtrl.$inject = ['$scope', 'monitoringSrv'];

function MonitoringCtrl($scope, monitoringSrv) {

	var vm = this;

	//functions
	vm.labels = [];
	vm.series = ['Series A', 'Series B'];
	vm.options = { pointDot: false };
	vm.data = [[], []];

	monitoringSrv.getInfo().success(function (data) {
		vm.cpu = data.metrics[0].series;
		vm.cpu.data = {};
		vm.cpu.data.values = [];
		vm.cpu.data.labels = [];
		vm.network = data.metrics[1].series;
		vm.memory = data.metrics[2].series;
		vm.disk = data.metrics[3].series;
		console.log(data);

		for(var j = 0; j < vm.cpu.length; j++) {
			var currentValues = [];
			for(var i = 0; i < vm.cpu[j].values.length; i++) {
				var label = '';
				if(i % 10 == 0) {
					label = 't';
				}
				vm.cpu.data.labels.push(label);
				currentValues.push(vm.cpu[j].values[i][1]);
			}
			vm.cpu.data.values.push(currentValues);
		}
		console.log(vm.cpu.data);
	});	

	vm.onClick = function (points, evt) {
		console.log(points, evt);
	};
};

