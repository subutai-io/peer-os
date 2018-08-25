'use strict';

angular.module('subutai.monitoring.controller', [])
	.controller('MonitoringCtrl', MonitoringCtrl);

MonitoringCtrl.$inject = ['$scope', 'monitoringSrv', 'cfpLoadingBar', '$http', '$sce', 'ngDialog', '$timeout'];

function MonitoringCtrl($scope, monitoringSrv, cfpLoadingBar, $http, $sce, ngDialog, $timeout) {

	var vm = this;

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	vm.currentType = 'environments';
	vm.isAdmin = false;

	vm.p2pRunning = false;

	vm.charts = [{}, {}, {}, {}];
	vm.environments = [];
	vm.containers = [];
	vm.hosts = [];
	vm.selectedEnvironment = '';
	vm.currentHost = '';
	vm.period = 1;
	vm.info = {};
	vm.p2pColor = false;
	vm.currentError = '';
	vm.parseOptions = {
		1: {labelStep: 10, valueStep: 1},
		6: {labelStep: 60, valueStep: 5},
		12: {labelStep: 120, valueStep: 10},
		24: {labelStep: 240, valueStep: 20},
		48: {labelStep: 480, valueStep: 40}
	};

	vm.statusTable = {
		"p2pStatuses": {
			"healthy": 0,
			"problems": 0,
			"notWork": 0,
		},
		"p2pUpdates": {
			"updated": 0,
			"normal": 0,
			"needUpdate": 0,
		}
	};

	vm.statusColors = [
		{"color": "#22b573", "text": "Already up-to-date", "status": "true", "statusText": "Healthy"},
		{"color": "#efc94c", "text": "Need update soon", "status": "WAIT", "statusText": "Problems"},
		{"color": "#c1272d", "text": "Update immediately", "status": "false", "statusText": "Not working"},
	];

	//functions
	vm.showContainers = showContainers;
	vm.setCurrentType = setCurrentType;
	vm.getServerData = getServerData;
	vm.getP2PStatus = getP2PStatus;
	vm.getServerDataAndP2PStatus = getServerDataAndP2PStatus;
	vm.initAccordeon = initAccordeon;
	vm.viewError = viewError;

	monitoringSrv.getEnvironments().success(function (data) {
		vm.environments = data;

		monitoringSrv.isAdminCheck().success(function (data) {
			if(data == true || data == 'true') {
				monitoringSrv.getResourceHosts().success(function (data) {
					vm.hosts = data;
					vm.isAdmin = true;
					vm.currentType = 'peer';
					vm.currentHost = vm.hosts.length > 0 ? vm.hosts[0].id : '';
                    getP2PStatus();
					getServerData();
				});
			} else {
				setFirstEnvByDefault();
			}
		});
	});

	function viewError(errorText) {
		vm.currentError = errorText;
		ngDialog.open({
			template: 'subutai-app/monitoring/partials/p2pErrorPopup.html',
			scope: $scope
		});
	}
	

	function setFirstEnvByDefault() {
		if(vm.environments.length > 0) {
			vm.selectedEnvironment = vm.environments[0].id;
			vm.containers = vm.environments[0].containers;
			if(vm.containers.length > 0) {
				vm.currentHost = vm.containers[0].id;
				getServerData();
			}
		}
	}

	function setCurrentType(type) {
		vm.containers = [];
		vm.selectedEnvironment = '';
		vm.currentHost = '';
		vm.currentType = type;

		if( type == 'management' ) {
			getServerData();
		}
	}

	function initAccordeon() {
		accordionInit();
	}

	function showContainers(environmentId) {
		vm.containers = [];
		for (var i in vm.environments) {
			if (environmentId == vm.environments[i].id) {
				vm.containers = vm.environments[i].containers;
				break;
			}
		}
	}

	function getServerDataAndP2PStatus(){
	    getServerData()

	    getP2PStatus()
	}

	function getP2PStatus(){
	    if (vm.currentHost) {
            monitoringSrv.isP2pRunning(vm.currentHost).success(function (data){
                vm.p2pRunning = (data == "true");
            }).error(function (error) {
                console.log(error);
                vm.p2pRunning = false;
            });
        }
	}

	function getServerData() {
		if (vm.period > 0 && ( vm.currentHost || vm.currentType == 'management' )) {
			LOADING_SCREEN();
			var env = vm.selectedEnvironment;
			var host = vm.currentHost;

			if( vm.currentType == 'management' )
			{
				env = "";
				host = "management";
			}

			monitoringSrv.getInfo(env, host, vm.period).success(function (data) {

				vm.charts = [];
				if(data['Metrics']) {
					for (var i = 0; i < data['Metrics'].length; i++) {
						angular.equals(data['Metrics'][i], {}) || angular.equals(data['Metrics'][i], null) ||
						angular.equals(data['Metrics'][i]['Series'], null)?
							vm.charts.push({data: [], name: "NO DATA"}) :
							vm.charts.push(getChartData(data['Metrics'][i]));
					}
				} else {
					for (var i = 0; i < 4; i++) {
						vm.charts.push({data: [], name: "NO DATA"});
					}
				}
				LOADING_SCREEN('none');
			}).error(function (error) {
				console.log(error);
				LOADING_SCREEN('none');
			});
		}
	}

	vm.onClick = function (points, evt) {
		console.log(points, evt);
	};

	function getChartData(obj) {
		var series = obj.Series;
		var seriesName = obj.Series[0].name;

		/** Chart options **/
		var chartOptions = {
			chart: {
				type: 'lineChart',
				height: 350,
				margin: {
					top: 20,
					right: 20,
					bottom: 70,
					left: 60
				},
				x: function (d) {
					return d.x;
				},
				y: function (d) {
					return d.y;
				},
				legend: {
					key: function (d) {
						return;
					},
					dispatch: {
						legendMouseover: function (t, u) {
							chartSeries.legend = t.key;
							$scope.$apply();
							return;
						},
						legendMouseout: function (t, u) {
							chartSeries.legend = "";
							$scope.$apply();
							return;
						}
					},
					padding: 20
				},
				useInteractiveGuideline: true,
				yAxis: {
					showMaxMin: false,
					tickFormat: function (d) {
						if (seriesName.indexOf('cpu') > -1 && d > 100) {
							return "";
						}
						return d;
					}
				},
				forceY: null,
				xAxis: {
					showMaxMin: false,
					tickValues: [],
					tickFormat: function (d) {
						return d3.time.format("%H:%M")(new Date(d));
					}
				},
				interpolate: 'monotone',
				callback: function (chart) {
				}
			}
		};

		if(seriesName == 'host_disk') {
			chartOptions.chart.interactiveLayer = getCustomTooltip('used', 'total');
		}

		if(seriesName == 'host_net') {
			chartOptions.chart.interactiveLayer = getCustomTooltip('in', 'out');
		}

		
		function getCustomTooltip(firstValue, secondValue) {
			return {"tooltip": {"contentGenerator": function(d) {

				var values = {};
				for (var i = 0; i < d.series.length; i++) {
					var currentSerieKey = d.series[i].key.split(' ');
					if(values[currentSerieKey[0]] == undefined) {
						values[currentSerieKey[0]] = {};
					}
					values[currentSerieKey[0]][currentSerieKey[1]] = {"value": d.series[i].value, "color": d.series[i].color};
				}

				var tooltipTable = [
					'<table>',
						'<thead>',
							'<tr>',
								'<td>',
									'<strong class="x-value">' + d.value + '</strong>',
								'</td>',
								'<td class="value_left">',
									firstValue,
								'</td>',
								'<td class="value_left">',
									'/',
								'</td>',
								'<td class="value_left">',
									secondValue,
								'</td>',
							'</tr>',
						'</thead>',
						'<tbody>',
				];

				for(var key in values) {

					var firstValueHtml = '';
					var secondValueHtml = '';
					if(values[key][firstValue] !== undefined) {
						firstValueHtml = '<div style="background-color: ' + values[key][firstValue].color + '"></div> ' + values[key][firstValue].value;
					}
					if(values[key][secondValue] !== undefined) {
						secondValueHtml = '<div style="background-color: ' + values[key][secondValue].color + '"></div> ' + values[key][secondValue].value;
					}

					var row = [
					'<tr>',
						'<td class="key">' + key + '</td>',
						'<td class="value_left legend-color-guide">',
							firstValueHtml,
						'</td>',
						'<td class="value_left">/</td>',
						'<td class="value_left legend-color-guide">',
							secondValueHtml,
						'</td>',
					'</tr>'
					].join('');
					tooltipTable.push(row);
				}

				tooltipTable.push('</tbody>');
				tooltipTable.push('</table>');
				return tooltipTable.join('');
			}}};
		}

		var chartSeries = {
			name: seriesName,
			unit: null,
			data: [],
			options: chartOptions,
			legend: ""
		};
		var maxValue = 0, unitCoefficient = 1;
		var minutes, start, end, diff, leftLimit, duration;
		var stubValues = [];

		/** Exclude "available" field from HOST_DISK series **/
		if(series[0].name == 'host_disk') {
			var temp = [];
			for(var serie in series) {
				if(series[serie].tags.type != 'available') {
					temp.push(series[serie]);
				}
			}
			series = temp;
		}

		/** Calculate amount of incomplete data received form rest **/
		start = moment.unix(series[0].values[0][0]);
		end = moment.unix(getEndDate(series));
		duration = moment.duration(end.diff(start)).asMinutes();
		diff = vm.period * 60 - duration;
		leftLimit = moment.unix(series[0].values[0][0]).subtract(diff, 'minutes');

		/** Generate stub values if data is incomplete at the begining **/
		if (diff > 0) {
			var startPoint = moment.unix(series[0].values[0][0]);
			while (startPoint.subtract(1, "minutes") >= leftLimit) {
				stubValues.unshift({
					x: startPoint.valueOf(),
					y: 0
				});
			}
		}

		/** Generate stub values if data is incomplete at the end **/
		for(var item in series) {

			if(moment.unix((series[item].values[series[item].values.length - 1][0])).valueOf() < moment.unix((getEndDate(series))).valueOf()) {
				var from = moment.unix(series[item].values[series[item].values.length - 1][0]);
				var to = moment.unix(getEndDate(series));


				from.add(1, "minutes");

				while(from.valueOf() <= to.valueOf()) {
					series[item].values.push([from.valueOf() / 1000, 0]);
					from.add(1, "minutes");
				}
			}
		}

		/** Restructure rest data into required format **/
		/** Append stub values at the beginning of VALUES array **/
		/** Generate scaled values for X axis **/
		for (var item in series) {
			var values = series[item].values;
			var realValues = [];
			for (var value in values) {
				realValues.push({
					x: moment.unix((values[value][0])).valueOf(),
					y: values[value][1]
				});
			}
			series[item].values = getXAxisScaledValues(stubValues.concat(realValues));
		}

		/** Define maximum value for right unit detection **/
		for(var serie in series) {
			for(var item in series[serie].values) {
				if(Math.round(series[serie].values[item].y * 100) / 100 > maxValue ) {
					maxValue = Math.round(series[serie].values[item].y * 100) / 100;
				} else {
					continue;
				}
			}
		}

		/** Set chart's X axis labels **/
		chartOptions.chart.xAxis.tickValues = getXAxisLabels(series[0].values);

		/** Define data unit **/
		switch (seriesName) {
			case 'host_cpu':
			case 'lxc_cpu':
				chartOptions.chart.yAxis.axisLabel = "%";
				chartOptions.chart.forceY = 100;
				chartSeries.unit = "%";
				break;
			case 'host_net':
			case 'lxc_net':
				switch (true) {
					case maxValue / Math.pow(10, 9) > 1:
						chartOptions.chart.yAxis.axisLabel = "Gbps";
						chartSeries.unit = "Gbps";
						chartOptions.chart.forceY = maxValue / Math.pow(10, 9);
						unitCoefficient = Math.pow(10, 9);
						break;
					case maxValue / Math.pow(10, 6) > 1:
						chartOptions.chart.yAxis.axisLabel = "Mbps";
						chartSeries.unit = "Mbps";
						chartOptions.chart.forceY = maxValue / Math.pow(10, 6);
						unitCoefficient = Math.pow(10, 6);
						break;
					case maxValue / Math.pow(10, 3) > 1:
						chartOptions.chart.yAxis.axisLabel = "Kbps";
						chartSeries.unit = "Kbps";
						chartOptions.chart.forceY = maxValue / Math.pow(10, 3);
						unitCoefficient = Math.pow(10, 3);
						break;
					default:
						chartOptions.chart.yAxis.axisLabel = "bps";
						chartSeries.unit = "bps";
						chartOptions.chart.forceY = maxValue;
						unitCoefficient = 1;
						break;
				}
				break;
			case 'host_memory':
			case 'lxc_memory':
			case 'host_disk':
			case 'lxc_disk':
				switch (true) {
					case maxValue / Math.pow(10, 9) > 1:
						chartOptions.chart.yAxis.axisLabel = "GB";
						chartOptions.chart.forceY = maxValue / Math.pow(10, 9);
						chartSeries.unit = "GB";
						unitCoefficient = Math.pow(10, 9);
						break;
					case maxValue / Math.pow(10, 6) > 1:
						chartOptions.chart.yAxis.axisLabel = "MB";
						chartOptions.chart.forceY = maxValue / Math.pow(10, 6);
						chartSeries.unit = "MB";
						unitCoefficient = Math.pow(10, 6);
						break;
					case maxValue / Math.pow(10, 3):
						chartOptions.chart.yAxis.axisLabel = "KB";
						chartOptions.chart.forceY = (maxValue / Math.pow(10, 3));
						chartSeries.unit = "KB";
						unitCoefficient = Math.pow(10, 3);
						break;
					default:
						chartOptions.chart.yAxis.axisLabel = "Byte";
						chartOptions.chart.forceY = maxValue;
						chartSeries.unit = "Byte";
						unitCoefficient = 1;
						break;
				}
			default:
				break;
		}

		/** Iterate over series of rest data **/
		for (var item in series) {
			var chartSerie = {
				values: [],
				key: series[item].tags.type ? series[item].tags.type : "",
				area: true
			};
			var values = series[item].values;

			for (var value in values) {

				switch (seriesName) {
					case 'host_net':
						chartSerie.key = series[item].tags.iface + ' ' + series[item].tags.type;
						break;
					case 'host_disk':
						chartSerie.key = series[item].tags.mount + ' ' + series[item].tags.type;
						break;
					case 'lxc_disk':
						chartSerie.key = series[item].tags.mount;
						break;
					case 'lxc_net':
					case 'host_cpu':
					case 'lxc_cpu':
					case 'host_memory':
					case 'lxc_memory':
						break;
					default:
						console.error('Unregistered target type!');
						break;
				}
				chartSerie.values.push({
					x: moment((values[value].x)).valueOf(),
					y: values[value].y == undefined ? 0 : parseFloat((values[value].y / unitCoefficient).toFixed(2))
				});
			}
			chartSeries.data.push(chartSerie);
		}

		/** Select chart color range **/
		if (seriesName.indexOf('lxc') > -1) {
			chartOptions.chart.color = d3.scale.category10().range();
		} else {
			chartOptions.chart.color = d3.scale.category20().range();
		}
		return chartSeries;
	}

	/** Generate X axis labels related to defined labelStep coefficient **/
	function getXAxisLabels(data) {
		var labels = [];
		var labelStepCoefficient = vm.parseOptions[parseInt(vm.period)].labelStep;
		var valueStepCoefficient = vm.parseOptions[vm.period].labelStep;
		for (var index = 0; index < data.length; index++) {
			if (moment((data[index].x)).get('minute') == 0 ||
					moment((data[index].x)).get('minute') % labelStepCoefficient == 0 ||
					parseInt(vm.period) == 1 && moment((data[index].x)).get('minute') % valueStepCoefficient == 0
			   ) {
				labels.push(moment((data[index].x)).valueOf());
				var tempStore = moment((data[index].x)).valueOf();
				while (true) {
					tempStore += labelStepCoefficient * 60000;
					if (tempStore > moment((data[data.length - 1].x)).valueOf()) {
						return labels;
					}
					labels.push(tempStore);
				}
				return labels;
			}
		}
	}

	/** Generate X axis values related to defined valueStep coefficient **/
	function getXAxisScaledValues(data) {
		var chartDataMap = getChartDataMap(data);
		var scaledData = [];
		var valueStepCoefficient = vm.parseOptions[parseInt(vm.period)].valueStep;
		var maxValue = moment((data[data.length - 1].x)).valueOf();

		for (var index = 0; index < data.length; index++) {
			if (moment((data[index].x)).get('minute') % valueStepCoefficient == 0) {
				scaledData.push({
					x: moment((data[index].x)).valueOf(),
					y: data[index].y
				});
				var tempStore = moment((data[index].x));
				while (tempStore.add(valueStepCoefficient, 'minutes').valueOf() <= maxValue) {
					scaledData.push({
						x: tempStore.valueOf(),
						y: chartDataMap[tempStore.valueOf()]
					});
				}
				if (tempStore.subtract(valueStepCoefficient, 'minutes').valueOf() == maxValue) {
					return scaledData;
				} else {
					scaledData.push({
						x: maxValue,
						y: chartDataMap[maxValue]
					});
					return scaledData;
				}
			}
			if (index == 0) {
				scaledData.push({
					x: moment((data[index].x)).valueOf(),
					y: data[index].y
				});
			}
		}
	}

	/** Generate map from objects array **/
	function getChartDataMap(data) {
		var dataMap = {};
		for (var index in data) {
			dataMap[data[index].x] = data[index].y;
		}
		return dataMap;
	}

	/** Get max timestamp of series **/
	function getEndDate(series) {
		var maxValue = 0;
		for (var serie in series) {
			if(series[serie].values[series[serie].values.length - 1][0] > maxValue) {
				maxValue = series[serie].values[series[serie].values.length - 1][0];
			} else {
				continue;
			}
		}
		return maxValue;
	}
};

function timestampConverter(timestamp){
	var a = new Date(timestamp * 1000);
	var year = a.getFullYear();
	var month = a.getMonth() + 1;
	var date = a.getDate();
	var hour = a.getHours();
	var min = a.getMinutes();
	var sec = a.getSeconds();

	if( month < 10 )
	{
		month = "0" + month;
	}

	var time = year + '-' + month + '-' + date + "T" + hour + ":" + min + ":" + sec + ".000Z";
	return time;
}
