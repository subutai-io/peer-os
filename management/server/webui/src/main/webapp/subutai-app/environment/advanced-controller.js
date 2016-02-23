'use strict';

angular.module('subutai.environment.adv-controller', [])
	.controller('AdvancedEnvironmentCtrl', AdvancedEnvironmentCtrl);

AdvancedEnvironmentCtrl.$inject = ['$scope', '$rootScope', 'environmentService', 'trackerSrv', 'SweetAlert', 'ngDialog'];

var graph = new joint.dia.Graph;
var GRID_SIZE = 60;
var GRID_SPACING = 5;

var PEER_MAP = {};
var PEER_WIDTH = 155;
var PEER_SPACE = 30;

var RH_WIDTH = 100;
var RH_SPACE = 10;

function AdvancedEnvironmentCtrl($scope, $rootScope, environmentService, trackerSrv, SweetAlert, ngDialog) {

	var vm = this;
	var GRID_CELL_SIZE = 100;
	var GRID_SIZE = 100;

	vm.buildEnvironment = buildEnvironment;

	vm.domainStrategies = [];
	vm.strategies = [];
	vm.activeCloudTab = 'peers';

	vm.peerIds = [];
	vm.resourceHosts = [];
	vm.currentResourceHosts = [];
	vm.advancedEnv = {};
	vm.nodeStatus = 'Add to';
	vm.nodeList = [];
	vm.colors = quotaColors;
	vm.templates = [];

	vm.cubeGrowth = 1;
	vm.environment2BuildName = 'Environment name';
	vm.currentPeer = false;
	vm.currentPeerIndex = false;
	vm.buildCompleted = false;

	// functions

	vm.initJointJs = initJointJs;
	vm.buildEnvironmentByJoint = buildEnvironmentByJoint;
	vm.editEnvironment = editEnvironment;
	vm.clearWorkspace = clearWorkspace;
	vm.addSettingsToTemplate = addSettingsToTemplate;

	vm.showResources = showResources;
	vm.addResource2Build = addResource2Build;
	vm.closePopup = closePopup;

	environmentService.getTemplates()
		.success(function (data) {
			vm.templates = data;
		})
		.error(function (data) {
			VARS_MODAL_ERROR( SweetAlert, 'Error on getting templates ' + data );
		});

	//vm.templates = ['mongo', 'cassandra', 'master', 'hadoop'];

	environmentService.getStrategies().success(function (data) {
		vm.strategies = data;
	});

	environmentService.getDomainStrategies().success(function (data) {
		vm.domainStrategies = data;
	});

	environmentService.getPeers().success(function (data) {
		vm.peerIds = data;
		//vm.peerIds['testPeer'] = ['rh1', 'rh2', 'rh3'];
	});
	clearWorkspace();

	/*peerRegistrationService.getResourceHosts().success(function (data) {
		vm.resourceHosts = data;
	});*/

	function closePopup() {
		vm.buildCompleted = false;
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

	function checkLastLog(status) {
		var lastLog = vm.logMessages[vm.logMessages.length - 1];
		lastLog.time = moment().format('HH:mm:ss');
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
							checkLastLog(true);
						}
					}
					for(i; i < logs.length; i++) {

						var logTime = moment().format('HH:mm:ss');
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
							"text": logs[i]
						};
						result.push(currentLog);
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
						checkLastLog(true);
						var currentLog = {
							"time": moment().format('HH:mm:ss'),
							"status": 'success',
							"classes": ['fa-check', 'g-text-green'],
							"text": 'Your environment has been built successfully'
						};
						vm.logMessages.push(currentLog);						
						vm.buildCompleted = true;
					}
				}
			}).error(function(error) {
				console.log(error);
			});
	}

	vm.logMessages = [];
	function buildEnvironment() {
		vm.buildStep = 'showLogs';

		vm.logMessages = [];
		var currentLog = {
			"time": '',
			"status": 'in-progress',
			"classes": ['fa-spinner', 'fa-pulse'],
			"text": 'Registering environment'
		};
		vm.logMessages.push(currentLog);

		environmentService.startEnvironmentAdvancedBuild(vm.environment2BuildName, JSON.stringify(vm.containers2Build))
			.success(function(data){
				vm.newEnvID = data;

				$rootScope.notifications = {
					"message": "Environment(" + data + ") creation has been started", 
					"date": moment().format('MMMM Do YYYY, HH:mm:ss')
				};

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

				//var logId = getLogsFromTracker(vm.newEnvID);
				var logId = getLogsFromTracker(vm.environment2BuildName);

			}).error(function(error){
				if(error && error.ERROR === undefined) {
					VARS_MODAL_ERROR( SweetAlert, 'Error: ' + error );
				} else {
					VARS_MODAL_ERROR( SweetAlert, 'Error: ' + error.ERROR );
				}
				currentLog.status = 'fail';
				currentLog.classes = ['fa-times', 'g-text-red'];
				currentLog.time = moment().format('HH:mm:ss');				

				$rootScope.notifications = {
					"message": "Error on creating environment. " + error, 
					"date": moment().format('MMMM Do YYYY, HH:mm:ss')
				};
			});
	}

	function showResources(peerId, resourcesId, index) {
		vm.currentResourceHosts = resourcesId;
		vm.currentPeer = peerId;
		vm.currentPeerIndex = index;
		$('.b-cloud-add-tools').animate({'left': '-200px'}, 300);
	}

	//add resource host
	function addResource2Build(currentResource, peerId, index) {
		var posX = calculatePeerPos();

		if( PEER_MAP[peerId] !== undefined ) {
			if( PEER_MAP[peerId].rh[currentResource] !== undefined ) {
				return PEER_MAP[peerId].rh[currentResource];
			}
		} else {
			PEER_MAP[peerId] = { rh: [], position : posX };
		}

		if(Object.keys(PEER_MAP[peerId].rh).length > 0) {
			var lastResourceInPeer = graph.getCell(PEER_MAP[peerId].rh[
				Object.keys(PEER_MAP[peerId].rh)[
					Object.keys(PEER_MAP[peerId].rh).length - 1
				]
			]);
			lastResourceInPeer.set('addClass', '');
			lastResourceInPeer.set('removeClass', 'b-resource-host_last');
		}
		var posY = calculateResourceHostPos(peerId);

		var resourceHost = new joint.shapes.resourceHostHtml.Element({
			position: { x: PEER_MAP[peerId].position * ( PEER_WIDTH + PEER_SPACE ), y: posY },
			size: { width: 155, height: 173 },
			peerId: peerId,
			hostId: currentResource,
			children: 0,
			grid: [],
			gridSize: { size: 2 },
			'resourceHostName': 'RH' + (index + 1),
			'peerName': 'Peer ' + (vm.currentPeerIndex + 1),
			'addClass': 'b-resource-host_last'
		});

		graph.addCell(resourceHost);
		PEER_MAP[peerId].rh[currentResource] = resourceHost.id;

		return resourceHost.id;
	}

	function calculateResourceHostPos(peerId) {
		if(PEER_MAP[peerId]) {
			var posY = 30;
			for(var key in PEER_MAP[peerId].rh) {
				var currentResource = graph.getCell(PEER_MAP[peerId].rh[key]);
				var currentResourceSize = currentResource.get('size');
				var currentResourcePos = currentResource.get('position');
				posY = posY + (currentResourceSize.height - 8);
			}
			return posY;
		} else {
			return 30;
		}
	}

	function calculatePeerPos() {
		var pos = [];

		for (var key in PEER_MAP) {
			if (PEER_MAP.hasOwnProperty(key)) {
				pos.push( PEER_MAP[key].position );
			}
		}

		if( pos.length == 0 )
			return 0;
		if( pos.length == 1 )
			return 1;

		pos.sort();

		for( var i = 1; i < pos.length; i++ )
		{
			if( pos[i - 1] + 1 !== pos[i] )
			{
				return pos[i - 1] + 1;
			}
		}

		return pos[pos.length - 1];
	}

	//custom shapes
	joint.shapes.tm = {};

	//simple creatiom templates
	joint.shapes.tm.toolElement = joint.shapes.basic.Generic.extend({

		toolMarkup: [
			'<g class="element-tools">',
				'<g class="element-tool-remove">',
					'<circle fill="#F8FBFD" r="8" stroke="#dcdcdc"/>',
					'<polygon transform="scale(1.2) translate(-5, -5)" fill="#292F6C" points="8.4,2.4 7.6,1.6 5,4.3 2.4,1.6 1.6,2.4 4.3,5 1.6,7.6 2.4,8.4 5,5.7 7.6,8.4 8.4,7.6 5.7,5 "/>',
					'<title>Remove</title>',
				'</g>',
				/*'<g class="element-call-menu">',
					'<circle fill="#F8FBFD" r="8" stroke="#dcdcdc"/>',
					'<polygon transform="scale(1.2) translate(-5, -5)" fill="#292F6C" points="8.4,2.4 7.6,1.6 5,4.3 2.4,1.6 1.6,2.4 4.3,5 1.6,7.6 2.4,8.4 5,5.7 7.6,8.4 8.4,7.6 5.7,5 "/>',
					'<title>Menu</title>',
				'</g>',*/
			'</g>'
		].join(''),

		defaults: joint.util.deepSupplement({
			attrs: {
				text: { 'font-weight': 400, 'font-size': 'small', fill: 'black', 'text-anchor': 'middle', 'ref-x': .5, 'ref-y': .5, 'y-alignment': 'middle' },
				'g.element-call-menu': {'ref-x': 18, 'ref-y': 25}
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
				'<rect class="b-magnet"/>',
				'<g class="b-container-plus-icon">',
					'<line fill="none" stroke="#FFFFFF" stroke-miterlimit="10" x1="0" y1="4.5" x2="9" y2="4.5"/>',
					'<line fill="none" stroke="#FFFFFF" stroke-miterlimit="10" x1="4.5" y1="0" x2="4.5" y2="9"/>',
				'</g>',
			'</g>'
		].join(''),

		defaults: joint.util.deepSupplement({
			type: 'tm.devElement',
			size: { width: 40, height: 40 },
			attrs: {
				title: {text: 'Static Tooltip'},
				'rect.b-border': {fill: '#fff', stroke: '#dcdcdc', 'stroke-width': 1, width: 40, height: 40, rx: 50, ry: 50},
				//'rect.b-magnet': {fill: '#04346E', width: 10, height: 10, rx: 50, ry: 50, magnet: true, transform: 'translate(16,28)'},
				'rect.b-magnet': {fill: '#04346E', width: 10, height: 10, rx: 50, ry: 50, transform: 'translate(16,28)'},
				'g.b-container-plus-icon': {'ref-x': 17.5, 'ref-y': 30, ref: 'rect', transform: 'scale(0.8)'},
				image: {'ref-x': 9, 'ref-y': 9, ref: 'rect', width: 25, height: 25},
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
					var rh = this.model.attributes.rh;
					var resourceHost = graph.getCell(rh.model);
					resourceHost.set('children', resourceHost.get('children') - 1);
					delete graph.getCell(rh.model).attributes.grid[rh.x][rh.y];
					this.model.remove();
					return;
					break;
				case 'element-call-menu':
					console.log(this.model);
					var elementPos = this.model.get('position');
					$('.js-dropen-menu').css({
						'left': (elementPos.x + 70) + 'px',
						'top': (elementPos.y + 83) + 'px',
						'display': 'block'
					});
					return;
					break;
				case 'rotatable':
					console.log(this.model);
					vm.currentTemplate = this.model;
					ngDialog.open({
						template: 'subutai-app/environment/partials/popups/templateSettingsAdvanced.html',
						scope: $scope
					});
					return;
					break;
				default:
			}
			joint.dia.CellView.prototype.pointerclick.apply(this, arguments);
		}
	});
	joint.shapes.tm.devElementView = joint.shapes.tm.ToolElementView;

	//resource host object
	joint.shapes.resourceHostHtml = {};
	joint.shapes.resourceHostHtml.Element = joint.shapes.basic.Rect.extend({

		markup: [
			'<g class="rotatable">',
				'<g class="scalable">',
					'<rect />',
				'</g>',
			'</g>',
			'<g class="element-tool-remove">',
				'<circle fill="#F8FBFD" r="10" stroke="#dcdcdc"/>',
				'<polygon transform="scale(1.2) translate(-5, -5)" fill="#292F6C" points="8.4,2.4 7.6,1.6 5,4.3 2.4,1.6 1.6,2.4 4.3,5 1.6,7.6 2.4,8.4 5,5.7 7.6,8.4 8.4,7.6 5.7,5 "/>',
				'<title>Remove</title>',
			'</g>',
		].join(''),

		defaults: joint.util.deepSupplement({
			type: 'resourceHostHtml.Element',
			attrs: {
				rect: { stroke: 'none', 'fill-opacity': 0, 'pointer-events':'none' },
				'g.element-tool-remove': {'ref-x': 156, 'ref-y': 0}
			}
		}, joint.shapes.basic.Rect.prototype.defaults)
	});

	// Resource host html.
	// -------------------------------------------------------------------------

	joint.shapes.resourceHostHtml.ElementView = joint.dia.ElementView.extend({

		template: [
			'<div class="b-resource-host">',
				'<div class="b-peer-title"></div>',
				//'<button class="b-peer-delete js-delete-peer"></button>',
				'<div class="b-resource-host__inner">',
					'<div class="b-recouce-host-title"></div>',
					'<div class="b-resource-host__containers">',
						'<div class="b-resource-host-containers">',
						'</div>',
					'</div>',
				'</div>',
			'</div>'
		].join(''),

		initialize: function() {
			_.bindAll(this, 'updateBox');
			joint.dia.ElementView.prototype.initialize.apply(this, arguments);

			this.$box = $(_.template(this.template)());
			this.$box.find('.js-delete-peer').on('click', _.bind(this.model.remove, this.model));
			this.model.on('change', this.updateBox, this);
			// Remove the box when the model gets removed from the graph.
			this.model.on('remove', this.removeBox, this);

			this.updateBox();
		},
		render: function() {
			joint.dia.ElementView.prototype.render.apply(this, arguments);
			this.paper.$el.prepend(this.$box);
			this.updateBox();
			return this;
		},
		pointerclick: function (evt, x, y) {
			var className = evt.target.parentNode.getAttribute('class');
			switch (className) {
				case 'element-tool-remove':
					this.model.remove();
					return;
					break;
				default:
			}
		},
		pointermove: function(evt, x, y) {
			return false;
		},
		updateBox: function() {
			// Set the position and dimension of the box so that it covers the JointJS element.
			var bbox = this.model.getBBox();

			if(this.model.get('addClass')) {
				this.$box.addClass(this.model.get('addClass'));
			}

			if(this.model.get('removeClass')) {
				this.$box.removeClass(this.model.get('removeClass'));
			}

			this.$box.find('.b-peer-title').text(this.model.get('peerName'));
			this.$box.find('.b-recouce-host-title').text(this.model.get('resourceHostName'));
			this.$box.css({ width: bbox.width, height: bbox.height, left: bbox.x, top: bbox.y, transform: 'rotate(' + (this.model.get('angle') || 0) + 'deg)' });
		},
		removeBox: function(evt) {
			var parentPeerId = this.model.get('peerId');
			var rhKeys = Object.keys(PEER_MAP[parentPeerId].rh);
			var hostIndex = rhKeys.indexOf(this.model.get('hostId'));
			//var swapFrom = rhKeys.slice(hostIndex, -1);

			var emptyPlace = this.model.get('size').height - 8;
			delete PEER_MAP[parentPeerId].rh[this.model.get('hostId')];

			if(Object.keys(PEER_MAP[parentPeerId].rh).length == 0) {

				var peerWidth = this.model.get('size').width;
				var posMod = (peerWidth + PEER_SPACE) * -1;
				movePeer(parentPeerId, posMod);
				delete PEER_MAP[parentPeerId];

			} else {

				for(var i = hostIndex + 1; i < rhKeys.length; i++) {
					var resourceHost = graph.getCell(PEER_MAP[parentPeerId].rh[rhKeys[i]]);
					var resourceHostPosition = resourceHost.get('position');
					resourceHost.set('position', {x: resourceHostPosition.x, y: (resourceHostPosition.y - emptyPlace)});
					changePositionToEmbeds(resourceHost.get('embeds'), false, (emptyPlace * -1));
				}

				var lastResourceInPeer = graph.getCell(PEER_MAP[parentPeerId].rh[
					Object.keys(PEER_MAP[parentPeerId].rh)[
						Object.keys(PEER_MAP[parentPeerId].rh).length - 1
					]
				]);
				lastResourceInPeer.set('addClass', 'b-resource-host_last');
				lastResourceInPeer.set('removeClass', '');
			}

			this.$box.remove();
		}
	});	

	function initJointJs() {

		var paper = new joint.dia.Paper({
			el: $('#js-environment-creation'),
			width: '100%',
			height: '100%',
			model: graph,
			gridSize: 1
		});

	paper.on('cell:pointerdown', function(cellView) {
		cellView.prevPos = cellView.model.get('position');
	});

	paper.on('cell:pointerup', function(cellView) {

		if( cellView.model.get( 'containerName' ) === undefined )
			return;

		var models = graph.findModelsFromPoint({x : cellView._dx, y: cellView._dy});

		for( var i = 0; i < models.length; i++ ) {
			if (models[i].get('hostId') !== undefined) {
				if( cellView.model.get("parent") != models[i].id )
				{
					var rh = cellView.model.get('rh');
					var prevParent = graph.getCell(cellView.model.get("parent"));
					prevParent.unembed(cellView.model);
					delete prevParent.get('grid')[rh.x][rh.y];

					var rPos = models[i].get('position');
					var gPos = placeRhSimple( models[i] );

					cellView.model.set('rh', { model: models[i].id, x: gPos.x, y: gPos.y});
					var x = (rPos.x + gPos.x * GRID_SIZE + GRID_SPACING) + 23;
					var y = (rPos.y + gPos.y * GRID_SIZE + GRID_SPACING) + 49;					
					cellView.model.set('position', { x: x, y: y });

					models[i].embed(cellView.model);


					return;
				}
			}
		}

		cellView.model.set('position', cellView.prevPos);
	});

		$('.js-scrollbar').perfectScrollbar();
	}

	vm.buildStep = 'confirm';
	function buildEnvironmentByJoint() {

		vm.newEnvID = [];		

		var allElements = graph.getCells();
		vm.env2Build = {};
		vm.containers2Build = [];
		vm.buildStep = 'confirm';

		for(var i = 0; i < allElements.length; i++) {
			if(allElements[i].get('type') == 'tm.devElement') {

				var currentElement = allElements[i];
				var container2Build = {
					"size": currentElement.get('quotaSize'),
					"templateName": currentElement.get('templateName'),
					"name": currentElement.get('containerName'),
					"peerId": currentElement.get('parentPeerId'),
					"hostId": currentElement.get('parentHostId'),
					"position": currentElement.get('position'),
					"sshGroupId" : 0,
					"hostsGroupId" : 0
				};

				if (vm.env2Build[currentElement.get('templateName')] === undefined) {
					vm.env2Build[currentElement.get('templateName')] = {};
					vm.env2Build[currentElement.get('templateName')].count = 1;
					vm.env2Build[currentElement.get('templateName')]
						.sizes = {};
					vm.env2Build[currentElement.get('templateName')]
						.sizes[currentElement.get('quotaSize')] = 1;
				} else {
					vm.env2Build[currentElement.get('templateName')].count++;
					if(vm.env2Build[currentElement.get('templateName')].sizes[currentElement.get('quotaSize')] === undefined) {
						vm.env2Build[currentElement.get('templateName')]
							.sizes[currentElement.get('quotaSize')] = 1;
					} else {
						vm.env2Build[currentElement.get('templateName')]
							.sizes[currentElement.get('quotaSize')]++;
					}
				}

				vm.containers2Build.push(container2Build);

			}
		}

		console.log(vm.containers2Build);
		ngDialog.open({
			template: 'subutai-app/environment/partials/popups/environment-build-info-advanced.html',
			scope: $scope,
			className: 'b-build-environment-info'
		});
	}

	function editEnvironment(environment) {
		clearWorkspace();
		console.log(environment);
		for(var i = 0; i < environment.containers.length; i++) {
			var container = environment.containers[i];
			var resourceHostItemId = addResource2Build(container.hostId, container.peerId, i);
			var resourceHost = graph.getCell(resourceHostItemId);
			var img = 'assets/templates/' + container.templateName + '.jpg';
			if(!imageExists(img)) {
				img = 'assets/templates/no-image.jpg';
			}
			addContainerToHost(resourceHost, container.templateName, img, container.type);
		}
	}

	function clearWorkspace() {
		vm.cubeGrowth = 0;
		PEER_MAP = [];
		graph.resetCells();
		$('.b-resource-host').remove();
	}

	function addSettingsToTemplate(settings) {
		vm.currentTemplate.set('quotaSize', settings.quotaSize);
		vm.currentTemplate.attr('rect.b-magnet/fill', vm.colors[settings.quotaSize]);
		vm.currentTemplate.set('containerName', settings.containerName);
		ngDialog.closeAll();
	}
}

function imageExists(image_url){

    var http = new XMLHttpRequest();

    http.open('HEAD', image_url, false);
    http.send();

    return http.status != 404;

}

function placeRhSimple( model ) {
	var array = model.attributes.grid;
	var sizeObj = model.attributes.gridSize;
	var children = model.get('children');
	var size = sizeObj.size;

	for( var j = 0; j < size; j++ ) {
		for( var i = 0; i < size; i++ ) {
			if( array[i] === undefined ) {
				array[i] = new Array();
				array[i][j] = 1;

				return {x:i, y:j};
			}

			if( array[i][j] !== 1 ) {
				array[i][j] = 1;
				return {x:i, y:j};
			}
		}
	}

	array[size] = new Array();
	array[size][0] = 1;
	size++;
	sizeObj.size = size;

	return { x : size - 1, y : 0 };
}

function growResourceHost(model) {
	var currentModelSize = model.get('size');
	model.resize(currentModelSize.width + 60, currentModelSize.height + 60);
	model.attr('g.element-tool-remove/ref-x', model.get('size').width + 1);
}

function movePeer(peerId, posMod, counter) {
	if(counter == undefined || counter == null) counter = false;
	var peerKeys = Object.keys(PEER_MAP);
	var peerIndex = peerKeys.indexOf(peerId);				
	for(var i = peerIndex + 1; i < peerKeys.length; i++) {

		var x = posMod;
		if(counter && counter > 0) {
			x = counter * posMod;
			counter++;
		}

		for(var key in PEER_MAP[peerKeys[i]].rh) {
			var resourceHost = graph.getCell(PEER_MAP[peerKeys[i]].rh[key]);
			var resourceHostPosition = resourceHost.get('position');
			resourceHost.set('position', {x: resourceHostPosition.x + x, y: resourceHostPosition.y});
			changePositionToEmbeds(resourceHost.get('embeds'), x, false);
		}
	}
}

function changePositionToEmbeds(embeds, posX, posY) {
	if(embeds) {
		for(var i = 0; i < embeds.length; i++) {
			var container = graph.getCell(embeds[i]);
			var containerPosition = container.get('position');

			var x = containerPosition.x;
			if(posX) {
				x = x + posX;
			}

			var y = containerPosition.y;
			if(posY) {
				y = y + posY;
			}

			container.set('position', {x: x, y: y});
		}
	}
}

function checkResourceHost(model) {
	var sizeObj = model.attributes.gridSize;
	var children = model.get('children');
	var size = sizeObj.size;

	if(children >= (size * size)) {

		var peerId = model.get('peerId');
		var hostId = model.get('hostId');
		var rhKeys = Object.keys(PEER_MAP[peerId].rh);
		var hostIndex = rhKeys.indexOf(model.get('hostId'));

		var counter = 0;
		for(var key in PEER_MAP[peerId].rh) {
			var resourceHost = graph.getCell(PEER_MAP[peerId].rh[key]);
			growResourceHost(resourceHost);

			if(key !== hostId) {
				resourceHost.attributes.gridSize.size = size + 1;
			}

			if(counter > 0) {
				var resourceHostPosition = resourceHost.get('position');
				var yPosMod = (counter * 60);
				resourceHost.set('position', {x: resourceHostPosition.x, y: (resourceHostPosition.y + yPosMod)});
				changePositionToEmbeds(resourceHost.get('embeds'), false, yPosMod);
			}

			counter++;
		}
		movePeer(peerId, 60, 1);
	}
	
}

function imageExists(image_url){
	var http = new XMLHttpRequest();

	http.open('HEAD', image_url, false);
	http.send();

	return http.status != 404;
}

function startDrag( event ) {

	var containerImage = $(event.target).parent().find('img');

	var ghostImage = document.createElement("span");
	ghostImage.className = 'b-cloud-item b-hidden-object';
	ghostImage.id = 'js-ghost-image';
	ghostImage.style.backgroundImage = "url('" + containerImage.attr('src') + "')";
	document.body.appendChild(ghostImage);
	event.dataTransfer.setDragImage(document.createElement("span"), 0, 0);

	event.dataTransfer.setData( "template", $(event.target).data('template') );
	event.dataTransfer.setData( "img", containerImage.attr('src') );
}

function dragOver( event ) {
	var ghostImage = document.getElementById('js-ghost-image');	
	ghostImage.style.left = event.pageX + 'px';
	ghostImage.style.top = event.pageY + 'px';
	event.preventDefault();
}

function endtDrag( event ) {
	document.getElementById('js-ghost-image').remove();
}

var containerCounter = 1;
function drop(event) {
	event.preventDefault();

	var template = event.dataTransfer.getData("template");
	var img = event.dataTransfer.getData("img");

	var posX = event.offsetX;
	var posY = event.offsetY;

	var models = graph.findModelsFromPoint({x :posX, y: posY});

	for( var i = 0; i < models.length; i++ ) {
		if( models[i].attributes.hostId !== undefined )	{
			addContainerToHost(models[i], template, img);
		}
	}
}

function addContainerToHost(model, template, img, size) {
	if(size == undefined || size == null) size = 'SMALL';
	checkResourceHost(model);
	var rPos = model.attributes.position;
	var gPos = placeRhSimple( model );

	var x = (rPos.x + gPos.x * GRID_SIZE + GRID_SPACING) + 23;
	var y = (rPos.y + gPos.y * GRID_SIZE + GRID_SPACING) + 49;

	var devElement = new joint.shapes.tm.devElement({
		position: { x: x, y: y },
		templateName: template,
		parentPeerId: model.get('peerId'),
		parentHostId: model.get('hostId'),
		quotaSize: 'SMALL',
		containerName: 'Container ' + (containerCounter++).toString(),
		attrs: {
			image: { 'xlink:href': img },
			'rect.b-magnet': {fill: quotaColors[size]},
			title: {text: template}
		},
		rh: {
			model: model.id,
			x: gPos.x,
			y: gPos.y
		}
	});

	graph.addCell(devElement);
	model.embed(devElement);
	model.set('children', model.get('children') + 1);
}

