'use strict';

angular.module('subutai.col-select', [])
.controller('ColSelectCtrl', ColSelectCtrl)
.directive('colSelect', colSelect);


function ColSelectCtrl() {
	var vm = this;

	vm.colSelect = colSelect;

	vm.roles = [
		{
			id: 1,
			role: 'elem1',
			selected: false
		},
		{
			id: 2,
			role: 'elem2',
			selected: false
		},
		{
			id: 3,
			role: 'elem3',
			selected: true
		},
		{
			id: 4,
			role: 'elem4',
			selected: true
		}
	];

	//// Implementation

	function colSelect(id) {
		for( var i = 0; i < vm.roles.length; i++ ) {
			if( vm.roles[i].id == id ) {
				vm.roles[i].selected = !vm.roles[i].selected;
			}
		}
	}
}

function colSelect() {
	return {
		restrict: 'E',
		templateUrl: 'subutai-app/common/directives/col-select/col-select.html'
	}
}
