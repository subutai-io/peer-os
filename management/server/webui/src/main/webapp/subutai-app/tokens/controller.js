'use strict';

angular.module('subutai.tokens.controller', [])
	.controller('TokensCtrl', TokensCtrl);


TokensCtrl.$inject = ['identitySrv', '$scope', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile', 'ngDialog', 'SweetAlert'];


function TokensCtrl(identitySrv, $scope, DTOptionsBuilder, DTColumnBuilder, $resource, $compile, ngDialog, SweetAlert) {

	var vm = this;
	vm.tokens = {};
	vm.users = [];
	vm.newToken = {};
	vm.token2Edit = {};

	//functions
	vm.addToken = addToken;
	vm.editTokenForm = editTokenForm;
	vm.editToken = editToken;
	vm.deleteToken = deleteToken;
	vm.viewToken = viewToken;

	identitySrv.getUsers().success(function (data) {
		vm.users = data;
	});

	vm.dtInstance = {};
	vm.dtOptions = DTOptionsBuilder
		.fromFnPromise(function() {
			return $resource( identitySrv.getTokensUrl() ).query().$promise;
		})
		.withPaginationType('full_numbers')
		.withOption('createdRow', createdRow)
		.withOption('order', [[ 3, "desc" ]])
		.withOption('columnDefs', [ {"width": "105px", "targets": [7]} ])
		.withOption('stateSave', true);

	vm.dtColumns = [
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionEdit),
		DTColumnBuilder.newColumn('userName').withTitle('Username'),
		DTColumnBuilder.newColumn('token').withTitle('Token Name'),
		DTColumnBuilder.newColumn(null).withTitle('TTL/Date').renderWith(dateFormat),
		DTColumnBuilder.newColumn(null).withTitle('Type').renderWith(getTokenType),
		DTColumnBuilder.newColumn('hashAlgorithm').withTitle('Hash Algorithm'),
		DTColumnBuilder.newColumn('issuer').withTitle('Issued by'),
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(viewTokenButton),
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionDelete)
	];

	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

	function actionEdit(data, type, full, meta) {
		vm.tokens[data.token] = data;
		return '<a href class="b-icon b-icon_edit" ng-click="tokensCtrl.editTokenForm(\'' + data.token + '\')"></a>';
	}

	function dateFormat(data, type, full, meta) {
		var currentDate = new Date(data.validDate);

		var day = currentDate.getDate();
		var month = currentDate.getMonth() + 1;
		var year = currentDate.getFullYear();

		var houre = currentDate.getHours();
		var minutes = currentDate.getMinutes();
		var seconds = currentDate.getSeconds();

		return day + '/' + month + '/' + year + ' ' + houre + ':' + minutes + ':' + seconds;
	}

	function getTokenType(data, type, full, meta) {
		var type = '';
		if(data.type == 1) {
			type = 'Session';
		} else {
			type = 'Permanent';
		}
		return type;
	}

	function viewTokenButton(data, type, full, meta) {
		return '<a href class="b-btn b-btn_green" ng-click="tokensCtrl.viewToken(\'' + data.secret + '\')">Show token</a>';
	}

	function actionDelete(data, type, full, meta) {
		return '<a href="" class="b-icon b-icon_remove" ng-click="tokensCtrl.deleteToken(\'' + data.token + '\')"></a>';
	}

	function addToken() {
		if(vm.newToken.token === undefined || vm.newToken.token.length < 1) return;
		if(vm.newToken.period === undefined || vm.newToken.period.length < 1) return;
		if(vm.newToken.userId === undefined) return;
		try {
			identitySrv.addToken(vm.newToken).success(function (data) {
				vm.dtInstance.reloadData(null, false);
			});
		} catch(e) {
			SweetAlert.swal("ERROR!", "Token creation error. Error: " + e, "error");
		}
	}

	function editTokenForm(token) {
		vm.token2Edit = vm.tokens[token];
		vm.token2Edit.newToken = vm.token2Edit.token;
		ngDialog.open({
			template: 'subutai-app/tokens/partials/tokenForm.html',
			scope: $scope,
		});
	}

	function editToken() {
		identitySrv.editToken(vm.token2Edit).success(function (data) {
			ngDialog.closeAll();
			vm.token2Edit = {};
			vm.dtInstance.reloadData(null, false);
		});
	}

	function deleteToken(token) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: 'Delete "' + token + '" token!',
			type: "warning",
			showCancelButton: true,
			confirmButtonColor: "#ff3f3c",
			confirmButtonText: "Delete",
			cancelButtonText: "Cancel",
			closeOnConfirm: false,
			closeOnCancel: true,
			showLoaderOnConfirm: true
		},
		function (isConfirm) {
			if (isConfirm) {
				try {
					identitySrv.deleteToken(token).success(function (data) {
						//SweetAlert.swal("Success!", "Your token has been deleted.", "success");
						SweetAlert.swal({
							title : 'Deleted',
							text : 'User token has been deleted!',
							timer: VARS_TOOLTIP_TIMEOUT,
							showConfirmButton: false
						});
						vm.dtInstance.reloadData(null, false);
					}).error(function (data) {
						SweetAlert.swal("ERROR!", "User token is safe :). Error: " + data, "error");
					});
				} catch(e) {
					SweetAlert.swal("ERROR!", "User token is safe. Error: " + e, "error");
				}
			}
		});		
	}

	function viewToken(secret) {
		vm.showSecret = secret;
		ngDialog.open({
			template: 'subutai-app/tokens/partials/tokenPopup.html',
			scope: $scope,
		});
	}
}

