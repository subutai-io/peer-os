'use strict';

angular.module('subutai.login.controller', [])
	.controller('LoginCtrl', LoginCtrl)
	.controller('ChangePassCtrl', ChangePassCtrl);

LoginCtrl.$inject = ['$scope', 'loginSrv', '$http', '$rootScope'];
ChangePassCtrl.$inject = ['$scope', 'loginSrv', 'SweetAlert'];

function ChangePassCtrl( $scope, loginSrv, SweetAlert) {
	var vm = this;

	vm.changePass = changePass;

	function changePass(passObj) {
		if ($scope.changePassForm.$valid) {
			LOADING_SCREEN();
			loginSrv.changePass(passObj).success(function(data){
				LOADING_SCREEN('none');
				SweetAlert.swal ("Success!", "You have successfully changed password.", "success");
			}).error(function(error){
				LOADING_SCREEN('none');
				SweetAlert.swal ("ERROR!", "Error: " + error, "error");
			});
		}
	}
}


function LoginCtrl( $scope, loginSrv, $http, $rootScope )
{
	var vm = this;

	vm.name = "";
	vm.pass = "";
	vm.errorMessage = false;
	vm.activeMode = 'username';

	vm.passExpired = false;
	vm.newPass = "";
	vm.passConf = "";

	//functions
	vm.login = login;

	vm.requestSign = "";
	vm.resetPwd=false;
	vm.requestPwdReset=requestPwdReset;

    $http.get('https://192.168.0.104:8443/rest/v1/identity/signtoken', {
        withCredentials: true,
        headers: {'Content-Type': 'application/json'}
    }).success(function (data) {
        vm.requestSign = data;
    });

    function requestPwdReset(){
        vm.errorMessage="";
        vm.resetPwd = true;
    }


	function login() {

        vm.errorMessage="";

		if( vm.newPass.length > 0 || vm.resetPwd) {

			if( vm.newPass !== vm.passConf ) {
				vm.errorMessage = "New password doesn't match the 'Confirm password' field";
			} else {
                if(vm.resetPwd){
                    if(!$.trim(vm.name)){
                        vm.errorMessage="Empty username";
                    }
                    else if(!$.trim(vm.newPass)){
                        vm.errorMessage="Empty password";
                    }else if(!$.trim(vm.requestSign)){
                        vm.errorMessage="Empty request sign";
                    }else{

                        var postData = 'username=' + vm.name + '&password=' + vm.newPass + '&sign=' + encodeURIComponent(vm.requestSign);

                        loginSrv.resetPass(postData).success(function(data){
                             $rootScope.currentUser = vm.name;
                             $http.defaults.headers.common['sptoken'] = getCookie('sptoken');
                             //$state.go('home');
                             checkUserPermissions();
                         }).error(function(error){
                             vm.errorMessage = error;
                         });
                    }
                }else{

		            var postData = 'username=' + vm.name + '&password=' + vm.pass+'&newpassword=' + vm.newPass;

                    loginSrv.login( postData ).success(function(data){
                        $rootScope.currentUser = vm.name;
                        $http.defaults.headers.common['sptoken'] = getCookie('sptoken');
                        //$state.go('home');
                        checkUserPermissions();
                    }).error(function(error){
                        vm.errorMessage = error;
                    });
				}
			}
		} else {

			var postData = 'username=' + vm.name + '&password=' + vm.pass;

			loginSrv.login( postData ).success(function(data){
				$rootScope.currentUser = vm.name;
				$http.defaults.headers.common['sptoken'] = getCookie('sptoken');
				sessionStorage.removeItem('notifications');

				loginSrv.getHubIp().success(function(data){
					localStorage.setItem('getHubIp', data);
				}).error(function(error){
					console.log(error);
					localStorage.setItem('getHubIp', 'hub.subut.ai');
				});

				//$state.go('home');
				checkUserPermissions();
			}).error(function(error, status){
				vm.errorMessage = error;

				if( status == 412 ) {
					vm.passExpired = true;
				}
			});
		}
	}

	function checkUserPermissions() {
		if ((localStorage.getItem('currentUser') == undefined || localStorage.getItem('currentUser') == null
			|| localStorage.getItem('currentUserToken') != getCookie('sptoken')) && getCookie('sptoken')) {

			LOADING_SCREEN();
			$http.get(SERVER_URL + "rest/ui/identity/user", {
				withCredentials: true,
				headers: {'Content-Type': 'application/json'}
			}).success(function (data) {

				localStorage.removeItem('currentUser');
				localStorage.removeItem('currentUserPermissions');
				localStorage.removeItem('currentUserToken');

				localStorage.setItem('currentUser', data.userName);
				localStorage.setItem('currentUserToken', getCookie('sptoken'));

				var perms = [];
				for( var i = 0; i < data.roles.length; i++ ) {
					for( var j = 0; j < data.roles[i].permissions.length; j++ ) {
						perms.push(data.roles[i].permissions[j].object);
					}
				}

				localStorage.setItem('currentUserPermissions', perms);

				window.location = '/';
			}).error(function(error) {
				window.location = '/';
			});
		} else {
			window.location = '/';
		}
	}
}

