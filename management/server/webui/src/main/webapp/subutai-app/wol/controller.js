'use strict';

angular.module('subutai.wol', ['subutai.wol.service'])
	.controller('wolViewCtrl', wolViewCtrl)
	.controller('wolModalCtrl', wolModalCtrl);

wolViewCtrl.$inject = ['wolService', '$scope', '$modal', '$log'];
wolModalCtrl.$inject = ['$scope', '$modalInstance', 'items'];

function wolViewCtrl(wolService, $scope, $modal, $log) {

	$scope.items = ['item1', 'item2', 'item3'];

	$scope.animationsEnabled = true;

	$scope.openWolModal = function (size) {

		var modalInstance = $modal.open({
			animation: $scope.animationsEnabled,
			templateUrl: 'subutai-app/wol/partials/view.html',
			controller: 'wolModalCtrl',
			size: size,
			resolve: {
				items: function () {
					return $scope.items;
				}
			}
		});

		modalInstance.result.then(function (selectedItem) {
			$scope.selected = selectedItem;
		}, function () {
			$log.info('Modal dismissed at: ' + new Date());
		});
	};

	$scope.toggleAnimation = function () {
		$scope.animationsEnabled = !$scope.animationsEnabled;
	};
}

function wolModalCtrl($scope, $modalInstance, items) {

	$scope.items = items;
	$scope.selected = {
		item: $scope.items[0]
	};

	$scope.ok = function () {
		$modalInstance.close($scope.selected.item);
	};

	$scope.cancel = function () {
		$modalInstance.dismiss('cancel');
	};
};
