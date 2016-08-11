'use strict';

angular.module('subutai.nav-menu', [])
        .directive('navMenu', NavMenu);

NavMenu.$inject = ['$compile'];

function NavMenu($compile) {
    return {
        restrict: 'E',
        scope: false,
        link: function (scope, element, attr) {

            scope.$watch(function () { return localStorage.currentUserPermissions; },function(newVal,oldVal){
                // var templateUrl = getTemplateUrl(scope.data[0].typeID);
                // var data = $templateCache.get(templateUrl);
                // element.html(data);
                // $compile(element.contents())(scope);

                if( newVal !== oldVal && newVal !== undefined )
                    scope.$apply();
            });
        },
        templateUrl: 'subutai-app/common/partials/nav-menu.html'
    }
}
