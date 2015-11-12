/**
 * Created by talas on 5/15/15.
 */
'use strict';

angular
    .module('subutai.registry.controller', [])
    .controller('RegistryCtrl', RegistryCtrl);

RegistryCtrl.$inject = ['registryService'];

function RegistryCtrl(registryService) {
    var self = this;

    self.oldText = null;
    self.newText = null;

    self.templates = [];
    self.diffFiles = [];
    self.filesTree = [{}];

    getTemplates();

    function getTemplates() {
        registryService.getTemplates().success(function (data) {
            self.templates = data;
        }).error(function () {
            self.status = "Couldn't get templates";
        });
    }

    self.getTemplateByName = function (templateName, templatesArray) {
        for (var inx in templatesArray) {
            if (templatesArray.hasOwnProperty(inx)) {
                var template = templatesArray[inx];
                if (templateName === template.pk.templateName) {
                    return template;
                }
                else {
                    return self.getTemplateByName(templateName, template.children);
                }
            }
        }
    };

    //Combobox tree events
    self.templateASelected = function (value) {
        self.templateA = value;
        if (self.templateB === undefined) {
            self.templateB = self.getTemplateByName(self.templateA.parentTemplateName, self.templates);
        }
        self.getTemplatesDiffFiles();
    };

    self.templateBSelected = function (value) {
        self.templateB = value;
        if (self.templateA === undefined) {
            self.templateA = self.getTemplateByName(self.templateB.parentTemplateName, self.templates);
        }
        self.getTemplatesDiffFiles();
    };

    self.logChanges = function () {
        if (self.templateA !== undefined) {
            console.log(self.templateA.pk);
        }
        if (self.templateB !== undefined) {
            console.log(self.templateB.pk);
        }
    };

    self.getTemplatesDiffFiles = function () {
        registryService.getTemplatesDiffFiles()
            .success(function (data) {
                self.diffFiles = data;
                self.buildFilesTree();
            })
            .error(function () {
                console.error("Couldn't complete request.");
                self.status = "Couldn't get templates diff files.";
            });
    };

    self.getFileContent = function (file) {
        console.log(JSON.stringify(file));
        registryService
            .getFileContents(self.templateA.pk.templateName, self.templateB.pk.templateName, file.pathToFile)
            .success(function (data) {
                var fileContents = data;
                self.oldText = fileContents["templateA"];
                self.newText = fileContents["templateB"];
            }).error(function () {
                self.status = "Couldn't get file contents";
                console.error("Error getting file contents.");
            });
    };

    self.buildFilesTree = function () {
        function nodeConstructor(itemName) {
            return {name: itemName, children: []};
        }

        while (self.filesTree.length > 0) {
            self.filesTree.pop();
        }
        self.diffFiles.forEach(function (file) {
            var path = file.pathToFile.split('/');
            var stepObj, step, inx, item, tmp;
            if (path.length >= 1) {
                step = path[0];
                // Iterate over root items for first node step existence
                for (inx = 0; inx < self.filesTree.length; inx++) {
                    item = self.filesTree[inx];
                    if (item.name === step) {
                        stepObj = item;
                        break;
                    }
                }
                if (stepObj === undefined) {
                    stepObj = nodeConstructor(step);
                    self.filesTree.push(stepObj);
                }
            }

            // Continue looking through node items till we get to the end of path
            for (var i = 1; i < path.length; i++) {
                step = path[i];
                tmp = undefined;
                for (var j = 0; j < stepObj.children.length; j++) {
                    item = stepObj.children[j];
                    if (item.name === step) {
                        tmp = item;
                        break;
                    }
                }
                if (tmp === undefined) {
                    tmp = nodeConstructor(step);
                    stepObj.children.push(tmp);
                }
                stepObj = tmp;
            }
        });
    };

    //Side tree items
    self.toggle = function (scope) {
        scope.toggle();
    };

    self.collapseAll = function () {
        self.$broadcast('collapseAll');
    };
    self.expandAll = function () {
        self.$broadcast('expandAll');
    };
}
