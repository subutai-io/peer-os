'use strict';

angular.module('subutai.environment.adv-controller', [])
    .controller('AdvancedEnvironmentCtrl', AdvancedEnvironmentCtrl);

AdvancedEnvironmentCtrl.$inject = ['$scope', '$rootScope', 'environmentService', 'trackerSrv', 'SweetAlert', 'ngDialog', 'identitySrv', 'templateSrv'];

var graph = new joint.dia.Graph;
var paper;
var GRID_SIZE = 60;
var GRID_SPACING = 5;

var PEER_MAP = {};
var PEER_WIDTH = 155;
var PEER_SPACE = 30;

var RH_WIDTH = 100;
var RH_SPACE = 10;

function AdvancedEnvironmentCtrl($scope, $rootScope, environmentService, trackerSrv, SweetAlert, ngDialog, identitySrv, templateSrv) {

    var vm = this;

//	checkCDNToken(templateSrv, $rootScope)

    vm.buildEnvironment = buildEnvironment;
    vm.buildEditedEnvironment = buildEditedEnvironment;
    vm.logMessages = [];
    var containerSettingMenu = $('.js-dropen-menu');
    var currentTemplate = {};
    $scope.identity = angular.identity;

    vm.activeCloudTab = 'peers';
    vm.templatesType = 'all';

    vm.peerIds = [];
    vm.numChangedContainers = 0;
    vm.resourceHosts = [];
    vm.currentResourceHosts = [];
    vm.advancedEnv = {};
    vm.nodeStatus = 'Add to';
    vm.nodeList = [];
    vm.colors = quotaColors;
    vm.templates = {};
    vm.templatesList = [];


    vm.excludedContainers = [];
    vm.cubeGrowth = 1;
    vm.environment2BuildName = '';
    vm.currentPeer = false;
    vm.currentPeerIndex = false;
    vm.buildCompleted = false;
    vm.selectedPlugin = false;
    vm.editingEnv = false;
    vm.isEditing = false;
    vm.downloadProgress = '';
    vm.rhId = '';

    // functions

    vm.initJointJs = initJointJs;
    vm.buildEnvironmentByJoint = buildEnvironmentByJoint;
    vm.editEnvironment = editEnvironment;
    vm.clearWorkspace = clearWorkspace;
    vm.addSettingsToTemplate = addSettingsToTemplate;
    vm.getFilteredTemplates = getFilteredTemplates;

    vm.showResources = showResources;
    vm.addResource2Build = addResource2Build;
    vm.closePopup = closePopup;

    //plugins actions
    vm.selectPlugin = selectPlugin;
    vm.setTemplatesByPlugin = setTemplatesByPlugin;
    vm.loadOwnTemplates = loadOwnTemplates;

    vm.getCdnToken = getCdnToken;

    function getCdnToken(){
        return localStorage.getItem('cdnToken');
    }

    function loadOwnTemplates() {
        templateSrv.getOwnTemplates()
            .then(function (data) {
                vm.templates['own'] = data;
                getFilteredTemplates();
            });
    }

    function loadTemplates(callback) {
        templateSrv.getTemplates()
            .then(function (data) {
                vm.templates['all'] = data;
                getFilteredTemplates(callback);
            });
    }

    loadTemplates();
//    loadOwnTemplates();

    $rootScope.$on('cdnTokenSet', function (event, data) {
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

    function getPeers() {
        $('.js-peer-load-screen').show();
        environmentService.getPeers().success(function (data) {
            vm.peerIds = data;
            $('.js-peer-load-screen').hide();
        }).error(function (error) {
            $('.js-peer-load-screen').hide();
            VARS_MODAL_ERROR(SweetAlert, 'Error on getting peers: ' + error);
        });
    }

    getPeers();

    clearWorkspace();

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

    var timeoutId;

    function getLogById(id, envId) {

        clearTimeout(timeoutId);

        trackerSrv.getDownloadProgress(envId)
            .success(function (data) {

                if (data.length > 0) {

                    data.sort();

                    var output = '<table class="b-main-table b-main-table_progrss">';
                    var checker = false;
                    for (var i = 0; i < data.length; i++) {

                        output += [
                            '<tr>',
                            '<th colspan="2">',
                            'Peer ' + shortenIdName(data[i].peerId, 3),
                            '</th>',
                            '</tr>',
                        ].join('');
                        for (var j = 0; j < data[i].templatesDownloadProgress.length; j++) {
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
                                if (p.templatesDownloadProgress[tpl] != 100) {
                                    checker = true;
                                }
                            }
                        }
                    }
                    output += '</table>';


                    if (checker == true) {
                        $('.js-download-progress').html(output);
                    }
                    else {
                        $('.js-download-progress').html('');
                    }
                }
                else
                    $('.js-download-progress').html('');
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

                  vm.logMessages =result;

                  if(data.state != 'RUNNING') {
                      vm.buildCompleted = true;
                      vm.isEditing = false;

                      $('.js-download-progress').html('');
                      $rootScope.notificationsUpdate = 'getLogByIdAdv';
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

        environmentService.startEnvironmentAdvancedBuild(vm.environment2BuildName, JSON.stringify(vm.containers2Build))
            .success(function (data) {
                vm.newEnvID = data;

                currentLog = {
                    "time": '',
                    "status": 'in-progress',
                    "classes": ['fa-spinner', 'fa-pulse'],
                    "log": 'Environment creation has been started'
                };
                vm.logMessages.push(currentLog);

                getLogById(data.trackerId, data.environmentId);
                initScrollbar();

                $rootScope.notificationsUpdate = 'startEnvironmentAdvancedBuild';
            }).error(function (error) {
            ngDialog.closeAll();
            if (error && error.ERROR === undefined) {
                VARS_MODAL_ERROR(SweetAlert, 'Error: ' + error);
            } else {
                VARS_MODAL_ERROR(SweetAlert, 'Error: ' + error.ERROR);
            }
            $rootScope.notificationsUpdate = 'startEnvironmentAdvancedBuildError';
        });
        vm.environment2BuildName = '';
    }

    function buildEditedEnvironment() {
        vm.buildStep = 'showLogs';

        vm.buildCompleted = false;
        vm.logMessages = [];
        var currentLog = {
            "time": '',
            "status": 'in-progress',
            "classes": ['fa-spinner', 'fa-pulse'],
            "log": 'Environment modification has been started'
        };
        vm.logMessages.push(currentLog);

        var quotaContainers = [];

        for (var key in vm.editingEnv.changingContainers) {
            quotaContainers.push({"key": key, "value": vm.editingEnv.changingContainers[key]});
        }

        var containers = {
            "topology": vm.containers2Build,
            "removedContainers": vm.containers2Remove,
            "environmentId": vm.editingEnv.id,
            "changingContainers": quotaContainers
        };

        environmentService.modifyEnvironment(containers, 'advanced')
            .success(function (data) {
                vm.newEnvID = data;

                getLogById(data, containers.environmentId);
                initScrollbar();
                $scope.$emit('reloadEnvironmentsList');

                $rootScope.notificationsUpdate = 'modifyEnvironmentAdv';
            }).error(function (error) {
            ngDialog.closeAll();
            if (error && error.ERROR === undefined) {
                VARS_MODAL_ERROR(SweetAlert, 'Error: ' + error);
            } else {
                VARS_MODAL_ERROR(SweetAlert, 'Error: ' + error.ERROR);
            }
            $scope.$emit('reloadEnvironmentsList');

            $rootScope.notificationsUpdate = 'modifyEnvironmentAdvError';
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

        var isManagement = false;
        var peerName = '';
        for (var i in vm.peerIds) {
            var peer = vm.peerIds[i];
            if (peer.id == peerId) {
                peerName = peer.name;
                for (var j in peer.resourceHosts) {
                    var rh = peer.resourceHosts[j];
                    if (rh.id == currentResource && rh.isManagement) {
                        isManagement = true;
                    }
                }
            }
        }

        var posX = calculatePeerPos();

        if (PEER_MAP[peerId] !== undefined) {
            if (PEER_MAP[peerId].rh[currentResource] !== undefined) {
                return PEER_MAP[peerId].rh[currentResource];
            }
        } else {
            PEER_MAP[peerId] = {rh: [], position: posX};
        }

        if (Object.keys(PEER_MAP[peerId].rh).length > 0) {
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
            position: {x: PEER_MAP[peerId].position * ( PEER_WIDTH + PEER_SPACE ), y: posY},
            size: {width: 155, height: 173},
            peerId: peerId,
            hostId: currentResource,
            children: 0,
            grid: [],
            gridSize: {size: 2},
            'resourceHostName': 'RH ' + (currentResource.substr(vm.rhId.length - 3)) + (isManagement ? " [MH]" : ""),
            'peerName': /*'Peer ' + */peerName/*peerId.substr(peerId.length - 3)*/,
            'addClass': 'b-resource-host_last'
        });

        graph.addCell(resourceHost);
        PEER_MAP[peerId].rh[currentResource] = resourceHost.id;

        return resourceHost.id;
    }

    function calculateResourceHostPos(peerId) {
        if (PEER_MAP[peerId]) {
            var posY = 30;
            for (var key in PEER_MAP[peerId].rh) {
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
                pos.push(PEER_MAP[key].position);
            }
        }

        if (pos.length == 0)
            return 0;
        if (pos.length == 1)
            return 1;

        pos.sort();

        for (var i = 1; i < pos.length; i++) {
            if (pos[i - 1] + 1 !== pos[i]) {
                return pos[i - 1] + 1;
            }
        }

        return pos[pos.length - 1] + 1;
    }

    //custom shapes
    joint.shapes.tm = {};

    //simple creation templates
    joint.shapes.tm.toolElement = joint.shapes.basic.Generic.extend({

        toolMarkup: [
            '<g class="element-tools">',
            '<g class="element-tool-remove">',
            '<circle fill="#F8FBFD" r="8" stroke="#dcdcdc"/>',
            '<polygon transform="scale(1.2) translate(-5, -5)" fill="#292F6C" points="8.4,2.4 7.6,1.6 5,4.3 2.4,1.6 1.6,2.4 4.3,5 1.6,7.6 2.4,8.4 5,5.7 7.6,8.4 8.4,7.6 5.7,5 "/>',
            '<title>Remove</title>',
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
                    width: 10,
                    height: 10,
                    rx: 50,
                    ry: 50,
                    transform: 'translate(16,28)'
                },
                'g.b-container-plus-icon': {'ref-x': 17.5, 'ref-y': 30, ref: 'rect', transform: 'scale(0.8)'}
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
            size: {width: 40, height: 40},
            attrs: {
                title: {text: 'Static Tooltip'},
                'rect.b-border': {
                    fill: '#fff',
                    stroke: '#dcdcdc',
                    'stroke-width': 1,
                    width: 40,
                    height: 40,
                    rx: 50,
                    ry: 50
                },
                'rect.b-magnet': {
                    fill: '#04346E',
                    width: 10,
                    height: 10,
                    rx: 50,
                    ry: 50,
                    transform: 'translate(16,28)'
                },
                'g.b-container-plus-icon': {'ref-x': 17.5, 'ref-y': 30, ref: 'rect', transform: 'scale(0.8)'},
                image: {'ref-x': 9, 'ref-y': 9, ref: 'rect', width: 25, height: 25},
            }
        }, joint.shapes.tm.toolElement.prototype.defaults)
    });

    //custom view
    joint.shapes.tm.ToolElementView = joint.dia.ElementView.extend({
        initialize: function () {
            joint.dia.ElementView.prototype.initialize.apply(this, arguments);
            this.isMovingContainer = false;
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
                        vm.excludedContainers.push(this.model);
                    }

                    var rh = this.model.attributes.rh;
                    var resourceHost = graph.getCell(rh.model);
                    resourceHost.set('children', resourceHost.get('children') - 1);

                    delete graph.getCell(rh.model).attributes.grid[rh.x][rh.y];
                    this.model.remove();
                    filterPluginsList();
                    return;
                    break;
                case 'element-call-menu':
                case 'b-container-plus-icon':
                    currentTemplate = this.model;
                    $('#js-container-name').val(currentTemplate.get('containerName')).trigger('change');
                    if (currentTemplate.get('edited') == true) {
                        $('#js-container-name').prop('disabled', true);
                    } else {
                        $('#js-container-name').prop('disabled', false);
                    }
                    $('#js-container-size').val(currentTemplate.get('quotaSize')).trigger('change');

                    if (currentTemplate.get('quotaSize') == 'CUSTOM') {
                        $('#js-quotasize-custom-cpu').val(currentTemplate.get('cpuQuota')).trigger('change');
                        $('#js-quotasize-custom-ram').val(currentTemplate.get('ramQuota')).trigger('change');
                        $('#js-quotasize-custom-disk').val(currentTemplate.get('diskQuota')).trigger('change');
                    }

                    containerSettingMenu.find('.header').html('Settings for <b>' + this.model.get('templateName') + '</b> container');
                    var elementPos = this.model.get('position');
                    containerSettingMenu.css({
                        'left': (elementPos.x - 2) + 'px',
                        'top': (elementPos.y + 45) + 'px',
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
        },
        pointermove: function (evt, x, y) {
            if(this.model.get('edited') == true) {
                // disable moving for existing environment's containers
                return false;
            } else {
                this.isMovingContainer = true;
                joint.dia.ElementView.prototype.pointermove.apply(this, arguments);
            }
        },
        pointerup: function (evt, x, y) {
            joint.dia.ElementView.prototype.pointerup.apply(this, arguments);

            if ( this.isMovingContainer == true ) {
                var models = graph.findModelsFromPoint({x: evt.offsetX, y: evt.offsetY});

                if ( models != undefined && models.length > 0 ) {
                    this.model.set('parentHostId', models[0].get('hostId'));
                    this.model.set('parentPeerId', models[0].get('peerId'));
                    this.model.get('rh')['model'] = models[0].id;
                }
            }

            this.isMovingContainer = false;
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
                rect: {stroke: 'none', 'fill-opacity': 0, 'pointer-events': 'none'},
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
            '<div class="b-resource-host__inner">',
            '<div class="b-recouce-host-title"></div>',
            '<div class="b-resource-host__containers">',
            '<div class="b-resource-host-containers">',
            '</div>',
            '</div>',
            '</div>',
            '</div>'
        ].join(''),

        initialize: function () {
            _.bindAll(this, 'updateBox');
            joint.dia.ElementView.prototype.initialize.apply(this, arguments);

            this.$box = $(_.template(this.template)());
            this.$box.find('.js-delete-peer').on('click', _.bind(this.model.remove, this.model));
            this.model.on('change', this.updateBox, this);
            // Remove the box when the model gets removed from the graph.
            this.model.on('remove', this.removeBox, this);

            this.updateBox();
        },
        render: function () {
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
        pointermove: function (evt, x, y) {
            return false;
        },
        updateBox: function () {
            // Set the position and dimension of the box so that it covers the JointJS element.
            var bbox = this.model.getBBox();

            if (this.model.get('addClass')) {
                this.$box.addClass(this.model.get('addClass'));
            }

            if (this.model.get('removeClass')) {
                this.$box.removeClass(this.model.get('removeClass'));
            }

            this.$box.find('.b-peer-title').text(this.model.get('peerName'));
            this.$box.find('.b-recouce-host-title').text(this.model.get('resourceHostName'));
            this.$box.css({
                width: bbox.width,
                height: bbox.height,
                left: bbox.x,
                top: bbox.y,
                transform: 'rotate(' + (this.model.get('angle') || 0) + 'deg)'
            });
        },
        removeBox: function (evt) {
            var parentPeerId = this.model.get('peerId');
            var rhKeys = Object.keys(PEER_MAP[parentPeerId].rh);
            var hostIndex = rhKeys.indexOf(this.model.get('hostId'));

            var emptyPlace = this.model.get('size').height - 8;
            delete PEER_MAP[parentPeerId].rh[this.model.get('hostId')];

            if (Object.keys(PEER_MAP[parentPeerId].rh).length == 0) {

                var peerWidth = this.model.get('size').width;
                var posMod = (peerWidth + PEER_SPACE) * -1;
                movePeer(parentPeerId, posMod);
                delete PEER_MAP[parentPeerId];

            } else {

                for (var i = hostIndex + 1; i < rhKeys.length; i++) {
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

        setTimeout(function () {
            document.getElementById('js-environment-creation').addEventListener('destroyEnvironment', function (e) {
                if (vm.editingEnv && vm.editingEnv.id == e.detail) {
                    clearWorkspace();
                    vm.editingEnv = false;
                }
            }, false);
        }, 1000);

        paper = new joint.dia.Paper({
            el: $('#js-environment-creation'),
            width: '2000px',
            height: '2000px',
            model: graph,
            gridSize: 1
        });

        paper.on('cell:pointerdown', function (cellView) {
            cellView.prevPos = cellView.model.get('position');
        });

        paper.on('cell:pointerup', function (cellView) {

            if (cellView.model.get('containerName') === undefined)
                return;

            var models = graph.findModelsFromPoint({x: cellView._dx, y: cellView._dy});

            for (var i = 0; i < models.length; i++) {
                if (models[i].get('hostId') !== undefined) {
                    if (cellView.model.get("parent") != models[i].id) {

                        var rh = cellView.model.get('rh');
                        var prevParent = graph.getCell(cellView.model.get("parent"));
                        prevParent.unembed(cellView.model);
                        prevParent.set('children', prevParent.get('children') - 1);
                        delete prevParent.get('grid')[rh.x][rh.y];

                        checkResourceHost(models[i]);
                        var rPos = models[i].get('position');
                        var gPos = placeRhSimple(models[i]);

                        cellView.model.set('rh', {model: models[i].id, x: gPos.x, y: gPos.y});
                        var x = (rPos.x + gPos.x * GRID_SIZE + GRID_SPACING) + 23;
                        var y = (rPos.y + gPos.y * GRID_SIZE + GRID_SPACING) + 49;
                        cellView.model.set('position', {x: x, y: y});

                        models[i].embed(cellView.model);
                        models[i].set('children', models[i].get('children') + 1);

                        return;
                    }
                }
            }

            cellView.model.set('position', cellView.prevPos);
        });

        initScrollbar();
    }

    vm.buildStep = 'confirm';
    function buildEnvironmentByJoint() {

        vm.buildCompleted = false;
        vm.newEnvID = [];
        vm.buildStep = 'confirm';
        clearTimeout(timeoutId);
        vm.logMessages = [];
        vm.env2Build = {};
        vm.containers2Build = [];
        vm.env2Remove = {};
        vm.containers2Remove = [];

        var allElements = graph.getCells();
        var addContainers = getContainers2Build(allElements, true);
        vm.env2Build = addContainers.containersObj;
        vm.containers2Build = addContainers.containersList;

        if (vm.editingEnv) {
            var removeContainers = getContainers2Build(vm.excludedContainers, false, true);
            vm.env2Remove = removeContainers.containersObj;
            vm.containers2Remove = removeContainers.containersList;

            vm.numChangedContainers = 0;

            for (var key in vm.editingEnv.changingContainers) {
                vm.numChangedContainers++;
            }
        }

        ngDialog.open({
            template: 'subutai-app/environment/partials/popups/environment-build-info-advanced.html',
            scope: $scope,
            className: 'b-build-environment-info',
            preCloseCallback: function (value) {
                if (vm.buildCompleted) {
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
        vm.isEditing = true;
        vm.editingEnv = environment;
        vm.editingEnv.changingContainers = [];
        vm.environment2BuildName = environment.name;
        vm.excludedContainers = [];
        vm.currentPeerIndex = 0;
        for (var i = 0; i < environment.containers.length; i++) {
            var container = environment.containers[i];

            if (container.containerName.match(/(\d+)(?!.*\d)/g) != null) {
                if (containerCounter < parseInt(container.containerName.match(/(\d+)(?!.*\d)/g)) + 1) {
                    containerCounter = parseInt(container.containerName.match(/(\d+)(?!.*\d)/g)) + 1;
                }
            }

            if (container.hostname.match(/(\d+)(?!.*\d)/g) != null) {
                if (containerCounter < parseInt(container.hostname.match(/(\d+)(?!.*\d)/g)) + 1) {
                    containerCounter = parseInt(container.hostname.match(/(\d+)(?!.*\d)/g)) + 1;
                }
            }

            vm.rhId = container.rhId;
            var resourceHostItemId = addResource2Build(container.rhId, container.peerId, i);
            var resourceHost = graph.getCell(resourceHostItemId);
            vm.currentPeerIndex++;
            var img = 'assets/templates/' + container.templateName + '.jpg';
            if (!imageExists(img)) {
                img = 'assets/templates/no-image.jpg';
            }
            addContainerToHost(resourceHost, container.templateName, img, container.type, container.quota, container.id, container.hostname, container.templateId);
        }
        filterPluginsList();
    }

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
        var addedContainers = getContainers2Build(allElements);

        if (addedContainers.containersList.length > 0) {
            vm.filteredPlugins = {};
            for (var i = 0; i < addedContainers.containersList.length; i++) {

                var currentTemplate = addedContainers.containersList[i].templateName;

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

    $scope.filterPluginsList = filterPluginsList;

    function selectPlugin(plugin) {

        if (vm.selectedPlugin) {
            vm.selectedPlugin.selected = false;
        }

        vm.selectedPlugin = plugin;
        vm.selectedPlugin.selected = true;
    }

    function setTemplatesByPlugin() {

        if (vm.selectedPlugin.requirement !== undefined) {

            var firstPeer;
            for (firstPeer in vm.peerIds) break;
            var resourceHostItemId = addResource2Build(vm.peerIds[firstPeer].resourceHosts[0].id, firstPeer, 0);
            var resourceHost = graph.getCell(resourceHostItemId);

            for (var template in vm.selectedPlugin.requirement) {
                for (var i = 0; i < vm.selectedPlugin.requirement[template]; i++) {

                    var allElements = graph.getCells();
                    var addedContainers = getContainers2Build(allElements, true);

                    if (addedContainers.containersList.length > 0) {

                        var alreadyONWorckspace = false;
                        var templatesCounter = 0;
                        for (var j = 0; j < addedContainers.containersList.length; j++) {
                            var currentTemplate = addedContainers.containersList[j].templateName;
                            if (currentTemplate == template.toLowerCase()) {
                                alreadyONWorckspace = true;
                                templatesCounter++;
                            }
                        }
                        if (!alreadyONWorckspace || templatesCounter == i) {
                            var img = 'assets/templates/' + template + '.jpg';
                            if (!imageExists(img)) {
                                img = 'assets/templates/no-image.jpg';
                            }
                            environmentService.getVerifiedTemplate(template.toLowerCase()).success(function (verifiedTemplate) {
                                addContainerToHost(resourceHost, template, img, vm.selectedPlugin.size, null, null, null, verifiedTemplate.id);
                            });
                        }

                    } else {
                        var img = 'assets/templates/' + template + '.jpg';
                        if (!imageExists(img)) {
                            img = 'assets/templates/no-image.jpg';
                        }
                        environmentService.getVerifiedTemplate(template.toLowerCase()).success(function (verifiedTemplate) {
                            addContainerToHost(resourceHost, template, img, vm.selectedPlugin.size, null, null, null, verifiedTemplate.id);
                        });
                    }

                }
            }
        }
        $('.b-template-settings').stop().slideUp(100);

    }

    function getContainers2Build(models, onlyNew, getRemoved) {
        if (onlyNew == undefined || onlyNew == null) onlyNew = false;
        if (getRemoved == undefined || getRemoved == null) getRemoved = false;
        var result = {"containersObj": {}, "containersList": []};

        for (var i = 0; i < models.length; i++) {
            if (models[i].get('type') == 'tm.devElement') {

                var currentElement = models[i];
                if (onlyNew && currentElement.get('containerId')) {
                    continue;
                }

                if (getRemoved) {
                    result.containersList.push(currentElement.get('containerId'));
                } else {
                    var isCustom = currentElement.get('quotaSize') == 'CUSTOM';

                    var container2Build = {

                        "quota": isCustom ? {
                                "containerSize": currentElement.get('quotaSize'),
                                "cpuQuota": currentElement.get("cpuQuota"),
                                "ramQuota": currentElement.get("ramQuota") + 'MiB',
                                "diskQuota": currentElement.get("diskQuota") + 'GiB',
                            } : {"containerSize": currentElement.get('quotaSize')},

                        "templateName": currentElement.get('templateName'),
                        "templateId": currentElement.get('templateId'),
                        "name": currentElement.get('containerName'),
                        "peerId": currentElement.get('parentPeerId'),
                        "hostId": currentElement.get('parentHostId'),
                        "position": currentElement.get('position'),
                        "sshGroupId": 0,
                        "hostsGroupId": 0
                    };
                    result.containersList.push(container2Build);
                }

                if (result.containersObj[currentElement.get('templateName')] === undefined) {
                    result.containersObj[currentElement.get('templateName')] = {};
                    result.containersObj[currentElement.get('templateName')].count = 1;
                    result.containersObj[currentElement.get('templateName')].sizes = {};
                    result.containersObj[currentElement.get('templateName')].sizes[currentElement.get('quotaSize')] = 1;
                    result.containersObj[currentElement.get('templateName')].name = getTemplateNameById(currentElement.get('templateName'), vm.templatesList);
                    result.containersObj[currentElement.get('templateName')].id = currentElement.get('templateId');
                } else {
                    result.containersObj[currentElement.get('templateName')].count++;
                    if (result.containersObj[currentElement.get('templateName')].sizes[currentElement.get('quotaSize')] === undefined) {
                        result.containersObj[currentElement.get('templateName')]
                            .sizes[currentElement.get('quotaSize')] = 1;
                    } else {
                        result.containersObj[currentElement.get('templateName')]
                            .sizes[currentElement.get('quotaSize')]++;
                    }
                }
            }
        }
        return result;
    }

    function clearWorkspace() {
        vm.cubeGrowth = 0;
        PEER_MAP = {};
        vm.environment2BuildName = '';

        vm.env2Build = {};
        vm.containers2Build = [];

        vm.env2Remove = {};
        vm.containers2Remove = [];

        vm.isEditing = false;
        vm.editingEnv = false;
        graph.resetCells();
        $('.b-resource-host').remove();
        filterPluginsList();
    }

    function addSettingsToTemplate(templateSettings, sizeDetails) {

        if (! /^[a-zA-Z][a-zA-Z0-9\-]{0,49}$/.test(templateSettings.containerName)){
                    SweetAlert.swal("Invalid hostname", "The container hostname must start with a letter and have as interior characters only letters, digits, and hyphen", "error");

                    return;
        }

        var isCustom = templateSettings.quotaSize == 'CUSTOM';

        currentTemplate.set('quotaSize', templateSettings.quotaSize);

        if (isCustom) {
            currentTemplate.set('cpuQuota', templateSettings.cpuQuota);
            currentTemplate.set('ramQuota', templateSettings.ramQuota);
            currentTemplate.set('diskQuota', templateSettings.diskQuota);
        }

        currentTemplate.attr('title/text', templateSettings.containerName + ' (' + currentTemplate.get('templateName') +  ') ' + templateSettings.quotaSize);
        currentTemplate.attr('rect.b-magnet/fill', vm.colors[templateSettings.quotaSize]);
        currentTemplate.set('containerName', templateSettings.containerName);

        containerSettingMenu.hide();

        if (vm.isEditing) {
            var id = currentTemplate.attributes.containerId;

            var res = $.grep(vm.editingEnv.containers, function (e, i) {
                return e.id == id;
            });

            if (res[0]) {
                res = res[0];

                if (res.type == templateSettings.quotaSize && vm.editingEnv.changingContainers[id] && !isCustom) {
                    delete vm.editingEnv.changingContainers[id];
                }

                if (res.type != templateSettings.quotaSize || isCustom) {

                    vm.editingEnv.changingContainers[id] = {"containerSize": templateSettings.quotaSize};

                    if (isCustom) {
                        vm.editingEnv.changingContainers[id].cpuQuota = templateSettings.cpuQuota;
                        vm.editingEnv.changingContainers[id].ramQuota = templateSettings.ramQuota;
                        vm.editingEnv.changingContainers[id].diskQuota = templateSettings.diskQuota;
                    }
                }
            }
        }
    }

}

function placeRhSimple(model) {
    var array = model.attributes.grid;
    var sizeObj = model.attributes.gridSize;
    var size = sizeObj.size;

    for (var j = 0; j < size; j++) {
        for (var i = 0; i < size; i++) {
            if (array[i] === undefined) {
                array[i] = new Array();
                array[i][j] = 1;

                return {x: i, y: j};
            }

            if (array[i][j] !== 1) {
                array[i][j] = 1;
                return {x: i, y: j};
            }
        }
    }

    array[size] = new Array();
    array[size][0] = 1;
    size++;
    sizeObj.size = size;

    return {x: size - 1, y: 0};
}

function growResourceHost(model) {
    var currentModelSize = model.get('size');
    model.resize(currentModelSize.width + 60, currentModelSize.height + 60);
    model.attr('g.element-tool-remove/ref-x', model.get('size').width + 1);
}

function movePeer(peerId, posMod, counter) {
    if (counter == undefined || counter == null) counter = false;
    var peerKeys = Object.keys(PEER_MAP);
    var peerIndex = peerKeys.indexOf(peerId);
    for (var i = peerIndex + 1; i < peerKeys.length; i++) {

        var x = posMod;
        if (counter && counter > 0) {
            x = counter * posMod;
        }

        PEER_MAP[peerKeys[i]].position--;

        for (var key in PEER_MAP[peerKeys[i]].rh) {
            var resourceHost = graph.getCell(PEER_MAP[peerKeys[i]].rh[key]);
            var resourceHostPosition = resourceHost.get('position');
            resourceHost.set('position', {x: resourceHostPosition.x + x, y: resourceHostPosition.y});
            changePositionToEmbeds(resourceHost.get('embeds'), x, false);
        }
    }
}

function changePositionToEmbeds(embeds, posX, posY) {
    if (embeds) {
        for (var i = 0; i < embeds.length; i++) {
            var container = graph.getCell(embeds[i]);
            var containerPosition = container.get('position');

            var x = containerPosition.x;
            if (posX) {
                x = x + posX;
            }

            var y = containerPosition.y;
            if (posY) {
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

    if (children >= (size * size)) {

        var peerId = model.get('peerId');
        var hostId = model.get('hostId');
        var rhKeys = Object.keys(PEER_MAP[peerId].rh);
        var hostIndex = rhKeys.indexOf(model.get('hostId'));

        var counter = 0;
        for (var key in PEER_MAP[peerId].rh) {
            var resourceHost = graph.getCell(PEER_MAP[peerId].rh[key]);
            growResourceHost(resourceHost);

            if (key !== hostId) {
                resourceHost.attributes.gridSize.size = size + 1;
            }

            if (counter > 0) {
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

var lastUsedRhIdx = -1;
function clickIt(event){

    var rhs = [];
    for(var peerId in PEER_MAP){
        for(var rhId in PEER_MAP[peerId].rh){
            rhs.push(graph.getCell(PEER_MAP[peerId].rh[rhId]));
        }
    }

    if(rhs.length == 0) return;

    var theRH;
    if(rhs.length > lastUsedRhIdx + 1){
        lastUsedRhIdx++;
    }else{
        lastUsedRhIdx = 0;
    }
    theRH = rhs[lastUsedRhIdx];

    var containerImage = $(event.target).parent().find('img');
    var template = $(event.target).data('template');
    var templateId = $(event.target).data('template-id');
    var img = containerImage.attr('src');

    addContainerToHost(theRH, template, img, null, null, null, null, templateId);
}

function startDrag(event) {

    var containerImage = $(event.target).parent().find('img');

    var ghostImage = document.createElement("span");
    ghostImage.className = 'b-cloud-item b-hidden-object';
    ghostImage.id = 'js-ghost-image';
    ghostImage.style.backgroundImage = "url('" + containerImage.attr('src') + "')";

    document.body.appendChild(ghostImage);
    if (navigator.userAgent.indexOf('Safari') != -1 && navigator.userAgent.indexOf('Chrome') == -1) {
        event.dataTransfer.setDragImage(ghostImage, 0, 0);
    } else {
        event.dataTransfer.setDragImage(document.createElement("span"), 0, 0);
    }

    event.dataTransfer.setData("template", $(event.target).data('template'));
    event.dataTransfer.setData("templateId", $(event.target).data('template-id'));
    event.dataTransfer.setData("img", containerImage.attr('src'));
}

function dragOver(event) {
    var ghostImage = document.getElementById('js-ghost-image');
    ghostImage.style.left = event.pageX + 'px';
    ghostImage.style.top = event.pageY + 'px';
    event.preventDefault();
}

function endtDrag(event) {
    event.preventDefault();
    document.getElementById('js-ghost-image').remove();
}

var containerCounter = 1;
function drop(event) {

    var template = event.dataTransfer.getData("template");
    var templateId = event.dataTransfer.getData("templateId");
    var img = event.dataTransfer.getData("img");

    var posX = event.offsetX;
    var posY = event.offsetY;

    var models = graph.findModelsFromPoint({x: posX, y: posY});

    for (var i = 0; i < models.length; i++) {
        if (models[i].attributes.hostId !== undefined) {
            addContainerToHost(models[i], template.toLowerCase(), img, null, null, null, null, templateId);
        }
    }
}

function addContainerToHost(model, template, img, size, quota, containerId, name, templateId) {
    if (size === undefined || size === null) {
        size = 'TINY';
        if (template == 'appscale') {
            size = 'HUGE';
        }
    }
    var edited = false;
    if (containerId == undefined || containerId == null) {
        containerId = false;
    } else {
        edited = true;
    }
    checkResourceHost(model);
    var rPos = model.attributes.position;
    var gPos = placeRhSimple(model);

    var x = (rPos.x + gPos.x * GRID_SIZE + GRID_SPACING) + 23;
    var y = (rPos.y + gPos.y * GRID_SIZE + GRID_SPACING) + 49;

    if (templateId == undefined || templateId == null) {
        var templateId = getTemplateIdByName(template, templatesList);
    }

    var containerName = '';
    if (name == undefined || name == null) {
        containerName = 'Container' + (containerCounter++).toString();
    } else {
        containerName = name;
    }

    var devElement = new joint.shapes.tm.devElement({
        position: {x: x, y: y},
        edited: edited,
        templateName: template,
        templateId: templateId,
        parentPeerId: model.get('peerId'),
        parentHostId: model.get('hostId'),
        quotaSize: size,
        cpuQuota: quota === null ? 33 : quota.cpu,
        ramQuota: quota === null ? 2048 : quota.ram,
        diskQuota: quota === null ? 10 : quota.disk,

        containerId: containerId,
        containerName: containerName,
        attrs: {
            image: {'xlink:href': img},
            'rect.b-magnet': {fill: quotaColors[size]},
            title: {text: containerName + " ('" + template + "') " + size}
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

    angular.element(document.getElementById('js-environment-creation')).scope().filterPluginsList();
}

function shortenIdName(name, factor) {
    return name.substring(0, factor) + '..' + name.substring(name.length - factor, name.length);
}
