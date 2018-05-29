'use strict';

angular.module('subutai.template.service', [])
    .factory('templateSrv', templateSrv);


templateSrv.$inject = ['$http'];

function templateSrv($http) {
   var BASE_URL = SERVER_URL + 'rest/v1/templates/';

   var templateSrv = {
        getTemplates: getTemplates,
        getFingerprint: getFingerprint,
        getObtainedCdnToken: getObtainedCdnToken,
        obtainCdnToken: obtainCdnToken
   };

   return templateSrv;

   function getTemplates(){
       return $http.get(BASE_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
   }

   function getFingerprint(){
       return $http.get(BASE_URL + "fingerprint", {withCredentials: true});
   }

   function getObtainedCdnToken(){
       return $http.get(BASE_URL + "token", {withCredentials: true});
   }

   function obtainCdnToken(signedFingerprint){
       var postData = "signedFingerprint=" + encodeURIComponent(signedFingerprint)

       return $http.post(
           BASE_URL + "token",
           postData,
           {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
       );
   }
}