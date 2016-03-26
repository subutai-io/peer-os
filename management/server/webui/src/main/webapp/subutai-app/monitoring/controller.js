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
		vm.currentHost = vm.hosts.length > 0 ? vm.hosts[0].id : '';
		getServerData();
	});

	function setCurrentType(type) {
		vm.containers = [];
		vm.selectedEnvironment = '';
		vm.currentHost = '';
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
		// if (vm.period > 0 && vm.currentHost) {
		// 	LOADING_SCREEN();
		// 	monitoringSrv.getInfo(vm.selectedEnvironment, vm.currentHost, vm.period).success(function (data) {
        //
		// 		var data = {"Metrics":[{"Series":[{"name":"host_cpu","tags":{"type":"idle"},"columns":["time","value"],"values":[[1458970470,15.506410256409435],[1458970500,62.462915479580985],[1458970530,88.23120476798698],[1458970560,91.36126436781682],[1458970590,95.96833333333295],[1458970620,94.52212643678067],[1458970650,73.76537356322127]]},{"name":"host_cpu","tags":{"type":"iowait"},"columns":["time","value"],"values":[[1458970470,1.1641025641025606],[1458970500,4.060968660968665],[1458970530,1.0323116219667934],[1458970560,0.019540229885058123],[1458970590,0.0023809523809518395],[1458970620,0.030952380952381495],[1458970650,0.027083333333333334]]},{"name":"host_cpu","tags":{"type":"nice"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_cpu","tags":{"type":"system"},"columns":["time","value"],"values":[[1458970470,4.360256410256409],[1458970500,2.6349952516619206],[1458970530,1.0041294167730976],[1458970560,1.2357471264367745],[1458970590,0.9223809523809489],[1458970620,1.0303776683087031],[1458970650,0.7880747126436821]]},{"name":"host_cpu","tags":{"type":"user"},"columns":["time","value"],"values":[[1458970470,42.99871794871795],[1458970500,27.660446343779693],[1458970530,4.1785014899957185],[1458970560,6.1142145593869826],[1458970590,1.4384126984126548],[1458970620,1.808292282430284],[1458970650,1.9738505747126813]]}]},{"Series":[{"name":"host_net","tags":{"iface":"eth0","type":"in"},"columns":["time","value"],"values":[[1458970470,1.8457723205128193e+06],[1458970500,150542.27188645204],[1458970530,183513.15004104775],[1458970560,95258.28275862536],[1458970590,30379.455295554795],[1458970620,32625.89872742494],[1458970650,29105.424784489474]]},{"name":"host_net","tags":{"iface":"eth0","type":"out"},"columns":["time","value"],"values":[[1458970470,5094.465384615461],[1458970500,5524.90595238097],[1458970530,60512.80956486035],[1458970560,124231.32873563214],[1458970590,826.356937602597],[1458970620,862.6970853859559],[1458970650,1679.9329741379247]]},{"name":"host_net","tags":{"iface":"lo","type":"in"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"lo","type":"out"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"lxc-br","type":"in"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"lxc-br","type":"out"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"mng-gw","type":"in"},"columns":["time","value"],"values":[[1458970470,5706.84230769231],[1458970500,6766.190109890108],[1458970530,36622.9685550082],[1458970560,69825.35287356323],[1458970590,1518.3940476190298],[1458970620,1553.2461822660018],[1458970650,2168.0764367816196]]},{"name":"host_net","tags":{"iface":"mng-gw","type":"out"},"columns":["time","value"],"values":[[1458970470,1.7736264423076923e+06],[1458970500,133550.29157509157],[1458970530,167812.0048440079],[1458970560,101203.66091953914],[1458970590,35492.48780787984],[1458970620,37965.15357143184],[1458970650,33519.54791666567]]},{"name":"host_net","tags":{"iface":"mng-net","type":"in"},"columns":["time","value"],"values":[[1458970470,0.3230769230769359],[1458970500,1.076923076923064],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"mng-net","type":"out"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"nat","type":"in"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"nat","type":"out"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"ovs-system","type":"in"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"ovs-system","type":"out"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"wan","type":"in"},"columns":["time","value"],"values":[[1458970470,1.7694116230769218e+06],[1458970500,127400.94331501723],[1458970530,161820.22688834468],[1458970560,94952.01724137664],[1458970590,29643.38563218514],[1458970620,32128.174137930077],[1458970650,28777.661278736592]]},{"name":"host_net","tags":{"iface":"wan","type":"out"},"columns":["time","value"],"values":[[1458970470,5021.083333333271],[1458970500,5522.56144688651],[1458970530,35841.68271756979],[1458970560,68931.84367816088],[1458970590,825.1783661741142],[1458970620,862.1377257798488],[1458970650,1511.4500718392433]]}]},{"Series":[{"name":"host_memory","tags":{"type":"Active"},"columns":["time","value"],"values":[[1458970440,1.8818186633846154e+09],[1458970470,2.091268726153846e+09],[1458970500,2.5168833801481485e+09],[1458970530,2.5213343249655175e+09],[1458970560,2.5274478592e+09],[1458970590,2.5290169782857137e+09],[1458970620,2.5315315288275867e+09],[1458970650,2.53320064e+09]]},{"name":"host_memory","tags":{"type":"Buffers"},"columns":["time","value"],"values":[[1458970440,1.053995323076923e+07],[1458970470,1.0558227692307692e+07],[1458970500,1.058163674074074e+07],[1458970530,1.059705820689655e+07],[1458970560,1.0613282133333333e+07],[1458970590,1.062151314285714e+07],[1458970620,1.062954372413793e+07],[1458970650,1.0635264e+07]]},{"name":"host_memory","tags":{"type":"Cached"},"columns":["time","value"],"values":[[1458970440,1.6767896024615386e+09],[1458970470,1.795243401846154e+09],[1458970500,1.8513998885925925e+09],[1458970530,1.8574468766896553e+09],[1458970560,1.8616634026666665e+09],[1458970590,1.8556339931428573e+09],[1458970620,1.8581638179310346e+09],[1458970650,1.8593205760000002e+09]]},{"name":"host_memory","tags":{"type":"MemFree"},"columns":["time","value"],"values":[[1458970440,3.5865203003076925e+09],[1458970470,3.3915484947692304e+09],[1458970500,2.96229561837037e+09],[1458970530,2.9531645881379313e+09],[1458970560,2.943876983466667e+09],[1458970590,2.948530468571429e+09],[1458970620,2.9441516932413797e+09],[1458970650,2.94120064e+09]]}]},{"Series":[{"name":"host_disk","tags":{"mount":"/boot/efi","type":"available"},"columns":["time","value"],"values":[[1458970440,1.2340736e+07],[1458970470,1.2340736e+07],[1458970500,1.2340736e+07],[1458970530,1.2340736e+07],[1458970560,1.2340736e+07],[1458970590,1.2340736e+07],[1458970620,1.2340736e+07],[1458970650,1.2340736e+07]]},{"name":"host_disk","tags":{"mount":"/boot/efi","type":"total"},"columns":["time","value"],"values":[[1458970440,6.6059264e+07],[1458970470,6.6059264e+07],[1458970500,6.6059264e+07],[1458970530,6.6059264e+07],[1458970560,6.6059264e+07],[1458970590,6.6059264e+07],[1458970620,6.6059264e+07],[1458970650,6.6059264e+07]]},{"name":"host_disk","tags":{"mount":"/boot/efi","type":"used"},"columns":["time","value"],"values":[[1458970440,5.3718528e+07],[1458970470,5.3718528e+07],[1458970500,5.3718528e+07],[1458970530,5.3718528e+07],[1458970560,5.3718528e+07],[1458970590,5.3718528e+07],[1458970620,5.3718528e+07],[1458970650,5.3718528e+07]]},{"name":"host_disk","tags":{"mount":"/mnt","type":"available"},"columns":["time","value"],"values":[[1458970440,1.0538945173661539e+11],[1458970470,1.0515364627692308e+11],[1458970500,1.0491778935466666e+11],[1458970530,1.0480622344827586e+11],[1458970560,1.048017424384e+11],[1458970590,1.047983893942857e+11],[1458970620,1.04806125568e+11],[1458970650,1.0480603904e+11]]},{"name":"host_disk","tags":{"mount":"/mnt","type":"total"},"columns":["time","value"],"values":[[1458970440,1.073741824e+11],[1458970470,1.073741824e+11],[1458970500,1.073741824e+11],[1458970530,1.073741824e+11],[1458970560,1.073741824e+11],[1458970590,1.073741824e+11],[1458970620,1.073741824e+11],[1458970650,1.073741824e+11]]},{"name":"host_disk","tags":{"mount":"/mnt","type":"used"},"columns":["time","value"],"values":[[1458970440,9.564922486153846e+08],[1458970470,1.1922164184615383e+09],[1458970500,1.4297898097777774e+09],[1458970530,1.542123943724138e+09],[1458970560,1.546606592e+09],[1458970590,1.5499743817142859e+09],[1458970620,1.542238208e+09],[1458970650,1.542324736e+09]]},{"name":"host_disk","tags":{"mount":"/writable/cache/system","type":"available"},"columns":["time","value"],"values":[[1458970440,9.51525376e+08],[1458970470,9.51525376e+08],[1458970500,9.51525376e+08],[1458970530,9.51525376e+08],[1458970560,9.51525376e+08],[1458970590,9.51525376e+08],[1458970620,9.51525376e+08],[1458970650,9.51525376e+08]]},{"name":"host_disk","tags":{"mount":"/writable/cache/system","type":"total"},"columns":["time","value"],"values":[[1458970440,1.02330368e+09],[1458970470,1.02330368e+09],[1458970500,1.02330368e+09],[1458970530,1.02330368e+09],[1458970560,1.02330368e+09],[1458970590,1.02330368e+09],[1458970620,1.02330368e+09],[1458970650,1.02330368e+09]]},{"name":"host_disk","tags":{"mount":"/writable/cache/system","type":"used"},"columns":["time","value"],"values":[[1458970440,1.314816e+06],[1458970470,1.314816e+06],[1458970500,1.314816e+06],[1458970530,1.314816e+06],[1458970560,1.314816e+06],[1458970590,1.314816e+06],[1458970620,1.314816e+06],[1458970650,1.314816e+06]]},{"name":"host_disk","tags":{"mount":"/writable","type":"available"},"columns":["time","value"],"values":[[1458970440,1.4348039089230769e+09],[1458970470,1.4347854769230773e+09],[1458970500,1.434771456e+09],[1458970530,1.434770043586207e+09],[1458970560,1.434763264e+09],[1458970590,1.434763264e+09],[1458970620,1.434763264e+09],[1458970650,1.434759168e+09]]},{"name":"host_disk","tags":{"mount":"/writable","type":"total"},"columns":["time","value"],"values":[[1458970440,1.615716352e+09],[1458970470,1.615716352e+09],[1458970500,1.615716352e+09],[1458970530,1.615716352e+09],[1458970560,1.615716352e+09],[1458970590,1.615716352e+09],[1458970620,1.615716352e+09],[1458970650,1.615716352e+09]]},{"name":"host_disk","tags":{"mount":"/writable","type":"used"},"columns":["time","value"],"values":[[1458970440,8.035564307692307e+07],[1458970470,8.037407507692307e+07],[1458970500,8.0388096e+07],[1458970530,8.03895084137931e+07],[1458970560,8.0396288e+07],[1458970590,8.0396288e+07],[1458970620,8.0396288e+07],[1458970650,8.040038399999999e+07]]},{"name":"host_disk","tags":{"mount":"/","type":"available"},"columns":["time","value"],"values":[[1458970440,3.00056576e+08],[1458970470,3.00056576e+08],[1458970500,3.00056576e+08],[1458970530,3.00056576e+08],[1458970560,3.00056576e+08],[1458970590,3.00056576e+08],[1458970620,3.00056576e+08],[1458970650,3.00056576e+08]]},{"name":"host_disk","tags":{"mount":"/","type":"total"},"columns":["time","value"],"values":[[1458970440,1.02330368e+09],[1458970470,1.02330368e+09],[1458970500,1.02330368e+09],[1458970530,1.02330368e+09],[1458970560,1.02330368e+09],[1458970590,1.02330368e+09],[1458970620,1.02330368e+09],[1458970650,1.02330368e+09]]},{"name":"host_disk","tags":{"mount":"/","type":"used"},"columns":["time","value"],"values":[[1458970440,6.52783616e+08],[1458970470,6.52783616e+08],[1458970500,6.52783616e+08],[1458970530,6.52783616e+08],[1458970560,6.52783616e+08],[1458970590,6.52783616e+08],[1458970620,6.52783616e+08],[1458970650,6.52783616e+08]]}]}]};
        //
		// 		vm.charts = [];
		// 		if(data['Metrics']) {
		// 			for (var i = 0; i < data['Metrics'].length; i++) {
		// 				angular.equals(data['Metrics'][i], {}) || angular.equals(data['Metrics'][i], null) ||
		// 				angular.equals(data['Metrics'][i]['Series'], null)?
		// 					vm.charts.push({data: [], name: "NO DATA"}) :
		// 					vm.charts.push(getChartData(data['Metrics'][i]));
		// 			}
		// 		} else {
		// 			for (var i = 0; i < 4; i++) {
		// 				vm.charts.push({data: [], name: "NO DATA"});
		// 			}
		// 		}
		// 		LOADING_SCREEN('none');
		// 	}).error(function (error) {
		// 		console.log(error);
		// 		LOADING_SCREEN('none');
		// 	});
		// }
		var data = {"Metrics":[{"Series":[{"name":"host_cpu","tags":{"type":"idle"},"columns":["time","value"],"values":[[1458970470,15.506410256409435],[1458970500,62.462915479580985],[1458970530,88.23120476798698],[1458970560,91.36126436781682],[1458970590,95.96833333333295],[1458970620,94.52212643678067],[1458970650,73.76537356322127]]},{"name":"host_cpu","tags":{"type":"iowait"},"columns":["time","value"],"values":[[1458970470,1.1641025641025606],[1458970500,4.060968660968665],[1458970530,1.0323116219667934],[1458970560,0.019540229885058123],[1458970590,0.0023809523809518395],[1458970620,0.030952380952381495],[1458970650,0.027083333333333334]]},{"name":"host_cpu","tags":{"type":"nice"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_cpu","tags":{"type":"system"},"columns":["time","value"],"values":[[1458970470,4.360256410256409],[1458970500,2.6349952516619206],[1458970530,1.0041294167730976],[1458970560,1.2357471264367745],[1458970590,0.9223809523809489],[1458970620,1.0303776683087031],[1458970650,0.7880747126436821]]},{"name":"host_cpu","tags":{"type":"user"},"columns":["time","value"],"values":[[1458970470,42.99871794871795],[1458970500,27.660446343779693],[1458970530,4.1785014899957185],[1458970560,6.1142145593869826],[1458970590,1.4384126984126548],[1458970620,1.808292282430284],[1458970650,1.9738505747126813]]}]},{"Series":[{"name":"host_net","tags":{"iface":"eth0","type":"in"},"columns":["time","value"],"values":[[1458970470,1.8457723205128193e+06],[1458970500,150542.27188645204],[1458970530,183513.15004104775],[1458970560,95258.28275862536],[1458970590,30379.455295554795],[1458970620,32625.89872742494],[1458970650,29105.424784489474]]},{"name":"host_net","tags":{"iface":"eth0","type":"out"},"columns":["time","value"],"values":[[1458970470,5094.465384615461],[1458970500,5524.90595238097],[1458970530,60512.80956486035],[1458970560,124231.32873563214],[1458970590,826.356937602597],[1458970620,862.6970853859559],[1458970650,1679.9329741379247]]},{"name":"host_net","tags":{"iface":"lo","type":"in"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"lo","type":"out"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"lxc-br","type":"in"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"lxc-br","type":"out"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"mng-gw","type":"in"},"columns":["time","value"],"values":[[1458970470,5706.84230769231],[1458970500,6766.190109890108],[1458970530,36622.9685550082],[1458970560,69825.35287356323],[1458970590,1518.3940476190298],[1458970620,1553.2461822660018],[1458970650,2168.0764367816196]]},{"name":"host_net","tags":{"iface":"mng-gw","type":"out"},"columns":["time","value"],"values":[[1458970470,1.7736264423076923e+06],[1458970500,133550.29157509157],[1458970530,167812.0048440079],[1458970560,101203.66091953914],[1458970590,35492.48780787984],[1458970620,37965.15357143184],[1458970650,33519.54791666567]]},{"name":"host_net","tags":{"iface":"mng-net","type":"in"},"columns":["time","value"],"values":[[1458970470,0.3230769230769359],[1458970500,1.076923076923064],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"mng-net","type":"out"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"nat","type":"in"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"nat","type":"out"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"ovs-system","type":"in"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"ovs-system","type":"out"},"columns":["time","value"],"values":[[1458970470,0],[1458970500,0],[1458970530,0],[1458970560,0],[1458970590,0],[1458970620,0],[1458970650,0]]},{"name":"host_net","tags":{"iface":"wan","type":"in"},"columns":["time","value"],"values":[[1458970470,1.7694116230769218e+06],[1458970500,127400.94331501723],[1458970530,161820.22688834468],[1458970560,94952.01724137664],[1458970590,29643.38563218514],[1458970620,32128.174137930077],[1458970650,28777.661278736592]]},{"name":"host_net","tags":{"iface":"wan","type":"out"},"columns":["time","value"],"values":[[1458970470,5021.083333333271],[1458970500,5522.56144688651],[1458970530,35841.68271756979],[1458970560,68931.84367816088],[1458970590,825.1783661741142],[1458970620,862.1377257798488],[1458970650,1511.4500718392433]]}]},{"Series":[{"name":"host_memory","tags":{"type":"Active"},"columns":["time","value"],"values":[[1458970440,1.8818186633846154e+09],[1458970470,2.091268726153846e+09],[1458970500,2.5168833801481485e+09],[1458970530,2.5213343249655175e+09],[1458970560,2.5274478592e+09],[1458970590,2.5290169782857137e+09],[1458970620,2.5315315288275867e+09],[1458970650,2.53320064e+09]]},{"name":"host_memory","tags":{"type":"Buffers"},"columns":["time","value"],"values":[[1458970440,1.053995323076923e+07],[1458970470,1.0558227692307692e+07],[1458970500,1.058163674074074e+07],[1458970530,1.059705820689655e+07],[1458970560,1.0613282133333333e+07],[1458970590,1.062151314285714e+07],[1458970620,1.062954372413793e+07],[1458970650,1.0635264e+07]]},{"name":"host_memory","tags":{"type":"Cached"},"columns":["time","value"],"values":[[1458970440,1.6767896024615386e+09],[1458970470,1.795243401846154e+09],[1458970500,1.8513998885925925e+09],[1458970530,1.8574468766896553e+09],[1458970560,1.8616634026666665e+09],[1458970590,1.8556339931428573e+09],[1458970620,1.8581638179310346e+09],[1458970650,1.8593205760000002e+09]]},{"name":"host_memory","tags":{"type":"MemFree"},"columns":["time","value"],"values":[[1458970440,3.5865203003076925e+09],[1458970470,3.3915484947692304e+09],[1458970500,2.96229561837037e+09],[1458970530,2.9531645881379313e+09],[1458970560,2.943876983466667e+09],[1458970590,2.948530468571429e+09],[1458970620,2.9441516932413797e+09],[1458970650,2.94120064e+09]]}]},{"Series":[{"name":"host_disk","tags":{"mount":"/boot/efi","type":"available"},"columns":["time","value"],"values":[[1458970440,1.2340736e+07],[1458970470,1.2340736e+07],[1458970500,1.2340736e+07],[1458970530,1.2340736e+07],[1458970560,1.2340736e+07],[1458970590,1.2340736e+07],[1458970620,1.2340736e+07],[1458970650,1.2340736e+07]]},{"name":"host_disk","tags":{"mount":"/boot/efi","type":"total"},"columns":["time","value"],"values":[[1458970440,6.6059264e+07],[1458970470,6.6059264e+07],[1458970500,6.6059264e+07],[1458970530,6.6059264e+07],[1458970560,6.6059264e+07],[1458970590,6.6059264e+07],[1458970620,6.6059264e+07],[1458970650,6.6059264e+07]]},{"name":"host_disk","tags":{"mount":"/boot/efi","type":"used"},"columns":["time","value"],"values":[[1458970440,5.3718528e+07],[1458970470,5.3718528e+07],[1458970500,5.3718528e+07],[1458970530,5.3718528e+07],[1458970560,5.3718528e+07],[1458970590,5.3718528e+07],[1458970620,5.3718528e+07],[1458970650,5.3718528e+07]]},{"name":"host_disk","tags":{"mount":"/mnt","type":"available"},"columns":["time","value"],"values":[[1458970440,1.0538945173661539e+11],[1458970470,1.0515364627692308e+11],[1458970500,1.0491778935466666e+11],[1458970530,1.0480622344827586e+11],[1458970560,1.048017424384e+11],[1458970590,1.047983893942857e+11],[1458970620,1.04806125568e+11],[1458970650,1.0480603904e+11]]},{"name":"host_disk","tags":{"mount":"/mnt","type":"total"},"columns":["time","value"],"values":[[1458970440,1.073741824e+11],[1458970470,1.073741824e+11],[1458970500,1.073741824e+11],[1458970530,1.073741824e+11],[1458970560,1.073741824e+11],[1458970590,1.073741824e+11],[1458970620,1.073741824e+11],[1458970650,1.073741824e+11]]},{"name":"host_disk","tags":{"mount":"/mnt","type":"used"},"columns":["time","value"],"values":[[1458970440,9.564922486153846e+08],[1458970470,1.1922164184615383e+09],[1458970500,1.4297898097777774e+09],[1458970530,1.542123943724138e+09],[1458970560,1.546606592e+09],[1458970590,1.5499743817142859e+09],[1458970620,1.542238208e+09],[1458970650,1.542324736e+09]]},{"name":"host_disk","tags":{"mount":"/writable/cache/system","type":"available"},"columns":["time","value"],"values":[[1458970440,9.51525376e+08],[1458970470,9.51525376e+08],[1458970500,9.51525376e+08],[1458970530,9.51525376e+08],[1458970560,9.51525376e+08],[1458970590,9.51525376e+08],[1458970620,9.51525376e+08],[1458970650,9.51525376e+08]]},{"name":"host_disk","tags":{"mount":"/writable/cache/system","type":"total"},"columns":["time","value"],"values":[[1458970440,1.02330368e+09],[1458970470,1.02330368e+09],[1458970500,1.02330368e+09],[1458970530,1.02330368e+09],[1458970560,1.02330368e+09],[1458970590,1.02330368e+09],[1458970620,1.02330368e+09],[1458970650,1.02330368e+09]]},{"name":"host_disk","tags":{"mount":"/writable/cache/system","type":"used"},"columns":["time","value"],"values":[[1458970440,1.314816e+06],[1458970470,1.314816e+06],[1458970500,1.314816e+06],[1458970530,1.314816e+06],[1458970560,1.314816e+06],[1458970590,1.314816e+06],[1458970620,1.314816e+06],[1458970650,1.314816e+06]]},{"name":"host_disk","tags":{"mount":"/writable","type":"available"},"columns":["time","value"],"values":[[1458970440,1.4348039089230769e+09],[1458970470,1.4347854769230773e+09],[1458970500,1.434771456e+09],[1458970530,1.434770043586207e+09],[1458970560,1.434763264e+09],[1458970590,1.434763264e+09],[1458970620,1.434763264e+09],[1458970650,1.434759168e+09]]},{"name":"host_disk","tags":{"mount":"/writable","type":"total"},"columns":["time","value"],"values":[[1458970440,1.615716352e+09],[1458970470,1.615716352e+09],[1458970500,1.615716352e+09],[1458970530,1.615716352e+09],[1458970560,1.615716352e+09],[1458970590,1.615716352e+09],[1458970620,1.615716352e+09],[1458970650,1.615716352e+09]]},{"name":"host_disk","tags":{"mount":"/writable","type":"used"},"columns":["time","value"],"values":[[1458970440,8.035564307692307e+07],[1458970470,8.037407507692307e+07],[1458970500,8.0388096e+07],[1458970530,8.03895084137931e+07],[1458970560,8.0396288e+07],[1458970590,8.0396288e+07],[1458970620,8.0396288e+07],[1458970650,8.040038399999999e+07]]},{"name":"host_disk","tags":{"mount":"/","type":"available"},"columns":["time","value"],"values":[[1458970440,3.00056576e+08],[1458970470,3.00056576e+08],[1458970500,3.00056576e+08],[1458970530,3.00056576e+08],[1458970560,3.00056576e+08],[1458970590,3.00056576e+08],[1458970620,3.00056576e+08],[1458970650,3.00056576e+08]]},{"name":"host_disk","tags":{"mount":"/","type":"total"},"columns":["time","value"],"values":[[1458970440,1.02330368e+09],[1458970470,1.02330368e+09],[1458970500,1.02330368e+09],[1458970530,1.02330368e+09],[1458970560,1.02330368e+09],[1458970590,1.02330368e+09],[1458970620,1.02330368e+09],[1458970650,1.02330368e+09]]},{"name":"host_disk","tags":{"mount":"/","type":"used"},"columns":["time","value"],"values":[[1458970440,6.52783616e+08],[1458970470,6.52783616e+08],[1458970500,6.52783616e+08],[1458970530,6.52783616e+08],[1458970560,6.52783616e+08],[1458970590,6.52783616e+08],[1458970620,6.52783616e+08],[1458970650,6.52783616e+08]]}]}]};

		console.log(data['Metrics']);
		vm.char = [];
		if(data['Metrics']) {
			for (var i = 0; i < data['Metrics'].length; i++) {
				console.log(data['Metrics'][i]);
				angular.equals(data['Metrics'][i], {}) || angular.equals(data['Metrics'][i], null) ||
				angular.equals(data['Metrics'][i]['Series'], null)?
					vm.char.push({data: [], name: "NO DATA"}) :
					vm.char.push(getChartData(data['Metrics'][i]));
			}
		} else {
			for (var i = 0; i < 4; i++) {
				vm.char.push({data: [], name: "NO DATA"});
			}
		}
		console.log(vm.char);
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
		start = moment.unix((series[0].values[0][0]));
		end = moment.unix((getEndDate(series)));
		duration = moment.duration(end.diff(start)).asMinutes();
		diff = vm.period * 60 - duration;
		leftLimit = moment.unix((series[0].values[0][0])).subtract(diff, 'minutes');

		/** Generate stub values if data is incomplete at the begining **/
		if (diff > 0) {
			var startPoint = moment.unix((series[0].values[0][0]));
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
				var from = moment.unix((series[item].values[series[item].values.length - 1][0])).valueOf();
				var to = moment.unix((getEndDate(series)).valueOf());


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
					x: moment.unix((values[value].x)).valueOf(),
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
			if (moment.unix((data[index].x)).get('minute') == 0 ||
					moment.unix((data[index].x)).get('minute') % labelStepCoefficient == 0 ||
					parseInt(vm.period) == 1 && moment.unix((data[index].x)).get('minute') % valueStepCoefficient == 0
			   ) {
				labels.push(moment.unix((data[index].x)).valueOf());
				var tempStore = moment.unix((data[index].x)).valueOf();
				while (true) {
					tempStore += labelStepCoefficient * 60000;
					if (tempStore > moment.unix((data[data.length - 1].x)).valueOf()) {
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
		var maxValue = moment.unix((data[data.length - 1].x)).valueOf();

		for (var index = 0; index < data.length; index++) {
			if (moment.unix((data[index].x)).get('minute') % valueStepCoefficient == 0) {
				scaledData.push({
					x: moment.unix((data[index].x)).valueOf(),
					y: data[index].y
				});
				var tempStore = moment.unix((data[index].x));
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
					x: moment.unix((data[index].x)).valueOf(),
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