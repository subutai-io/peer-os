'use strict';

angular.module('subutai.template.service', [])
    .factory('templateSrv', templateSrv);


templateSrv.$inject = ['$http', '$q'];

function templateSrv($http, $q) {
   var BASE_URL = SERVER_URL + 'rest/v1/templates';

   var templateSrv = {
        getTemplates: getTemplates,
        getOwnTemplates: getOwnTemplates,
        getFingerprint: getFingerprint,
        getObtainedCdnToken: getObtainedCdnToken,
        obtainCdnToken: obtainCdnToken,
        getTokenRequest: getTokenRequest
   };

   return templateSrv;

   function getTemplates(){

        var callF = $q.defer();

        $http.get(BASE_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}})
            .success(function(data) {
                callF.resolve(data);
            });

        return callF.promise;
   }

   function getOwnTemplates(){

        var callF = $q.defer();

        $http.get(BASE_URL + "/own", {withCredentials: true, headers: {'Content-Type': 'application/json'}})
            .success(function(data) {
                callF.resolve(data);
            });

        return callF.promise;
   }

   function getFingerprint(){
       return $http.get(BASE_URL + "/fingerprint", {withCredentials: true});
   }

   function getObtainedCdnToken(){
       return $http.get(BASE_URL + "/token", {withCredentials: true});
   }

   function getTokenRequest(){
       return $http.get(BASE_URL + "/request", {withCredentials: true});
   }

   function obtainCdnToken(signedRequest){
       var postData = "signedRequest=" + encodeURIComponent(signedRequest)

       return $http.post(
           BASE_URL + "/token",
           postData,
           {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
       );
   }
}