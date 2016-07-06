'use strict';

angular.module('subutai.environment.simple-controller', [])
	.controller('EnvironmentSimpleViewCtrl', EnvironmentSimpleViewCtrl);

EnvironmentSimpleViewCtrl.$inject = ['$scope', '$rootScope', 'environmentService', 'trackerSrv', 'SweetAlert', 'ngDialog', '$timeout'];

function EnvironmentSimpleViewCtrl($scope, $rootScope, environmentService, trackerSrv, SweetAlert, ngDialog, $timeout) {

	var vm = this;
	var GRID_CELL_SIZE = 100;
	var containerSettingMenu = $('.js-dropen-menu');
	var currentTemplate = {};
	$scope.identity = angular.identity;

	vm.popupLogState = 'full';

	vm.currentEnvironment = {};
	vm.buildEnvironment = buildEnvironment;
	vm.editEnvironment = editEnvironment;
	vm.notifyChanges = notifyChanges;
	vm.applyChanges = applyChanges;

	vm.environments = [];

	vm.colors = quotaColors;
	vm.templates = [];
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

	// @todo workaround
	environmentService.getTemplates()
		.then(function (data) {
			vm.templates = data;
			getFilteredTemplates();
		});

	function getFilteredTemplates() {
		vm.templatesList = [];
		for(var i in vm.templates) {
			if(vm.templatesType == 'all' || i == vm.templatesType) {
				vm.templatesList = vm.templatesList.concat(vm.templates[i]);
			}
		}
	}

	function resetPlugin() {
		if(vm.selectedPlugin.selected !== undefined) vm.selectedPlugin.selected = false;
		vm.selectedPlugin = false;
	}

	function closePopup() {
		vm.buildCompleted = false;
		resetPlugin();
		ngDialog.closeAll();
	}

	function getLogsFromTracker(environmentId) {
		trackerSrv.getOperations('ENVIRONMENT MANAGER', moment().format('YYYY-MM-DD'), moment().format('YYYY-MM-DD'), 100)
			.success(function (data) {
				for(var i = 0; i < data.length; i++) {
					if(data[i].description.includes(environmentId)) {
						getLogById(data[i].id, true);
						break;
					}
				}
				return false;
			}).error(function(error) {
				console.log(error);
			});		
	}

	var timezone = new Date().getTimezoneOffset();
	function checkLastLog(status, log) {
		if(log === undefined || log === null) log = false;
		var lastLog = vm.logMessages[vm.logMessages.length - 1];

		if(log) {
			var logObj = JSON.parse(log.substring(0, log.length - 1));
			lastLog.time = moment(logObj.date).format('HH:mm:ss');
		} else {
			lastLog.time = moment().format('HH:mm:ss');
		}

		if(status === true) {
			lastLog.status = 'success';
			lastLog.classes = ['fa-check', 'g-text-green'];
		} else {
			lastLog.status = 'success';
			lastLog.classes = ['fa-times', 'g-text-red'];
		}
	}

	function getLogById(id, checkLast, prevLogs) {
		if(checkLast === undefined || checkLast === null) checkLast = false;
		if(prevLogs === undefined || prevLogs === null) prevLogs = false;
		trackerSrv.getOperation('ENVIRONMENT MANAGER', id)
			.success(function (data) {
				if(data.state == 'RUNNING') {

					if(checkLast) {
						checkLastLog(true);
					}

					var logs = data.log.split(/(?:\r\n|\r|\n)/g);
					var result = [];
					var i = 0;
					if(prevLogs) {
						i = prevLogs.length;
						if(logs.length > prevLogs.length) {
							checkLastLog(true, logs[i-1]);
						}
					}
					for(i; i < logs.length; i++) {

						var logCheck = logs[i].replace(/ /g,'');
						if(logCheck.length > 0) {

							var logObj = JSON.parse(logs[i].substring(0, logs[i].length - 1));
							var logTime = moment(logObj.date).format('HH:mm:ss');

							var logStatus = 'success';
							var logClasses = ['fa-check', 'g-text-green'];

							if(i+1 == logs.length) {
								logTime = '';
								logStatus = 'in-progress';
								logClasses = ['fa-spinner', 'fa-pulse'];
							}

							var  currentLog = {
								"time": logTime,
								"status": logStatus,
								"classes": logClasses,
								"text": logObj.log
							};
							result.push(currentLog);

						}
					}

					vm.logMessages = vm.logMessages.concat(result);

					setTimeout(function() {
						getLogById(id, false, logs);
					}, 2000);					

					return result;
				} else {
					if(data.state == 'FAILED') {
						checkLastLog(false);
					} else {
						//SweetAlert.swal("Success!", "Your environment has been built successfully.", "success");

						if(prevLogs) {
							var logs = data.log.split(/(?:\r\n|\r|\n)/g);
							if(logs.length > prevLogs.length) {
								checkLastLog(true, logs[logs.length-1]);
							}
						} else {
							checkLastLog(true);
						}
						var currentLog = {
							"time": moment().format('HH:mm:ss'),
							"status": 'success',
							"classes": ['fa-check', 'g-text-green'],
							"text": 'Your environment has been built successfully'
						};
						vm.logMessages.push(currentLog);						
						vm.buildCompleted = true;
						vm.isEditing = false;
					}

					$rootScope.notificationsUpdate = 'getLogById';
					$scope.$emit('reloadEnvironmentsList');
					clearWorkspace();
				}
			}).error(function(error) {
				console.log(error);
			});
	}

	vm.logMessages = [];
	function buildEnvironment() {
		vm.buildStep = 'showLogs';

		vm.buildCompleted = false;
		vm.logMessages = [];
		var currentLog = {
			"time": '',
			"status": 'in-progress',
			"classes": ['fa-spinner', 'fa-pulse'],
			"text": 'Registering environment'
		};
		vm.logMessages.push(currentLog);

		environmentService.startEnvironmentAutoBuild(vm.environment2BuildName, JSON.stringify(vm.containers2Build))
			.success(function(data){
				vm.newEnvID = data;
				currentLog.status = 'success';
				currentLog.classes = ['fa-check', 'g-text-green'];
				currentLog.time = moment().format('HH:mm:ss');

				currentLog = {
					"time": '',
					"status": 'in-progress',
					"classes": ['fa-spinner', 'fa-pulse'],
					"text": 'Environment creation has been started'
				};
				vm.logMessages.push(currentLog);

				//var logId = getLogsFromTracker(vm.environment2BuildName);
				getLogById(data, true);
				initScrollbar();

				$rootScope.notificationsUpdate = 'buildEnvironment';
			}).error(function(error){
				if(error && error.ERROR === undefined) {
					VARS_MODAL_ERROR( SweetAlert, 'Error: ' + error );
				} else {
					VARS_MODAL_ERROR( SweetAlert, 'Error: ' + error.ERROR );
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

		ngDialog.open({
			template: 'subutai-app/environment/partials/popups/environment-modification-info.html',
			scope: $scope,
			className: 'b-build-environment-info',
			preCloseCallback: function(value) {
				vm.buildCompleted = false;
				resetPlugin();
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
			includedContainers.push({
				"size": vm.currentEnvironment.includedContainers[i].get('quotaSize'),
				"templateName": vm.currentEnvironment.includedContainers[i].get('templateName'),
				"name": vm.currentEnvironment.includedContainers[i].get('containerName'),
				"position": vm.currentEnvironment.includedContainers[i].get('position')
			});
		}
		vm.currentEnvironment.modificationData = {
			topology: includedContainers,
			removedContainers: excludedContainers,
			environmentId: vm.currentEnvironment.id
		};

		ngDialog.open({
			template: 'subutai-app/environment/partials/popups/environment-modification-status.html',
			scope: $scope,
			className: 'b-build-environment-info',
			preCloseCallback: function(value) {
				vm.buildCompleted = false;
				resetPlugin();
			}
		});

		vm.currentEnvironment.modifyStatus = 'modifying';

		vm.buildCompleted = false;
		vm.logMessages = [];
		var currentLog = {
			"time": '',
			"status": 'in-progress',
			"classes": ['fa-spinner', 'fa-pulse'],
			"text": 'Applying your changes...'
		};
		vm.logMessages.push(currentLog);

		environmentService.modifyEnvironment(vm.currentEnvironment.modificationData).success(function (data) {
			vm.currentEnvironment.modifyStatus = 'modified';
			clearWorkspace();
			vm.isApplyingChanges = false;

			getLogById(data, true);
			initScrollbar();
			$rootScope.notificationsUpdate = 'modifyEnvironment';
		}).error(function (data) {
			vm.currentEnvironment.modifyStatus = 'error';
			clearWorkspace();
			vm.isApplyingChanges = false;
			
			checkLastLog(false);
			$rootScope.notificationsUpdate = 'modifyEnvironmentError';
		});
	}

	var graph = new joint.dia.Graph;
	
	//custom shapes
	joint.shapes.tm = {};

	//simple creatiom templates
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
				text: { 'font-weight': 400, 'font-size': 'small', fill: 'black', 'text-anchor': 'middle', 'ref-x': .5, 'ref-y': .5, 'y-alignment': 'middle' },
				'rect.b-magnet': {fill: '#04346E', width: 15, height: 15, rx: 50, ry: 50, transform: 'translate(26,51)'},
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
			size: { width: 70, height: 70 },
			attrs: {
				title: {text: 'Static Tooltip'},
				'rect.b-border': {fill: '#fff', stroke: '#dcdcdc', 'stroke-width': 1, width: 70, height: 70, rx: 50, ry: 50},
				//'rect.b-magnet': {fill: '#04346E', width: 10, height: 10, rx: 50, ry: 50, magnet: true, transform: 'translate(30,53)'},
				image: {'ref-x': 9, 'ref-y': 9, ref: 'rect', width: 50, height: 50},
			}
		}, joint.shapes.tm.toolElement.prototype.defaults)
	});

	//custom view
	joint.shapes.tm.ToolElementView = joint.dia.ElementView.extend({
		initialize: function() {
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
		mouseover: function(evt, x, y) {
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
						object !== null ? vm.currentEnvironment.includedContainers.splice(object.index, 1): null;
					}
					this.model.remove();
					delete vm.templateGrid[Math.floor(x / GRID_CELL_SIZE)][Math.floor(y / GRID_CELL_SIZE)];

					filterPluginsList();

					return;
					break;
				case 'element-tool-copy':
					addContainer(
						this.model.attributes.templateName,
						false,
						this.model.attributes.quotaSize,
						getTemplateNameById(this.model.attributes.templateName)
					);

					return;
					break;
				case 'element-call-menu':
				case 'b-container-plus-icon':
					currentTemplate = this.model;
					$('#js-container-name').val(currentTemplate.get('containerName')).trigger('change');
					$('#js-container-size').val(currentTemplate.get('quotaSize')).trigger('change');
					containerSettingMenu.find('.header').text('Settings ' + this.model.get('templateName'));
					var elementPos = this.model.get('position');
					containerSettingMenu.css({
						'left': (elementPos.x + 12) + 'px',
						'top': (elementPos.y + 73) + 'px',
						'display': 'block'
					});
					return;
					break;
				case 'rotatable':
					if(this.model.attributes.containerId) {
						return;
					}
					/*vm.currentTemplate = this.model;
					ngDialog.open({
						template: 'subutai-app/environment/partials/popups/templateSettings.html',
						scope: $scope
					});*/
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
			$('.js-call-plugins-popup').on('click', function() {
				$('.js-environment-plugins-menu').stop().show(300);
			});
			filterPluginsList();
		});
	}
	getPlugins();

	function filterPluginsList() {
		var allElements = graph.getCells();

		if(allElements.length > 0) {
			vm.filteredPlugins = {};
			for(var i = 0; i < allElements.length; i++) {

				var currentTemplate = allElements[i].get('templateName');

				for(var j = 0; j < vm.plugins.length; j++) {

					var currentPlugin = vm.plugins[j];

					if(vm.filteredPlugins[currentPlugin.name] == undefined) {
						if(currentPlugin.requirement !== undefined) {
							var requirementArray = Object.keys(currentPlugin.requirement);
							if(requirementArray.indexOf(currentTemplate) > -1) {
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

		if(vm.selectedPlugin) {
			vm.selectedPlugin.selected = false;
		}

		vm.selectedPlugin = plugin;
		vm.selectedPlugin.selected = true;
	}

	function setTemplatesByPlugin() {

		if(vm.selectedPlugin.requirement !== undefined) {
			for(var template in vm.selectedPlugin.requirement) {
				for(var i = 0; i < vm.selectedPlugin.requirement[template]; i++) {
					var allElements = graph.getCells();
					if(allElements.length > 0) {
						var alreadyONWorckspace = false;
						var templatesCounter = 0;
						for(var j = 0; j < allElements.length; j++) {
							var currentTemplate = allElements[j].get('templateName');
							if(currentTemplate == template.toLowerCase()) {
								alreadyONWorckspace = true;
								templatesCounter++;
								//break;
							}
						}
						if(!alreadyONWorckspace || templatesCounter == i) {
							addContainer(template.toLowerCase());
						}
					} else {
						addContainer(template.toLowerCase());
					}
				}
			}
		}
		$('.b-template-settings').stop().slideUp(100);

		//getPlugins();
	}

	var containerCounter = 1;
	function addContainer(template, $event, size, templateImg) {
		console.log(template);
		if($event === undefined || $event === null) $event = false;

		if(size === undefined || size === null) {
			size = 'SMALL';
			if(template == 'appscale') {
				size = 'HUGE';
			}
		}
		if(templateImg === undefined || templateImg === null) templateImg = template;

		var pos = findEmptyCubePostion();
		var img = 'assets/templates/' + templateImg + '.jpg';

		if($event) {
			img = $($event.currentTarget).find('img').attr('src');
		} else {
			if(!imageExists(img)) {
				img = 'assets/templates/no-image.jpg';
			}
		}

		var devElement = new joint.shapes.tm.devElement({
			position: { x: (GRID_CELL_SIZE * pos.x) + 20, y: (GRID_CELL_SIZE * pos.y) + 20 },
			templateName: template,
			quotaSize: size,
			containerName: 'Container ' + (containerCounter++).toString(),
			attrs: {
				image: { 'xlink:href': img },
				'rect.b-magnet': {fill: vm.colors[size]},
				title: {text: template}
			}
		});
		vm.isEditing ? vm.currentEnvironment.includedContainers.push(devElement) : null;
		graph.addCell(devElement);
		filterPluginsList();
		return false;
	}

	function findEmptyCubePostion() {
		for( var j = 0; j < vm.cubeGrowth; j++ ) {
			for( var i = 0; i < vm.cubeGrowth; i++ ) {
				if( vm.templateGrid[i] === undefined ) {
					vm.templateGrid[i] = new Array();
					vm.templateGrid[i][j] = 1;

					return {x:i, y:j};
				}

				if( vm.templateGrid[i][j] !== 1 ) {
					vm.templateGrid[i][j] = 1;
					return {x:i, y:j};
				}
			}
		}

		vm.templateGrid[vm.cubeGrowth] = new Array();
		vm.templateGrid[vm.cubeGrowth][0] = 1;
		vm.cubeGrowth++;
		return { x : vm.cubeGrowth - 1, y : 0 };
	}

	vm.findEmptyCubePostion = findEmptyCubePostion;	

	function initJointJs() {

		setTimeout(function (){
			document.getElementById('js-environment-creation').addEventListener('destroyEnvironment', function (e) {
				if(vm.currentEnvironment && vm.currentEnvironment.id == e.detail) {
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
		paper.on('cell:pointerdown', function(cellView) {
			p0 = cellView.model.get('position');
		});

		paper.on('cell:pointerup',
			function(cellView, evt, x, y) {

				var pos = cellView.model.get('position');
				var p1 = { x: g.snapToGrid(pos.x, GRID_CELL_SIZE) + 20, y: g.snapToGrid(pos.y, GRID_CELL_SIZE) + 20 };

				var i = Math.floor( p1.x / GRID_CELL_SIZE );
				var j = Math.floor( p1.y / GRID_CELL_SIZE );

				if( vm.templateGrid[i] === undefined )
				{
					vm.templateGrid[i] = new Array();
				}

				if( vm.templateGrid[i][j] !== 1 )
				{
					vm.templateGrid[i][j] = 1;
					cellView.model.set('position', p1);
					vm.cubeGrowth = vm.cubeGrowth < ( i + 1 ) ? ( i + 1 ) : vm.cubeGrowth;
					vm.cubeGrowth = vm.cubeGrowth < ( j + 1 ) ? ( j + 1 ) : vm.cubeGrowth;

					i = Math.floor( p0.x / GRID_CELL_SIZE );
					j = Math.floor( p0.y / GRID_CELL_SIZE );

					delete vm.templateGrid[i][j];
				}
				else
					cellView.model.set('position', p0);
			}
		);

		initScrollbar();

		//zoom on scroll
		/*paper.$el.on('mousewheel DOMMouseScroll', onMouseWheel);

		function onMouseWheel(e) {

			e.preventDefault();
			e = e.originalEvent;

			var delta = Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail))) / 50;
			var offsetX = (e.offsetX || e.clientX - $(this).offset().left); // offsetX is not defined in FF
			var offsetY = (e.offsetY || e.clientY - $(this).offset().top); // offsetY is not defined in FF
			var p = offsetToLocalPoint(offsetX, offsetY);
			var newScale = V(paper.viewport).scale().sx + delta; // the current paper scale changed by delta

			if (newScale > 0.4 && newScale < 2) {
				paper.setOrigin(0, 0); // reset the previous viewport translation
				paper.scale(newScale, newScale, p.x, p.y);
			}
		}

		function offsetToLocalPoint(x, y) {
			var svgPoint = paper.svg.createSVGPoint();
			svgPoint.x = x;
			svgPoint.y = y;
			// Transform point into the viewport coordinate system.
			var pointTransformed = svgPoint.matrixTransform(paper.viewport.getCTM().inverse());
			return pointTransformed;
		}*/
	}

	vm.buildStep = 'confirm';
	function buildEnvironmentByJoint() {

		vm.buildCompleted = false;

		vm.newEnvID = [];		

		var allElements = graph.getCells();
		vm.env2Build = {};
		vm.containers2Build = [];
		vm.buildStep = 'confirm';

		for(var i = 0; i < allElements.length; i++) {
			var currentElement = allElements[i];
			var container2Build = {
				"size": currentElement.get('quotaSize'),
				"templateName": currentElement.get('templateName'),
				"name": currentElement.get('containerName'),
				"position": currentElement.get('position')
			};

			if (vm.env2Build[currentElement.get('templateName')] === undefined) {
				vm.env2Build[currentElement.get('templateName')] = {};
				vm.env2Build[currentElement.get('templateName')].count = 1;
				vm.env2Build[currentElement.get('templateName')].sizes = {};
				vm.env2Build[currentElement.get('templateName')].sizes[currentElement.get('quotaSize')] = 1;
				vm.env2Build[currentElement.get('templateName')].name = getTemplateNameById( currentElement.get('templateName') );
			} else {
				vm.env2Build[currentElement.get('templateName')].count++;
				if(vm.env2Build[currentElement.get('templateName')].sizes[currentElement.get('quotaSize')] === undefined) {
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
			preCloseCallback: function(value) {
				vm.buildCompleted = false;
				resetPlugin();
			}
		});
	}

	function editEnvironment(environment) {

		if ( environment.dataSource == "hub" )
		{
			SweetAlert.swal( "Feature coming soon...", "This environment created on Hub. Please use Hub to manage it.", "success");

			return;
		}

		clearWorkspace();
		vm.isApplyingChanges = false;
		vm.currentEnvironment = environment;
		vm.currentEnvironment.excludedContainers = [];
		vm.currentEnvironment.includedContainers = [];
		vm.isEditing = true;
		for(var container in environment.containers) {
			var pos = vm.findEmptyCubePostion();
			var img = 'assets/templates/' + environment.containers[container].templateName + '.jpg';
			if(!imageExists(img)) {
				img = 'assets/templates/no-image.jpg';
			}
			var devElement = new joint.shapes.tm.devElement({
				position: { x: (GRID_CELL_SIZE * pos.x) + 20, y: (GRID_CELL_SIZE * pos.y) + 20 },
				templateName: environment.containers[container].templateName,
				quotaSize: environment.containers[container].type,
				hostname: environment.containers[container].hostname,
				containerId: environment.containers[container].id,
				attrs: {
					image: { 'xlink:href': img },
					'rect.b-magnet': {fill: vm.colors[environment.containers[container].type]},
					title: {text: environment.containers[container].templateName}
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
		//vm.selectedPlugin = false;
	}

	function addSettingsToTemplate(settings) {

		currentTemplate.set('quotaSize', settings.quotaSize);
		currentTemplate.attr('rect.b-magnet/fill', vm.colors[settings.quotaSize]);
		currentTemplate.set('containerName', settings.containerName);
		//ngDialog.closeAll();
		containerSettingMenu.hide();
	}

	function getElementByField(field, value, collection) {
		for(var index = 0; index < collection.length; index++) {
			if(collection[index][field] === value) {
				return {
					container: collection[index],
					index: index
				};
			}
		}
		return null;
	}

	function getTemplateNameById( id )
	{
		var arr = jQuery.grep(vm.templatesList, function( e ) {
			return ( e.id == id );
		});

		if( arr.length > 0 && arr[0].name.length > 0 )
		{
			return arr[0].name;
		}

		return id;
	}
}
