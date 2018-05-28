'use strict';

angular.module('subutai.template.service', [])
    .factory('templateSrv', templateSrv);


templateSrv.$inject = ['$http'];

function templateSrv($http) {
   var BASE_URL = SERVER_URL + 'rest/v1/templates/';

   var templateSrv = {
        getTemplates: getTemplates
   };

   return templateSrv;

   function getTemplates(){
       return $http.get(BASE_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
   }
}