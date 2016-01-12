'use strict';

angular.module('subutai.monitoring.controller', [])
    .controller('MonitoringCtrl', MonitoringCtrl);

MonitoringCtrl.$inject = ['$scope', '$timeout', 'monitoringSrv', 'cfpLoadingBar'];

function MonitoringCtrl($scope, $timeout, monitoringSrv, cfpLoadingBar) {

    var vm = this;

    cfpLoadingBar.start();
    angular.element(document).ready(function () {
        cfpLoadingBar.complete();
    });

    vm.currentType = 'peer';
    vm.charts = [{}, {}, {}, {}];
    vm.environments = [];
    vm.containers = [];
    vm.hosts = [];
    vm.selectedEnvironment = '';
    vm.currentHost = '';
    vm.period = 1;
    vm.parseOptions = {
        1: {labelStep: 10, valueStep: 1},
        6: {labelStep: 60, valueStep: 5},
        12: {labelStep: 120, valueStep: 10},
        24: {labelStep: 240, valueStep: 20},
        48: {labelStep: 480, valueStep: 40}
    };

    //functions
    vm.showContainers = showContainers;
    vm.setCurrentType = setCurrentType;
    vm.getServerData = getServerData;

    monitoringSrv.getEnvironments().success(function (data) {
        vm.environments = data;
    });

    monitoringSrv.getResourceHosts().success(function (data) {
        vm.hosts = data;
        for (var i = 0; i < vm.hosts.length; i++) {
            if (vm.hosts[i].hostname == 'management') {

                vm.hosts[i].id = '';

                var temp = angular.copy(vm.hosts[0]);
                vm.hosts[0] = angular.copy(vm.hosts[i]);
                vm.currentHost = vm.hosts[i].id;
                vm.hosts[i] = temp;

                getServerData();
                break;
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
        for (var i in vm.environments) {
            if (environmentId == vm.environments[i].id) {
                vm.containers = vm.environments[i].containers;
                break;
            }
        }
    }

    function getServerData() {
        if (vm.period > 0) {
            LOADING_SCREEN();
            monitoringSrv.getInfo(vm.selectedEnvironment, vm.currentHost, vm.period).success(function (data) {
                vm.charts = [];
                for (var i = 0; i < data.metrics.length; i++) {
                    angular.equals(data.metrics[i], {}) ?
                        vm.charts.push({data: [], name: "NO DATA"}) :
                        vm.charts.push(getChartData(data.metrics[i]));
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
        var series = obj.series;
        var seriesName = obj.series[0].name;

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

        /** Calculate amount of incomplete data received form rest **/
        start = moment(obj.series[0].values[0][0]);
        end = moment(getEndDate(obj));
        duration = moment.duration(end.diff(start)).asMinutes();
        diff = vm.period * 60 - duration;
        leftLimit = moment(obj.series[0].values[0][0]).subtract(diff, 'minutes');

        /** Generate stub values if data is incomplete at the begining **/
        if (diff > 0) {
            var startPoint = moment(obj.series[0].values[0][0]);
            while (startPoint.subtract(1, "minutes") >= leftLimit) {
                stubValues.unshift({
                    x: startPoint.valueOf(),
                    y: 0
                });
            }
        }

        /** Generate stub values if data is incomplete at the end **/
        for(var item in series) {
            if(moment(series[item].values[series[item].values.length - 1][0]).valueOf() < moment(getEndDate(obj)).valueOf()) {
                var from = moment(series[item].values[series[item].values.length - 1][0]);
                var to = moment(getEndDate(obj)).valueOf();

                from.add(1, "minutes");
                while(from.valueOf() <= to) {
                    series[item].values.push([from.valueOf(), 0]);
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
                    x: moment(values[value][0]).valueOf(),
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
                chartOptions.chart.forceY = 120;
                chartSeries.unit = "%";
                break;
            case 'host_net':
            case 'lxc_net':
                if (maxValue / Math.pow(10, 6) > 1) {
                    chartOptions.chart.yAxis.axisLabel = "Mbps";
                    chartOptions.chart.forceY = maxValue / Math.pow(10, 6) + (maxValue / Math.pow(10, 6)) / 3;
                    chartSeries.unit = "Mbps";
                    unitCoefficient = Math.pow(10, 6);
                    break;
                }
                if (maxValue / Math.pow(10, 3) > 1) {
                    chartOptions.chart.yAxis.axisLabel = "Kbps";
                    chartOptions.chart.forceY = (maxValue / Math.pow(10, 3)) + (maxValue / Math.pow(10, 3)) / 3;
                    chartSeries.unit = "Kbps";
                    unitCoefficient = Math.pow(10, 3);
                    break;
                }
                else {
                    chartOptions.chart.yAxis.axisLabel = "bps";
                    chartOptions.chart.forceY = maxValue + maxValue / 3;
                    chartSeries.unit = "bps";
                    unitCoefficient = 1;
                    break;
                }
                break;
                break;
            case 'host_memory':
            case 'lxc_memory':
            case 'host_disk':
            case 'lxc_disk':
                if (maxValue / Math.pow(10, 9) > 1) {
                    chartOptions.chart.yAxis.axisLabel = "GB";
                    chartOptions.chart.forceY = maxValue / Math.pow(10, 9) + (maxValue / Math.pow(10, 9)) / 3;
                    chartSeries.unit = "GB";
                    unitCoefficient = Math.pow(10, 9);
                    break;
                }
                if (maxValue / Math.pow(10, 6) > 1) {
                    chartOptions.chart.yAxis.axisLabel = "MB";
                    chartOptions.chart.forceY = maxValue / Math.pow(10, 6) + (maxValue / Math.pow(10, 6)) / 3;
                    chartSeries.unit = "MB";
                    unitCoefficient = Math.pow(10, 6);
                    break;
                }
                if (maxValue / Math.pow(10, 3) > 1) {
                    chartOptions.chart.yAxis.axisLabel = "KB";
                    chartOptions.chart.forceY = (maxValue / Math.pow(10, 3)) + (maxValue / Math.pow(10, 3)) / 3;
                    chartSeries.unit = "KB";
                    unitCoefficient = Math.pow(10, 3);
                    break;
                }
                else {
                    chartOptions.chart.yAxis.axisLabel = "Byte";
                    chartOptions.chart.forceY = maxValue + maxValue / 3;
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
                    x: moment(values[value].x).valueOf(),
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
            if (moment(data[index].x).get('minute') == 0 ||
                moment(data[index].x).get('minute') % labelStepCoefficient == 0 ||
                parseInt(vm.period) == 1 && moment(data[index].x).get('minute') % valueStepCoefficient == 0
            ) {
                labels.push(moment(data[index].x).valueOf());
                var tempStore = moment(data[index].x).valueOf();
                while (true) {
                    tempStore += labelStepCoefficient * 60000;
                    if (tempStore > moment(data[data.length - 1].x).valueOf()) {
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
        var maxValue = moment(data[data.length - 1].x).valueOf();

        for (var index = 0; index < data.length; index++) {
            if (moment(data[index].x).get('minute') % valueStepCoefficient == 0) {
                scaledData.push({
                    x: moment(data[index].x).valueOf(),
                    y: data[index].y
                });
                var tempStore = moment(data[index].x);
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
                    x: moment(data[index].x).valueOf(),
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
    function getEndDate(data) {
        var maxValue = 0;
        for (var serie in data.series) {
            if(moment(data.series[serie].values[data.series[serie].values.length - 1][0]).valueOf() > maxValue) {
                maxValue = moment(data.series[serie].values[data.series[serie].values.length - 1][0]).valueOf();
            } else {
                continue;
            }
        }
        return maxValue;
    }
};

