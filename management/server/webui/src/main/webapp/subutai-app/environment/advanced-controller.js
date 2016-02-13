'use strict';

angular.module('subutai.environment.adv-controller', [])
	.controller('AdvancedEnvironmentCtrl', AdvancedEnvironmentCtrl);

AdvancedEnvironmentCtrl.$inject = ['$scope', 'environmentService', 'peerRegistrationService', 'SweetAlert', 'ngDialog'];

var graph = new joint.dia.Graph;

function AdvancedEnvironmentCtrl($scope, environmentService, peerRegistrationService, SweetAlert, ngDialog) {

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
	vm.advancedEnv.currentNode = getDefaultValues();
	vm.nodeStatus = 'Add to';
	vm.nodeList = [];
	vm.colors = quotaColors;
	vm.templates = [];

	vm.templateGrid = [];
	vm.cubeGrowth = 1;
	vm.environment2BuildName = 'Environment name';
	vm.currentPeer = false;

	// functions

	vm.addNewNode = addNewNode;
	vm.removeNodeGroup = removeNodeGroup;
	vm.setNodeData = setNodeData;
	vm.setupAdvancedEnvironment = setupAdvancedEnvironment;
	vm.initJointJs = initJointJs;
	vm.buildEnvironmentByJoint = buildEnvironmentByJoint;
	vm.clearWorkspace = clearWorkspace;
	vm.sendToPending = sendToPending;
	vm.addSettingsToTemplate = addSettingsToTemplate;

	vm.showResources = showResources;
	vm.addResource2Build = addResource2Build;
	vm.addContainer = addContainer;

	environmentService.getTemplates()
		.success(function (data) {
			vm.templates = data;
		})
		.error(function (data) {
			VARS_MODAL_ERROR( SweetAlert, 'Error on getting templates ' + data );
		});

	environmentService.getStrategies().success(function (data) {
		vm.strategies = data;
	});

	environmentService.getDomainStrategies().success(function (data) {
		vm.domainStrategies = data;
	});

	environmentService.getPeers().success(function (data) {
		vm.peerIds = data;
	});

	peerRegistrationService.getResourceHosts().success(function (data) {
		vm.resourceHosts = data;
	});

	function buildEnvironment() {
		ngDialog.closeAll();
		SweetAlert.swal(
			{
				title : 'Environment',
				text : 'Creation has been started',
				timer: VARS_TOOLTIP_TIMEOUT,
				showConfirmButton: false
			}
		);		
		environmentService.startEnvironmentBuild (vm.newEnvID[0], encodeURIComponent(vm.newEnvID[1])).success(function (data) {
			SweetAlert.swal("Success!", "Your environment has been built successfully.", "success");
			loadEnvironments();
		}).error(function (data) {
			SweetAlert.swal("ERROR!", "Environment build error. Error: " + data.ERROR, "error");
		});
	}

	function addNewNode() {
		if(vm.nodeStatus == 'Add to') {
			var tempNode = vm.advancedEnv.currentNode;

			if(tempNode === undefined) return;
			if(tempNode.name === undefined || tempNode.name.length < 1) return;
			if(tempNode.numberOfContainers === undefined || tempNode.numberOfContainers < 1) return;
			if(tempNode.sshGroupId === undefined) return;
			if(tempNode.hostsGroupId === undefined) return;

			if( jQuery.grep( vm.nodeList, function( i ) {
					return tempNode.name == i.name;
				}).length != 0
			) return;

			vm.nodeList.push(tempNode);
		} else {
			vm.nodeStatus = 'Add to';
		}


		vm.advancedEnv.currentNode = angular.copy( vm.advancedEnv.currentNode );
		vm.advancedEnv.currentNode.name = "";
	}

	function setNodeData(key) {
		vm.nodeStatus = 'Update in';
		vm.advancedEnv.currentNode = vm.nodeList[key];
	}

	function removeNodeGroup(key)
	{
		vm.nodeList.splice(key, 1);
	}

	function getDefaultValues() {
		var defaultVal = {
			'templateName': 'master',
			'numberOfContainers': 2,
			'sshGroupId': 0,
			'hostsGroupId': 0,
			'type': 'TINY'
		};
		return defaultVal;
	}

	function setupAdvancedEnvironment() {
		if(vm.advancedEnv.name === undefined) return;
		if(vm.nodeList === undefined || vm.nodeList.length == 0) return;

		var finalEnvironment = vm.advancedEnv;
		finalEnvironment.nodeGroups = vm.nodeList;
		if(finalEnvironment.currentNod !== undefined) {
			finalEnvironment.nodeGroups.push(finalEnvironment.currentNode);
		}
		delete finalEnvironment.currentNode;

		var cloneContainers = {};

		for( var i = 0; i < finalEnvironment.nodeGroups.length; i++ )
		{
			var node = finalEnvironment.nodeGroups[i];
			for( var j = 0; j < node.numberOfContainers; j++ )
			{
				if( j < 0 ) break;

				if( cloneContainers[node.peerId] === undefined )
				{
					cloneContainers[node.peerId] = [];
				}

				cloneContainers[node.peerId].push(node);
			}
		}

		LOADING_SCREEN();
		ngDialog.closeAll();
		vm.activeTab = 'pending';
		environmentService.setupAdvancedEnvironment(finalEnvironment.name, cloneContainers)
			.success(function(data){
				console.log(data);
				loadEnvironments();
				LOADING_SCREEN('none');
			}).error(function(error){
				console.log(error);
				LOADING_SCREEN('none');
			});

		vm.nodeList = [];
		vm.advancedEnv = {};
		vm.advancedEnv.currentNode = getDefaultValues();
	}

	function showResources(peerId, resourcesId) {
		vm.currentResourceHosts = resourcesId;
		vm.currentPeer = peerId;
		$('.b-cloud-add-tools').animate({'left': '-200px'}, 300);
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
					this.model.remove();
					delete vm.templateGrid[Math.floor( x / GRID_CELL_SIZE )][ Math.floor( y / GRID_CELL_SIZE )];
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
						template: 'subutai-app/environment/partials/popups/templateSettings.html',
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
		defaults: joint.util.deepSupplement({
			type: 'resourceHostHtml.Element',
			attrs: {
				rect: { stroke: 'none', 'fill-opacity': 0 }
			}
		}, joint.shapes.basic.Rect.prototype.defaults)
	});

	// Resource host html.
	// -------------------------------------------------------------------------

	joint.shapes.resourceHostHtml.ElementView = joint.dia.ElementView.extend({

		template: [
			'<div class="b-resource-host">',
				'<div class="b-peer-title"></div>',
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
			this.$box.find('.delete').on('click', _.bind(this.model.remove, this.model));
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
		updateBox: function() {
			// Set the position and dimension of the box so that it covers the JointJS element.
			var bbox = this.model.getBBox();

			this.$box.find('.b-peer-title').text(this.model.get('peerName'));
			this.$box.find('.b-recouce-host-title').text(this.model.get('resourceHostName'));
			this.$box.css({ width: bbox.width, height: bbox.height, left: bbox.x, top: bbox.y, transform: 'rotate(' + (this.model.get('angle') || 0) + 'deg)' });
		},
		removeBox: function(evt) {
			this.$box.remove();
		}
	});	

	var containerCounter = 1;
	function addContainer(template, $event) {
		//var pos = findEmptyCubePostion();
		var parentPos = mainResourceHost.get('position');
		var pos = findEmptyPostionInResource();
		var img = $($event.currentTarget).find('img');

		var devElement = new joint.shapes.tm.devElement({
			//position: { x: (GRID_CELL_SIZE * pos.x) + 20, y: (GRID_CELL_SIZE * pos.y) + 20 },
			position: { x: parentPos.x + pos.x, y: parentPos.y + pos.y },
			templateName: template,
			quotaSize: 'SMALL',
			containerName: vm.environment2BuildName + ' ' + (containerCounter++).toString(),
			attrs: {
				image: { 'xlink:href': img.attr('src') },
				'rect.b-magnet': {fill: vm.colors['SMALL']},
				title: {text: template}
			}
		});

		graph.addCell(devElement);
		mainResourceHost.embed(devElement);
		vm.containersInResource.push(devElement);
		return false;
	}

	//add resource host
	var mainResourceHost;
	function addResource2Build(currentResource) {
		vm.containersInResource = [];
		mainResourceHost = new joint.shapes.resourceHostHtml.Element({
			position: { x: 80, y: 80 },
			size: { width: 155, height: 185 },
			peerId: vm.currentPeer,
			grid: [],
			gridSize: { size: 2 },
			hostId: currentResource.id,
			'resourceHostName': 'RH1',
			'peerName': 'Peer 1'
		});
		graph.addCell(mainResourceHost);
		return false;
	}

	vm.containersInResource = [];
	vm.containersInResourceMax = 4;
	vm.containersInRow = 2;
	function findEmptyPostionInResource() {
		if(vm.containersInResource.length == 0) {
			return {x: 28, y: 54};
		} else {
			var row = 0;
			for(var i = 0; i < vm.containersInResource.length; i++) {
				if((i + 1) % vm.containersInRow == 0) {
					row++;
				}
			}

			var x = 88;
			if(vm.containersInResource.length % vm.containersInRow == 0) {
				x = 28;
			}

			if(vm.containersInResource.length == vm.containersInResourceMax) {
				if(vm.containersInResourceMax == 4) {
					vm.containersInResourceMax = vm.containersInResourceMax + 5;
				} else {
					vm.containersInResourceMax = vm.containersInResourceMax + (vm.containersInResourceMax + 2);
				}

				//vm.containersInResourceMax = vm.containersInResourceMax + 2;

				var currentSize = mainResourceHost.get('size');
				vm.containersInRow++;
				mainResourceHost.resize(currentSize.width + 60, currentSize.height + 60);
			}
			return {x: x, y: (row * 60) + 54};
		}
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

	function initJointJs() {

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

		/*paper.on('cell:pointerup',
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
		);*/

		$('.js-scrollbar').perfectScrollbar();

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
		var allElements = graph.getCells();
		vm.env2Build = {};
		vm.containers2Build = [];
		vm.buildStep = 'confirm';
		console.log(allElements);
		for(var i = 0; i < allElements.length; i++) {
			var currentElement = allElements[i];
			if(currentElement.get('type') == 'tm.devElement') {
				var currentTemplateName = allElements[i].get('templateName');
				var container2Build = {
					"size": currentElement.get('quotaSize'),
					"templateName": currentTemplateName,
					"name": currentElement.get('containerName'),
					"position": currentElement.get('position')
				};
				if(vm.env2Build[currentTemplateName] === undefined) {
					vm.env2Build[currentTemplateName] = 1;
				} else {
					vm.env2Build[currentTemplateName]++;
				}
				vm.containers2Build.push(container2Build);
			}
		}
		console.log(vm.containers2Build);
		ngDialog.open({
			template: 'subutai-app/environment/partials/popups/environment-build-info.html',
			scope: $scope,
			className: 'b-build-environment-info'
		});
	}

	function clearWorkspace() {
		vm.cubeGrowth = 0;
		vm.templateGrid = [];
		graph.resetCells();
		$('.b-resource-host').remove();
	}

	function sendToPending() {
		LOADING_SCREEN();
		environmentService.startEnvironmentAutoBuild(vm.environment2BuildName, JSON.stringify(vm.containers2Build))
			.success(function(data){
				console.log(data);
				vm.newEnvID = data;
				vm.buildStep = 'pgpKey';
				LOADING_SCREEN('none');
			}).error(function(error){
				if(error.ERROR === undefined) {
					VARS_MODAL_ERROR( SweetAlert, 'Error: ' + error );
				} else {
					VARS_MODAL_ERROR( SweetAlert, 'Error: ' + error.ERROR );
				}
				LOADING_SCREEN('none');
			});
	}

	function addSettingsToTemplate(settings) {
		vm.currentTemplate.set('quotaSize', settings.quotaSize);
		vm.currentTemplate.attr('rect.b-magnet/fill', vm.colors[settings.quotaSize]);
		vm.currentTemplate.set('containerName', settings.containerName);
		ngDialog.closeAll();
	}
}

function startDrag( event ) {
	event.dataTransfer.setData( "template", $(event.target).data('template') );
}

function drop(event) {
	event.preventDefault();
	var data = event.dataTransfer.getData("template");
	console.log(event);

	var posX = event.offsetX;
	var posY = event.offsetY;

	var models = graph.findModelsFromPoint({x :posX, y: posY});

	for( var i = 0; i < models.length; i++ )
	{
		if( models[i].attributes.hostId !== undefined )
		{
			var rPos = models[i].attributes.position;
			console.log(models);
			var gPos = placeRhSimple( models[i].attributes.grid, models[i].attributes.gridSize );

			var devElement = new joint.shapes.tm.devElement({
				position: { x: rPos.x + gPos.x * GRID_SIZE + GRID_SPACING, y: rPos.y + gPos.y * GRID_SIZE + GRID_SPACING },
				templateName: data,
				quotaSize: 'SMALL', // var
				containerName: "EPTA NAME", // var
				attrs: {
					image: { 'xlink:href': 'assets/elements/avatar-empty.svg'}, // var
					'rect.b-magnet': {fill: "#CC00FF"}, // var
					title: {text: data}
				},
				rh: {
					model: models[i].id,
					x: gPos.x,
					y: gPos.y
				}
			});

			graph.addCell(devElement);
			//models[i].embed(devElement);
		}
	}
}

function dragOver( event ) {
	event.preventDefault();
}

var GRID_SIZE = 50;
var GRID_SPACING = 5;

var PEER_MAP = {};
var PEER_WIDTH = 120;
var PEER_SPACE = 20;

var RH_WIDTH = 100;
var RH_SPACE = 10;
function placeRhSimple( array, sizeObj ) {
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

	size++;
	sizeObj.size = size;
	array[size] = new Array();
	array[size][0] = 1;

	return { x : size - 1, y : 0 };
}

