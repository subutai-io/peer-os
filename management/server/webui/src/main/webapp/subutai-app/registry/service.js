/**
 * Created by talas on 5/15/15.
 */
'use strict';

angular.module('subutai.registry.service', [])
    .factory('registryService', registryService);


registryService.$inject = ['$http'];

function registryService($http) {
    var getTemplatesURL = 'subutai-app/registry/dummy-api/templates.json';

    var getFileContentForTemplatesURL = ['subutai-app/registry/dummy-api/fileContent.json', 'subutai-app/registry/dummy-api/fileContent1.json'];
    var inx = 1;

    var getTemplatesDiffFilesURL = 'subutai-app/registry/dummy-api/templatesDiffFiles.json';

    var registryService = {
        getTemplates: getTemplates,
        getFileContents: getFileContents,
        getTemplatesDiffFiles: getTemplatesDiffFiles
    };

    return registryService;

    function getTemplates() {
        return $http.get(getTemplatesURL);
    }

    function getFileContents(templateA, templateB, filePath) {
        return $http.get(getFileContentForTemplatesURL[inx++ % getFileContentForTemplatesURL.length]);
    }

    function getTemplatesDiffFiles(templateA, templateB) {
        return $http.get(getTemplatesDiffFilesURL);
    }
}