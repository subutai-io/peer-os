'use strict';

angular.module('subutai.core.controller', [])
    .controller('CoreCtrl', CoreCtrl);

CoreCtrl.$inject = ['coreSrv', '$scope'];
function CoreCtrl(coreSrv, $scope) {
    var vm = this;
    vm.myField = {};
    vm.companies = {};
    vm.myField.value = "Write Something here";
    //vm.check = function() {
    //    console.log("check worked --->", vm.myField.value);
    //};
    vm.getSample = function(){
        vm.myField.value = "Hehe we don't have sample fur ya yet XD  ";
    };
    vm.rows = [];


    vm.removeRow = function(index){
        // remove the row specified in index
        console.log("We are in the function remove ----->", index)
        vm.rows.splice( index, 1);
        // if no rows left in the array create a blank array
        if (vm.rows.length === 0){
            vm.rows = [];
        }
    };
    vm.check = function() {
        vm.rows.push({

            pick: false,
            question: vm.row.question,

            fnPlaceholder: "Fist Name",

            lnPlaceholder: "Last Name",

            text: vm.myField.value

        });

    };
};
