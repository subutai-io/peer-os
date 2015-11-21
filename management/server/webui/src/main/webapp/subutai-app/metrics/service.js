/**
 * Created by ubuntu on 5/15/15.
 */
'use strict';

angular.module('subutai.metrics.service', [])
    .factory('metricsSrv', metricsSrv);

metricsSrv.$inject = ['$http'];

function metricsSrv($http) {
    var getChartDataUrl = 'subutai-app/metrics/dummy-api/chartdata.json';
    var getEnvironmentsUrl = 'subutai-app/metrics/dummy-api/environments.json';
    var getChartOptionsUrl = 'subutai-app/metrics/dummy-api/chartOptions.json';
    var metricsSrv = {
        getChartData: getChartData,
        getChartOptions: getChartOptions,
        getEnvironments: getEnvironments
    };
    return metricsSrv;

    function getChartData() {
        return $http.get(getChartDataUrl);
    }
    function getChartOptions() {
        return $http.get(getChartOptionsUrl);
    }
    function getEnvironments() {
        return $http.get(getEnvironmentsUrl);
    }

}