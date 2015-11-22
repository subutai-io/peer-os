/**
 * Created by ubuntu on 5/15/15.
 */
'use strict';

angular.module('subutai.metrics.controller', ['jsTree.directive'])
.controller('MetricsCtrl', MetricsCtrl)
.filter('chartID', function () {
	return function() {
		// filter function; probably we'll need it later
	}

});

MetricsCtrl.$inject = ['metricsSrv', '$scope'];
function MetricsCtrl(metricsSrv, $scope) {
	var vm = this;
	var chartOptions;

	var timeArray = [],
	uniqueTime = [],
		cpuArray = [],
		ramArray = [],
			datasetArray = [],
			parsedValues = [];

	var cpuData = {},
	ramData = {},
		datasetData = {};

	var ctx1, ctx2, ctx3;

	vm.parseJsonData = parseJsonData;
	vm.parseEnvJson = parseEnvJson;
	vm.checkNode = checkNode;
	vm.buildChart = buildChart;
	vm.buildEnvChart = buildEnvChart;
	vm.reset = reset;

	metricsSrv.getChartData().success(function(data) {
		vm.charts = data;
	});

	metricsSrv.getChartOptions().success(function (data) {
		chartOptions = data;
	});

	metricsSrv.getEnvironments().success(function (data) {
		vm.environments = data;
	});

	function parseJsonData(chart) {

		for(var i = 0; i < chart[0].metrics.length; i++) {
			timeArray.push(chart[0].metrics[i].time);
			$.each(timeArray, function(i, el){
				if($.inArray(el, uniqueTime) === -1) uniqueTime.push(el);
			});
		}
		for(var x = 0; x < chart.length; x++) {
			for(var y = 0; y < chart[x].metrics.length; y++) {
				cpuArray.push(chart[x].metrics[y].cpu);
				ramArray.push(chart[x].metrics[y].ram);
				datasetArray.push(chart[x].metrics[y].dataset);
			}
		}

		cpuData = {
			labels: uniqueTime,
			datasets: [
			{
				label: "CPU metrics",
				fillColor: "rgba(0, 255, 0,0.5)",
				strokeColor: "rgba(0, 255, 0,1)",
				pointColor: "rgba(0, 255, 0,1)",
				pointStrokeColor: "#fff",
				pointHighlightFill: "#fff",
				pointHighlightStroke: "rgba(0, 255, 0,1)",
				data: cpuArray.splice(0,3)
			},
			{
				label: "CPU metrics",
				fillColor: "rgba(0, 204, 153,0.5)",
				strokeColor: "rgba(0, 204, 153,1)",
				pointColor: "rgba(0, 204, 153,1)",
				pointStrokeColor: "#fff",
				pointHighlightFill: "#fff",
				pointHighlightStroke: "rgba(0, 204, 153,1)",
				data: cpuArray.splice(0,6)
			}
			]
		};
		ramData = {
			labels: uniqueTime,
			datasets: [
			{
				label: "RAM metrics",
				fillColor: "rgba(0, 255, 0,0.5)",
				strokeColor: "rgba(0, 255, 0,1)",
				pointColor: "rgba(0, 255, 0,1)",
				pointStrokeColor: "#fff",
				pointHighlightFill: "#fff",
				pointHighlightStroke: "rgba(0, 255, 0,1)",
				data: ramArray.splice(0,3)
			},
			{
				label: "RAM metrics",
				fillColor: "rgba(0, 204, 153,0.5)",
				strokeColor: "rgba(0, 204, 153,1)",
				pointColor: "rgba(0, 204, 153,1)",
				pointStrokeColor: "#fff",
				pointHighlightFill: "#fff",
				pointHighlightStroke: "rgba(0, 204, 153,1)",
				data: ramArray.splice(0,6)
			}
			]
		};

		datasetData = {
			labels: uniqueTime,
			datasets: [
			{
				label: "DATASET metrics",
				fillColor: "rgba(0, 255, 0,0.5)",
				strokeColor: "rgba(0, 255, 0,1)",
				pointColor: "rgba(0, 255, 0,1)",
				pointStrokeColor: "#fff",
				pointHighlightFill: "#fff",
				pointHighlightStroke: "rgba(0, 255, 0,1)",
				data: datasetArray.splice(0,3)
			},
			{
				label: "DATASET metrics",
				fillColor: "rgba(0, 204, 153,0.5)",
				strokeColor: "rgba(0, 204, 153,1)",
				pointColor: "rgba(0, 204, 153,1)",
				pointStrokeColor: "#fff",
				pointHighlightFill: "#fff",
				pointHighlightStroke: "rgba(0, 204, 153,1)",
				data: datasetArray.splice(0,6)
			}
			]
		};
		return parsedValues = [uniqueTime, cpuData, ramData, datasetData];
	}
	function parseEnvJson(chart) {
		for(var i = 0; i < chart[0].lxcs[0].metrics.length; i++) {
			timeArray.push(chart[0].lxcs[0].metrics[i].time);
			$.each(timeArray, function(i, el){
				if($.inArray(el, uniqueTime) === -1) uniqueTime.push(el);
			});
		}
		for(var x = 0; x < chart[0].lxcs.length; x++) {
			for(var y = 0; y < chart[0].lxcs[0].metrics.length; y++) {
				cpuArray.push(chart[0].lxcs[x].metrics[y].cpu);
				ramArray.push(chart[0].lxcs[x].metrics[y].ram);
				datasetArray.push(chart[0].lxcs[x].metrics[y].dataset);
			}
		}
		cpuData = {
			labels: uniqueTime,
			datasets: [
			{
				label: "CPU metrics",
				fillColor: "rgba(255, 102, 0,0.5)",
				strokeColor: "rgba(255, 102, 0,1)",
				pointColor: "rgba(255, 102, 0,1)",
				pointStrokeColor: "#fff",
				pointHighlightFill: "#fff",
				pointHighlightStroke: "rgba(255, 102, 0,1)",
				data: cpuArray.splice(0,3)
			},
			{
				label: "CPU metrics",
				fillColor: "rgba(255, 204, 0,0.5)",
				strokeColor: "rgba(255, 204, 0,1)",
				pointColor: "rgba(255, 204, 0,1)",
				pointStrokeColor: "#fff",
				pointHighlightFill: "#fff",
				pointHighlightStroke: "rgba(255, 204, 0,1)",
				data: cpuArray.splice(0,6)
			}
			]
		};
		ramData = {
			labels: uniqueTime,
			datasets: [
			{
				label: "RAM metrics",
				fillColor: "rgba(255, 102, 0,0.5)",
				strokeColor: "rgba(255, 102, 0,1)",
				pointColor: "rgba(255, 102, 0,1)",
				pointStrokeColor: "#fff",
				pointHighlightFill: "#fff",
				pointHighlightStroke: "rgba(255, 102, 0,1)",
				data: ramArray.splice(0,3)
			},
			{
				label: "RAM metrics",
				fillColor: "rgba(255, 204, 0,0.5)",
				strokeColor: "rgba(255, 204, 0,1)",
				pointColor: "rgba(255, 204, 0,1)",
				pointStrokeColor: "#fff",
				pointHighlightFill: "#fff",
				pointHighlightStroke: "rgba(255, 204, 0,1)",
				data: ramArray.splice(0,6)
			}
			]
		};

		datasetData = {
			labels: uniqueTime,
			datasets: [
			{
				label: "DATASET metrics",
				fillColor: "rgba(255, 102, 0,0.5)",
				strokeColor: "rgba(255, 102, 0,1)",
				pointColor: "rgba(255, 102, 0,1)",
				pointStrokeColor: "#fff",
				pointHighlightFill: "#fff",
				pointHighlightStroke: "rgba(255, 102, 0,1)",
				data: datasetArray.splice(0,3)
			},
			{
				label: "DATASET metrics",
				fillColor: "rgba(255, 204, 0,0.5)",
				strokeColor: "rgba(255, 204, 0,1)",
				pointColor: "rgba(255, 204, 0,1)",
				pointStrokeColor: "#fff",
				pointHighlightFill: "#fff",
				pointHighlightStroke: "rgba(255, 204, 0,1)",
				data: datasetArray.splice(0,6)
			}
			]
		};
		return parsedValues = [uniqueTime, cpuData, ramData, datasetData];
	}
	function checkNode(node) {
		if(node == 'lxc1') {
			cpuData.datasets.shift();
			ramData.datasets.shift();
			datasetData.datasets.shift();
		}
		else if(node == 'lxc2') {
			cpuData.datasets.pop();
			ramData.datasets.pop();
			datasetData.datasets.pop();
		}
	}
	function buildChart(parsedValuesArray, options) {
		ctx1 = $("#cpuCanvas").get(0).getContext("2d");
		ctx2 = $("#ramCanvas").get(0).getContext("2d");
		ctx3 = $("#datasetCanvas").get(0).getContext("2d");
		new Chart(ctx1).Line(parsedValuesArray[1], options);
		new Chart(ctx2).Line(parsedValuesArray[2], options);
		new Chart(ctx3).Line(parsedValuesArray[3], options);
	}
	function buildEnvChart() {
		buildChart(parseEnvJson(vm.environments), chartOptions);
		return 0;
	}
	function reset() {
		$scope.envDropdown = '';
		$scope.form.$setPristine(true); //setPristine not working, but not showing errors as well
	}
	$scope.selectedNode = function(e,data) {
		reset();
		if(data.node.parent === "#") {
			for(var i = 0; i < vm.charts.length; i++) {
				buildChart(parseJsonData(vm.charts), chartOptions);
			}
		}
		else {
			for(var j = 0; j < vm.charts.length; j++){
				if(data.node.id === vm.charts[j].id) {
					var chart = parseJsonData(vm.charts);
					checkNode(data.node.id);
					buildChart(chart, chartOptions);
				}
			}
		}
	}
}
