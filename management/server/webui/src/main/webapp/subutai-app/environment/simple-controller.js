'use strict';

angular.module('subutai.environment.simple-controller', [])
.controller('EnvironmentSimpleViewCtrl', EnvironmentSimpleViewCtrl);

EnvironmentSimpleViewCtrl.$inject = ['$scope', '$rootScope', 'environmentService', 'trackerSrv', 'SweetAlert', 'ngDialog', '$timeout', 'identitySrv', 'templateSrv'];

function EnvironmentSimpleViewCtrl($scope, $rootScope, environmentService, trackerSrv, SweetAlert, ngDialog, $timeout, identitySrv, templateSrv) {

	var vm = this;

//	checkCDNToken(templateSrv, $rootScope)

	var GRID_CELL_SIZE = 100;
	var containerSettingMenu = $('.js-dropen-menu');
	var currentTemplate = {};
	$scope.identity = angular.identity;

    vm.logMessages = [];

	vm.popupLogState = 'full';

	vm.currentEnvironment = {};

	vm.buildEnvironment = buildEnvironment;
	vm.editEnvironment = editEnvironment;
	vm.notifyChanges = notifyChanges;
	vm.applyChanges = applyChanges;
	vm.getLastOctet = getLastOctet;

	vm.environments = [];

	vm.colors = quotaColors;
	vm.templates = {};
	vm.templatesList = [];

	vm.activeCloudTab = 'templates';
	vm.templatesType = 'all';

	vm.templateGrid = [];
	vm.cubeGrowth = 1;
	vm.environment2BuildName = '';
	vm.buildCompleted = false;
	vm.selectedPlugin = false;

	// functions

	vm.initJointJs = initJointJs;
	vm.buildEnvironmentByJoint = buildEnvironmentByJoint;
	vm.clearWorkspace = clearWorkspace;
	vm.addSettingsToTemplate = addSettingsToTemplate;
	vm.getFilteredTemplates = getFilteredTemplates;

	vm.addContainer = addContainer;
	vm.closePopup = closePopup;

	//plugins actions
	vm.selectPlugin = selectPlugin;
	vm.setTemplatesByPlugin = setTemplatesByPlugin;
    vm.loadOwnTemplates = loadOwnTemplates;

    vm.getCdnToken = getCdnToken;

    function getCdnToken(){
        return localStorage.getItem('cdnToken');
    }


    function loadOwnTemplates(){
        templateSrv.getOwnTemplates()
            .then(function (data) {
                vm.templates['own'] = data;
                getFilteredTemplates();
            });
    }

    function loadTemplates(callback){
        templateSrv.getTemplates()
            .then(function (data) {
                vm.templates['all'] = data;
                getFilteredTemplates(callback);
            });
    }

    loadTemplates();
//    loadOwnTemplates();

    $rootScope.$on('cdnTokenSet', function(event, data){
        loadOwnTemplates();
    });

    function addUniqueTemplates(filteredTemplates, groupedTemplates){
        for( var i in groupedTemplates){
            var found = false;
            for( var j in filteredTemplates){
                if( groupedTemplates[i].id == filteredTemplates[j].id){
                    found = true;
                    break;
                }
            }
            if(!found){
                filteredTemplates.push(groupedTemplates[i]);
            }
        }
        return filteredTemplates;
    }

    function getFilteredTemplates(callback) {
        var templatesLst = [];

        for (var i in vm.templates) {
            if (i == vm.templatesType) {
                templatesLst = addUniqueTemplates(templatesLst, vm.templates[i]);
            }
        }

        vm.templatesList = templatesLst;

        if(callback) callback();
    }

	function resetPlugin() {
		if (vm.selectedPlugin.selected !== undefined) vm.selectedPlugin.selected = false;
		vm.selectedPlugin = false;
	}

	function closePopup() {
		resetPlugin();
		vm.buildCompleted = false;
		ngDialog.closeAll();
	}

	function getLogsFromTracker(environmentId) {
		trackerSrv.getOperations('ENVIRONMENT MANAGER', moment().format('YYYY-MM-DD'), moment().format('YYYY-MM-DD'), 100)
			.success(function (data) {
				for (var i = 0; i < data.length; i++) {
					if (data[i].description.includes(environmentId)) {
						getLogById(data[i].id, environmentId);
						break;
					}
				}
				return false;
			}).error(function (error) {
				console.log(error);
			});
	}

	var timezone = new Date().getTimezoneOffset();

    var timeoutId;

	function getLogById(id, envId) {

        clearTimeout(timeoutId);

		trackerSrv.getDownloadProgress(envId)
			.success(function (data) {

				if( data.length > 0 ) {

					data.sort();

					var output = '<table class="b-main-table b-main-table_progrss">';
					var checker = false;
					for( var i = 0; i < data.length; i++ ) {

						output += [
							'<tr>',
								'<th colspan="2">',
									'Peer ' + shortenIdName(data[i].peerId, 3),
								'</th>',
							'</tr>',
						].join('');
						for( var j = 0; j < data[i].templatesDownloadProgress.length; j++ ) {
							var p = data[i].templatesDownloadProgress[j];

							for (var tpl in p.templatesDownloadProgress) {
								output += [
									'<tr>',
										'<td>',
											'RH ' + shortenIdName(p.rhId, 3),
										'</td>',
										'<td>',
											'<div class="b-progress-cloud b-progress-cloud_white b-progress-cloud_big">',
												'<div class="b-progress-cloud-fill" style="width: ' + p.templatesDownloadProgress[tpl] + '%;"></div>',
												'<span class="b-progress-cloud-text">' + tpl + '</span>',
											'</div>',
										'</td>',
									'</tr>'
								].join('');
								if( p.templatesDownloadProgress[tpl] != 100 ) {
									checker = true;
								}
							}
						}
					}
					output += '</table>';

					if( checker == true ) {
						$('.js-download-progress').html(output);
					} else {
						$('.js-download-progress').html('');
					}
				} else {
					$('.js-download-progress').html('');
				}
			})
			.error(function (data) {
				$('.js-download-progress').html('');
			});

		trackerSrv.getOperation('ENVIRONMENT MANAGER', id)
			.success(function (data) {

                    if(data.state == 'RUNNING') {
                        timeoutId = setTimeout(function() {
                            getLogById(id, envId);
                        }, 2000);
                    }

                    var logs = atob(data.log).split('},');
                    var result = [];
                    for(var i = 0; i < logs.length; i++) {

                        var logCheck = logs[i].replace(/ /g,'');
                        if(logCheck.length > 0) {
                            var logObj = JSON.parse(logs[i] + '}');
                            var logTime = moment(logObj.date).format('HH:mm:ss');

                            var logStatus = 'success';
                            var logClasses = ['fa-check', 'g-text-green'];

                            if(i+2 == logs.length) {
                                if(data.state == 'RUNNING') {
                                    logTime = '';
                                    logStatus = 'in-progress';
                                    logClasses = ['fa-spinner', 'fa-pulse'];
                                }else if(data.state == 'FAILED') {
                                    logStatus = 'success';
                                    logClasses = ['fa-times', 'g-text-red'];
                                }else{
                                    logStatus = 'success';
                                    logClasses = ['fa-check', 'g-text-green'];
                                }
                            }

                            var  currentLog = {
                                "time": logTime,
                                "status": logStatus,
                                "classes": logClasses,
                                "log": logObj.log
                            };
                            result.push(currentLog);

                        }
                    }

                    vm.logMessages = result;

                    if(data.state != 'RUNNING') {
                        vm.buildCompleted = true;
                        vm.isEditing = false;

                        $('.js-download-progress').html('');
                        $rootScope.notificationsUpdate = 'getLogById';
                        $scope.$emit('reloadEnvironmentsList');
                        clearWorkspace();
                    }
			}).error(function (error) {
				console.log(error);
			});
	}


	function buildEnvironment() {
		vm.buildStep = 'showLogs';

		vm.buildCompleted = false;
		vm.logMessages = [];
		var currentLog = {
			"time": '',
			"status": 'in-progress',
			"classes": ['fa-spinner', 'fa-pulse'],
			"log": 'Registering environment'
		};
		vm.logMessages.push(currentLog);

		environmentService.startEnvironmentAutoBuild(vm.environment2BuildName, JSON.stringify(vm.containers2Build))
			.success(function (data) {
				vm.newEnvID = data;
				currentLog.status = 'success';
				currentLog.classes = ['fa-check', 'g-text-green'];
				currentLog.time = moment().format('HH:mm:ss');

				currentLog = {
					"time": '',
					"status": 'in-progress',
					"classes": ['fa-spinner', 'fa-pulse'],
					"log": 'Environment creation has been started'
				};
				vm.logMessages.push(currentLog);

				getLogById(data.trackerId, data.environmentId);
				initScrollbar();

				$rootScope.notificationsUpdate = 'buildEnvironment';
			}).error(function (error) {
			    ngDialog.closeAll();
				if (error && error.ERROR === undefined) {
					VARS_MODAL_ERROR(SweetAlert, 'Error: ' + error);
				} else {
					VARS_MODAL_ERROR(SweetAlert, 'Error: ' + error.ERROR);
				}
				currentLog.status = 'fail';
				currentLog.classes = ['fa-times', 'g-text-red'];
				currentLog.time = moment().format('HH:mm:ss');

				$rootScope.notificationsUpdate = 'buildEnvironmentError';
			});
		vm.environment2BuildName = '';
	}

	function notifyChanges() {
		vm.buildCompleted = false;

		vm.currentEnvironment.excludedContainersByQuota =
			getSortedContainersByQuota(vm.currentEnvironment.excludedContainers);
		vm.currentEnvironment.includedContainersByQuota =
			getSortedContainersByQuota(vm.currentEnvironment.includedContainers);

		vm.currentEnvironment.numChangedContainers = 0;
		for (var key in vm.currentEnvironment.changingContainers) {
			vm.currentEnvironment.numChangedContainers++;
		}

		ngDialog.open({
			template: 'subutai-app/environment/partials/popups/environment-modification-info.html',
			scope: $scope,
			className: 'b-build-environment-info',
			preCloseCallback: function (value) {
				vm.buildCompleted = false;
			}
		});
	}

	function getSortedContainersByQuota(containers) {
		var sortedContainers = containers.length > 0 ? {} : null;
		for (var index = 0; index < containers.length; index++) {
			var quotaSize = containers[index].attributes.quotaSize;
			var templateName = containers[index].attributes.templateName;
			if (!sortedContainers[templateName]) {
				sortedContainers[templateName] = {};
				sortedContainers[templateName].quotas = {};
				sortedContainers[templateName]
					.quotas[quotaSize] = 1;
			} else {
				if (!sortedContainers[templateName].quotas) {
					sortedContainers[templateName].quotas = {};
					sortedContainers[templateName]
						.quotas[quotaSize] = 1;
				} else {
					if (!sortedContainers[templateName].quotas[quotaSize]) {
						sortedContainers[templateName]
							.quotas[quotaSize] = 1;
					} else {
						sortedContainers[containers[index].attributes.templateName]
							.quotas[quotaSize] += 1;
					}
				}
			}
		}
		return sortedContainers;
	}

	function applyChanges() {
		vm.isApplyingChanges = true;
		ngDialog.closeAll();

		var excludedContainers = [];
		for (var i = 0; i < vm.currentEnvironment.excludedContainers.length; i++) {
			excludedContainers.push(vm.currentEnvironment.excludedContainers[i].get('containerId'));
		}
		var includedContainers = [];
		for (var i = 0; i < vm.currentEnvironment.includedContainers.length; i++) {
		    var currentElement = vm.currentEnvironment.includedContainers[i];
			var isCustom = currentElement.get('quotaSize') == 'CUSTOM';
			includedContainers.push({
				"quota": isCustom ?  {
					"containerSize":currentElement.get('quotaSize'),
                    "cpuQuota": currentElement.get("cpuQuota"),
                    "ramQuota": currentElement.get("ramQuota") + 'MiB',
                    "diskQuota": currentElement.get("diskQuota") + 'GiB',
				} : { "containerSize":currentElement.get('quotaSize') },
				"templateName": currentElement.get('templateName'),
				"name": currentElement.get('containerName'),
				"position": currentElement.get('position'),
				"templateId" : currentElement.get('templateId')
			});
		}

		var quotaContainers = [];

		for (var key in vm.currentEnvironment.changingContainers) {
			quotaContainers.push({ "key" : key, "value" : vm.currentEnvironment.changingContainers[key] });
		}

		vm.currentEnvironment.modificationData = {
			topology: includedContainers,
			removedContainers: excludedContainers,
			changingContainers: quotaContainers,
			environmentId: vm.currentEnvironment.id
		};

		ngDialog.open({
			template: 'subutai-app/environment/partials/popups/environment-modification-status.html',
			scope: $scope,
			className: 'b-build-environment-info',
			preCloseCallback: function (value) {
				//resetPlugin();
				vm.buildCompleted = false;
			}
		});

		vm.currentEnvironment.modifyStatus = 'modifying';

		vm.buildCompleted = false;
		vm.logMessages = [];
		var currentLog = {
			"time": '',
			"status": 'in-progress',
			"classes": ['fa-spinner', 'fa-pulse'],
			"log": 'Environment modification has been started'
		};
		vm.logMessages.push(currentLog);

		environmentService.modifyEnvironment(vm.currentEnvironment.modificationData).success(function (data) {
			vm.currentEnvironment.modifyStatus = 'modified';
			clearWorkspace();
			vm.isApplyingChanges = false;

			getLogById(data, vm.currentEnvironment.modificationData.environmentId);
			initScrollbar();
			$rootScope.notificationsUpdate = 'modifyEnvironment';
		}).error(function (error) {
		    ngDialog.closeAll();
            if (error && error.ERROR === undefined) {
                VARS_MODAL_ERROR(SweetAlert, 'Error: ' + error);
            } else {
                VARS_MODAL_ERROR(SweetAlert, 'Error: ' + error.ERROR);
            }
			vm.currentEnvironment.modifyStatus = 'error';
			clearWorkspace();
			vm.isApplyingChanges = false;
			$rootScope.notificationsUpdate = 'modifyEnvironmentError';
		});
	}

	function getLastOctet(ip) {
		return ip.split(".")[2];
	}

	var graph = new joint.dia.Graph;

	//custom shapes
	joint.shapes.tm = {};

	//simple creation templates
	joint.shapes.tm.toolElement = joint.shapes.basic.Generic.extend({

		toolMarkup: [
			'<g class="element-tools element-tools_big">',
				'<g class="element-tool-remove">',
					'<circle fill="#F8FBFD" r="8" stroke="#dcdcdc"/>',
					'<polygon transform="scale(1.2) translate(-5, -5)" fill="#292F6C" points="8.4,2.4 7.6,1.6 5,4.3 2.4,1.6 1.6,2.4 4.3,5 1.6,7.6 2.4,8.4 5,5.7 7.6,8.4 8.4,7.6 5.7,5 "/>',
					'<title>Remove</title>',
				'</g>',
			'</g>',
			'<g class="element-tools element-tools_copy">',
				'<g class="element-tool-copy">',
					'<circle fill="#F8FBFD" r="8" stroke="#dcdcdc"/>',
					'<g class="copy-icon element-tool-copy">',
						'<path class="element-tool-copy" d="M7.1,9.5H0.7c-0.1,0-0.2-0.1-0.2-0.2V2.8c0-0.1,0.1-0.2,0.2-0.2h6.5c0.1,0,0.2,0.1,0.2,0.2v6.5C7.3,9.4,7.2,9.5,7.1,9.5z M0.8,9.1H7V3H0.8V9.1z"/>',
						'<path class="element-tool-copy" d="M9.3,7.3H8.8c-0.1,0-0.2-0.1-0.2-0.2S8.7,7,8.8,7h0.4V0.9H3v0.3c0,0.1-0.1,0.2-0.2,0.2c-0.1,0-0.2-0.1-0.2-0.2V0.7 c0-0.1,0.1-0.2,0.2-0.2h6.5c0.1,0,0.2,0.1,0.2,0.2v6.5C9.5,7.3,9.4,7.3,9.3,7.3z"/>',
					'</g>',
					'<title>Copy</title>',
				'</g>',
			'</g>',
			'<g class="element-call-menu">',
				'<rect class="b-magnet"/>',
				'<g class="b-container-plus-icon">',
					'<line fill="none" stroke="#FFFFFF" stroke-miterlimit="10" x1="0" y1="4.5" x2="9" y2="4.5"/>',
					'<line fill="none" stroke="#FFFFFF" stroke-miterlimit="10" x1="4.5" y1="0" x2="4.5" y2="9"/>',
				'</g>',
			'</g>'
			].join(''),

		defaults: joint.util.deepSupplement({
			attrs: {
				text: {
					'font-weight': 400,
					'font-size': 'small',
					fill: 'black',
						'text-anchor': 'middle',
						'ref-x': .5,
							'ref-y': .5,
							'y-alignment': 'middle'
				},
				'rect.b-magnet': {
					fill: '#04346E',
					width: 15,
					height: 15,
					rx: 50,
					ry: 50,
					transform: 'translate(26,51)'
				},
					'g.b-container-plus-icon': {'ref-x': 29, 'ref-y': 54.5, ref: 'rect', transform: 'scale(1)'}
			},
		}, joint.shapes.basic.Generic.prototype.defaults)

	});

	joint.shapes.tm.devElement = joint.shapes.tm.toolElement.extend({

		markup: [
			'<g class="rotatable">',
				'<g class="scalable">',
					'<rect class="b-border"/>',
				'</g>',
				'<title/>',
				'<image/>',
			'</g>'
		].join(''),

		defaults: joint.util.deepSupplement({
			type: 'tm.devElement',
			size: {width: 70, height: 70},
			attrs: {
				title: {text: 'Static Tooltip'},
				'rect.b-border': {
					fill: '#fff',
					stroke: '#dcdcdc',
					'stroke-width': 1,
					width: 70,
					height: 70,
					rx: 50,
					ry: 50
				},
				//'rect.b-magnet': {fill: '#04346E', width: 10, height: 10, rx: 50, ry: 50, magnet: true, transform: 'translate(30,53)'},
				image: {'ref-x': 9, 'ref-y': 9, ref: 'rect', width: 50, height: 50},
			}
		}, joint.shapes.tm.toolElement.prototype.defaults)
	});

	//custom view
	joint.shapes.tm.ToolElementView = joint.dia.ElementView.extend({
		initialize: function () {
			joint.dia.ElementView.prototype.initialize.apply(this, arguments);
		},
		render: function () {
			joint.dia.ElementView.prototype.render.apply(this, arguments);
			this.renderTools();
			this.update();
			return this;
		},
		renderTools: function () {
			var toolMarkup = this.model.toolMarkup || this.model.get('toolMarkup');
			if (toolMarkup) {
				var nodes = V(toolMarkup);
				V(this.el).append(nodes);
			}
			return this;
		},
		mouseover: function (evt, x, y) {
		},
		pointerclick: function (evt, x, y) {
			this._dx = x;
			this._dy = y;
			this._action = '';
			var className = evt.target.parentNode.getAttribute('class');
			switch (className) {
				case 'element-tool-remove':
					if (this.model.attributes.containerId) {
						vm.currentEnvironment.excludedContainers.push(this.model);
					} else {
						var object =
							vm.currentEnvironment.includedContainers ?
							getElementByField('id', this.model.id, vm.currentEnvironment.includedContainers) :
							null;
						object !== null ? vm.currentEnvironment.includedContainers.splice(object.index, 1) : null;
					}
					this.model.remove();
					delete vm.templateGrid[Math.floor(x / GRID_CELL_SIZE)][Math.floor(y / GRID_CELL_SIZE)];

					filterPluginsList();

					return;
					break;
				case 'element-tool-copy':
					addContainer(
							this.model.attributes.templateName.toLowerCase(),
							false,
							this.model.attributes.quotaSize,
							getTemplateNameById(this.model.attributes.templateName, vm.templatesList),
							this.model.attributes.templateId
							);

					return;
					break;
				case 'element-call-menu':
				case 'b-container-plus-icon':
					currentTemplate = this.model;
					$('#js-container-name').val(currentTemplate.get('containerName')).trigger('change');
					if(currentTemplate.get('edited') == true) {
						$('#js-container-name').prop('disabled', true);
					} else {
						$('#js-container-name').prop('disabled', false);
					}
					$('#js-container-size').val(currentTemplate.get('quotaSize')).trigger('change');

					if(currentTemplate.get('quotaSize') == 'CUSTOM'){
					    $('#js-quotasize-custom-cpu').val(currentTemplate.get('cpuQuota')).trigger('change');
					    $('#js-quotasize-custom-ram').val(currentTemplate.get('ramQuota')).trigger('change');
					    $('#js-quotasize-custom-disk').val(currentTemplate.get('diskQuota')).trigger('change');
					}

					containerSettingMenu.find('.header').html('Settings for <b>' + this.model.get('templateName') + '</b> container');
					var elementPos = this.model.get('position');
					containerSettingMenu.css({
						'left': (elementPos.x + 12) + 'px',
						'top': (elementPos.y + 73) + 'px',
						'display': 'block'
					});
					return;
					break;
				case 'rotatable':
					return;
					break;
				default:
			}
			joint.dia.CellView.prototype.pointerclick.apply(this, arguments);
		}
	});
	joint.shapes.tm.devElementView = joint.shapes.tm.ToolElementView;

	vm.plugins = [];
	vm.filteredPlugins = {};
	function getPlugins() {
		environmentService.getInstalledPlugins().success(function (data) {
			vm.plugins = data;
			if (vm.plugins === undefined || vm.plugins === "") {
				vm.plugins = [];
			}
			$('.js-call-plugins-popup').on('click', function () {
				$('.js-environment-plugins-menu').stop().show(300);
			});
			filterPluginsList();
		});
	}

	getPlugins();

	function filterPluginsList() {
		var allElements = graph.getCells();

		if (allElements.length > 0) {
			vm.filteredPlugins = {};
			for (var i = 0; i < allElements.length; i++) {

				var currentTemplate = allElements[i].get('templateName');

				for (var j = 0; j < vm.plugins.length; j++) {

					var currentPlugin = vm.plugins[j];

					if (vm.filteredPlugins[currentPlugin.name] == undefined) {
						if (currentPlugin.requirement !== undefined) {
							var requirementArray = Object.keys(currentPlugin.requirement);
							if (requirementArray.indexOf(currentTemplate) > -1) {
								vm.filteredPlugins[currentPlugin.name] = currentPlugin;
							}
						}
					}

				}

			}
		} else {
			vm.filteredPlugins = vm.plugins;
		}
		$('.js-pluginspopup-scroll').perfectScrollbar('update');
		$scope.$$phase || $scope.$apply();
	}

	function selectPlugin(plugin) {

		if (vm.selectedPlugin) {
			vm.selectedPlugin.selected = false;
		}

		vm.selectedPlugin = plugin;
		vm.selectedPlugin.selected = true;
	}

	function setTemplatesByPlugin() {

		if (vm.selectedPlugin.requirement !== undefined) {
			for (var template in vm.selectedPlugin.requirement) {
				for (var i = 0; i < vm.selectedPlugin.requirement[template]; i++) {
					var allElements = graph.getCells();
					if (allElements.length > 0) {
						var alreadyONWorckspace = false;
						var templatesCounter = 0;
						for (var j = 0; j < allElements.length; j++) {
							var currentTemplate = allElements[j].get('templateName');
							if (currentTemplate == template.toLowerCase()) {
								alreadyONWorckspace = true;
								templatesCounter++;
								//break;
							}
						}
						if (!alreadyONWorckspace || templatesCounter == i) {
                            environmentService.getVerifiedTemplate(template).success(function(verifiedTemplate){
                                addContainer(template.toLowerCase(), null, vm.selectedPlugin.size, null, verifiedTemplate.id);
                            });
						}
                    } else {
                        environmentService.getVerifiedTemplate(template).success(function(verifiedTemplate){
                            addContainer(template.toLowerCase(), null, vm.selectedPlugin.size, null, verifiedTemplate.id);
                        });
                    }
				}
			}
		}
		$('.b-template-settings').stop().slideUp(100);

	}

	var containerCounter = 1;
	function addContainer(template, $event, size, templateImg, templateId) {

		if($event === undefined || $event === null) $event = false;

		if (size === undefined || size === null) {
			size = 'TINY';
			if (template == 'appscale') {
				size = 'HUGE';
			}
		}

		//workaround issue #974
		//to implement properly, plugins should expose template id requirement
		if(templateId == undefined || templateId == null){
			templateId = getTemplateIdByName(template, vm.templatesList);
		}

		if (templateImg === undefined || templateImg === null) templateImg = template;

		var pos = findEmptyCubePostion();
		var img = 'assets/templates/' + templateImg + '.jpg';

		if ($event) {
			img = $($event.currentTarget).find('img').attr('src');
		} else {
			if (!imageExists(img)) {
				img = 'assets/templates/no-image.jpg';
			}
		}

		var containerName = 'Container' + (containerCounter++).toString();
		var devElement = new joint.shapes.tm.devElement({
			position: { x: (GRID_CELL_SIZE * pos.x) + 20, y: (GRID_CELL_SIZE * pos.y) + 20 },
			edited: false,
			templateName: template,
			quotaSize: size,
			containerName: containerName,
			templateId: templateId,
			attrs: {
				image: { 'xlink:href': img },
				'rect.b-magnet': {fill: vm.colors[size]},
				title: {text: containerName + " ('" + template + "') " + size}
			}
		});

        var theDiv = $("#js-environment-creation");

        theDiv.width ( theDiv.width() > devElement.attributes.position.x + 100
        ? theDiv.width() :devElement.attributes.position.x + 100) ;

        theDiv.height ( theDiv.height() > devElement.attributes.position.y + 100
        ? theDiv.height() :devElement.attributes.position.y + 100) ;

		vm.isEditing ? vm.currentEnvironment.includedContainers.push(devElement) : null;
		graph.addCell(devElement);
		filterPluginsList();
		return false;
	}

	function findEmptyCubePostion() {
		for (var j = 0; j < vm.cubeGrowth; j++) {
			for (var i = 0; i < vm.cubeGrowth; i++) {
				if (vm.templateGrid[i] === undefined) {
					vm.templateGrid[i] = new Array();
					vm.templateGrid[i][j] = 1;

					return {x: i, y: j};
				}

				if (vm.templateGrid[i][j] !== 1) {
					vm.templateGrid[i][j] = 1;
					return {x: i, y: j};
				}
			}
		}

		vm.templateGrid[vm.cubeGrowth] = new Array();
		vm.templateGrid[vm.cubeGrowth][0] = 1;
		vm.cubeGrowth++;
		return {x: vm.cubeGrowth - 1, y: 0};
	}

	vm.findEmptyCubePostion = findEmptyCubePostion;

	function initJointJs() {

		setTimeout(function () {
			document.getElementById('js-environment-creation').addEventListener('destroyEnvironment', function (e) {
				if (vm.currentEnvironment && vm.currentEnvironment.id == e.detail) {
					clearWorkspace();
					vm.currentEnvironment = {};
				}
			}, false);
		}, 1000);

		var paper = new joint.dia.Paper({
			el: $('#js-environment-creation'),
			width: '100%',
			height: '100%',
			model: graph,
			gridSize: 1
		});

		var p0;
		paper.on('cell:pointerdown', function (cellView) {
			p0 = cellView.model.get('position');
		});

		paper.on('cell:pointerup',
				function (cellView, evt, x, y) {

					var pos = cellView.model.get('position');
					var p1 = {x: g.snapToGrid(pos.x, GRID_CELL_SIZE) + 20, y: g.snapToGrid(pos.y, GRID_CELL_SIZE) + 20};

					var i = Math.floor(p1.x / GRID_CELL_SIZE);
					var j = Math.floor(p1.y / GRID_CELL_SIZE);

					if (vm.templateGrid[i] === undefined) {
						vm.templateGrid[i] = new Array();
					}

					if (vm.templateGrid[i][j] !== 1) {
						vm.templateGrid[i][j] = 1;
						cellView.model.set('position', p1);
						vm.cubeGrowth = vm.cubeGrowth < ( i + 1 ) ? ( i + 1 ) : vm.cubeGrowth;
						vm.cubeGrowth = vm.cubeGrowth < ( j + 1 ) ? ( j + 1 ) : vm.cubeGrowth;

						i = Math.floor(p0.x / GRID_CELL_SIZE);
						j = Math.floor(p0.y / GRID_CELL_SIZE);

						delete vm.templateGrid[i][j];
					}
					else
						cellView.model.set('position', p0);
				}
		);

		initScrollbar();

	}

	vm.buildStep = 'confirm';
	function buildEnvironmentByJoint() {

		vm.buildCompleted = false;
		clearTimeout(timeoutId);
        vm.logMessages = [];
		vm.newEnvID = [];

		var allElements = graph.getCells();
		vm.env2Build = {};
		vm.containers2Build = [];
		vm.buildStep = 'confirm';

		for (var i = 0; i < allElements.length; i++) {
			var currentElement = allElements[i];
			var isCustom = currentElement.get('quotaSize') == 'CUSTOM';
			var container2Build = {

				"quota": isCustom ?  {
					"containerSize":currentElement.get('quotaSize'),
                    "cpuQuota": currentElement.get("cpuQuota"),
                    "ramQuota": currentElement.get("ramQuota") + 'MiB',
                    "diskQuota": currentElement.get("diskQuota") + 'GiB',
				} : { "containerSize":currentElement.get('quotaSize') },

				"templateName": currentElement.get('templateName'),
				"name": currentElement.get('containerName'),
				"templateId" : currentElement.get('templateId'),
				"position": currentElement.get('position')
			};

			if (vm.env2Build[currentElement.get('templateName')] === undefined) {
				vm.env2Build[currentElement.get('templateName')] = {};
				vm.env2Build[currentElement.get('templateName')].count = 1;
				vm.env2Build[currentElement.get('templateName')].sizes = {};
				vm.env2Build[currentElement.get('templateName')].sizes[currentElement.get('quotaSize')] = 1;
				vm.env2Build[currentElement.get('templateName')].name = getTemplateNameById(currentElement.get('templateName'), vm.templatesList);
				vm.env2Build[currentElement.get('templateName')].id = currentElement.get('templateId');
			} else {
				vm.env2Build[currentElement.get('templateName')].count++;
				if (vm.env2Build[currentElement.get('templateName')].sizes[currentElement.get('quotaSize')] === undefined) {
					vm.env2Build[currentElement.get('templateName')].sizes[currentElement.get('quotaSize')] = 1;
				} else {
					vm.env2Build[currentElement.get('templateName')].sizes[currentElement.get('quotaSize')]++;
				}
			}

			vm.containers2Build.push(container2Build);
		}

		ngDialog.open({
			template: 'subutai-app/environment/partials/popups/environment-build-info.html',
			scope: $scope,
			className: 'b-build-environment-info',
			preCloseCallback: function (value) {
				if(vm.buildCompleted) {
					resetPlugin();
				}
				vm.buildCompleted = false;
			}
		});
	}

	function editEnvironment(environment) {

		if (environment.dataSource != "subutai") {
			SweetAlert.swal("Feature coming soon...", "This environment is created on Bazaar. Please use Bazaar to manage it.", "success");

			return;
		}
        vm.logMessages = [];
		clearWorkspace();
		vm.isApplyingChanges = false;
		vm.currentEnvironment = environment;
		vm.currentEnvironment.excludedContainers = [];
		vm.currentEnvironment.includedContainers = [];
		vm.currentEnvironment.changingContainers = [];
		vm.isEditing = true;

		for(var container in environment.containers) {
			var pos = vm.findEmptyCubePostion();
			var img = 'assets/templates/' + environment.containers[container].templateName + '.jpg';
			if(!imageExists(img)) {
				img = 'assets/templates/no-image.jpg';
			}

			if( environment.containers[container].containerName.match(/(\d+)(?!.*\d)/g) != null )
			{
				if( containerCounter < parseInt( environment.containers[container].containerName.match(/(\d+)(?!.*\d)/g) ) + 1 )
				{
					containerCounter = parseInt( environment.containers[container].containerName.match(/(\d+)(?!.*\d)/g) ) + 1;
				}
			}

			if( environment.containers[container].hostname.match(/(\d+)(?!.*\d)/g) != null )
			{
				if( containerCounter < parseInt( environment.containers[container].hostname.match(/(\d+)(?!.*\d)/g) ) + 1 )
				{
					containerCounter = parseInt( environment.containers[container].hostname.match(/(\d+)(?!.*\d)/g) ) + 1;
				}
			}

			var devElement = new joint.shapes.tm.devElement({
				position: { x: (GRID_CELL_SIZE * pos.x) + 20, y: (GRID_CELL_SIZE * pos.y) + 20 },
				edited: true,
				templateName: environment.containers[container].templateName,
				quotaSize: environment.containers[container].type,
				cpuQuota: environment.containers[container].quota.cpu,
				ramQuota: environment.containers[container].quota.ram,
				diskQuota: environment.containers[container].quota.disk,
				hostname: environment.containers[container].hostname,
				containerId: environment.containers[container].id,
				containerName: environment.containers[container].hostname,
				templateId : environment.containers[container].templateId,
				attrs: {
					image: { 'xlink:href': img },
					'rect.b-magnet': {fill: vm.colors[environment.containers[container].type]},
					title: {text: environment.containers[container].hostname + " ("
						+ environment.containers[container].templateName
							+ ") " + environment.containers[container].type
					}
				}
			});
			graph.addCell(devElement);
		}
		filterPluginsList();
	}

	function clearWorkspace() {
		vm.isEditing = false;
		vm.cubeGrowth = 0;
		vm.templateGrid = [];
		graph.resetCells();
		vm.environment2BuildName = '';
		filterPluginsList();
	}

	function addSettingsToTemplate(templateSettings, sizeDetails) {

        if (! /^[a-zA-Z][a-zA-Z0-9\-]{0,49}$/.test(templateSettings.containerName)){
                    SweetAlert.swal("Invalid hostname", "The container hostname must start with a letter and have as interior characters only letters, digits, and hyphen", "error");

                    return;
        }

        var isCustom = templateSettings.quotaSize == 'CUSTOM';

        currentTemplate.set('quotaSize', templateSettings.quotaSize);

        if(isCustom){
            currentTemplate.set('cpuQuota', templateSettings.cpuQuota );
            currentTemplate.set('ramQuota', templateSettings.ramQuota );
            currentTemplate.set('diskQuota', templateSettings.diskQuota );
        }

        currentTemplate.attr('title/text', templateSettings.containerName + ' (' + currentTemplate.get('templateName') +  ') ' + templateSettings.quotaSize);
		currentTemplate.attr('rect.b-magnet/fill', vm.colors[templateSettings.quotaSize]);
		currentTemplate.set('containerName', templateSettings.containerName);

		containerSettingMenu.hide();

        //for env modification
		if( vm.isEditing )
		{
			var id = currentTemplate.attributes.containerId;

			var res = $.grep( vm.currentEnvironment.containers, function( e, i ) {
				return e.id == id;
			});

			if( res[0] )
			{
				res = res[0];

                //checks if container size was changed back and removes from the set of containers to be updated
				if( res.type == templateSettings.quotaSize && vm.currentEnvironment.changingContainers[id] && !isCustom )
				{
					delete vm.currentEnvironment.changingContainers[id];
				}

                //if container size is changed then adds to the set of containers to be updated
				if( res.type != templateSettings.quotaSize || isCustom )
				{
					vm.currentEnvironment.changingContainers[id] = { "containerSize" : templateSettings.quotaSize };

					if( isCustom ){
                        vm.currentEnvironment.changingContainers[id].cpuQuota = templateSettings.cpuQuota;
                        vm.currentEnvironment.changingContainers[id].ramQuota = templateSettings.ramQuota;
                        vm.currentEnvironment.changingContainers[id].diskQuota = templateSettings.diskQuota;
					}
				}
			}
		}
	}

}

function shortenIdName( name, factor )
{
	return name.substring(0, factor) + '..' + name.substring(name.length - factor, name.length);
}
