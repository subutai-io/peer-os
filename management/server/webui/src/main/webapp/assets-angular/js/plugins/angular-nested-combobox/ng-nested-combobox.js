angular.module('ui.nested.combobox', [])
    .controller('NestedComboboxController', ['$scope', '$element', '$attrs', function ($scope, $element, $attrs) {
        'use strict';
        var that = this,
            oldMemberId = null;
        this.isOpen = false;
        this.currentMember = $scope.currentMember;

        $scope.$watch('controlDisabled', function (value) {
            that.controlDisabled = value;
        });

        $scope.$watch('currentMember', function (newValue) {
            that.selectValue(null, newValue);
        });

        this.toggleOpen = function () {

            if (that.controlDisabled === 'true') {
                this.isOpen.status = false;
                return false;
            }
            this.isOpen = !this.isOpen;
        };

        this.selectValue = function (event, member) {

            if (member === undefined) {
                return true;
            }

            if (oldMemberId === member.pk.md5sum) {
                return true;
            }

            if (member.pk.md5sum === 'root') {
                member.name = event.currentTarget.innerText;
            }
            //that.currentMember = member;
            $scope.changeEvent(member);
            that.currentMember = member;
            oldMemberId = member.pk.md5sum;

        };
    }])
    .directive('nestedComboBox', function () {
        'use strict';

        return {
            restrict: 'E',
            controller: 'NestedComboboxController',
            controllerAs: 'gs',
            replace: true,
            templateUrl: 'subutai-app/registry/partials/select-group.html',
            scope: {
                collection: '=',
                currentMember: '=',
                controlClass: '@',
                controlDisabled: '@',
                changeEvent: '='
            }
        };
    });