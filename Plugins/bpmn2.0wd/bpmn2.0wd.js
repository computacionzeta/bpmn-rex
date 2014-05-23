/**
 * Copyright (c) 2014
 * Luis Stroppi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
   @namespace Oryx name space for plugins
   @name ORYX.Plugins
*/
if(!ORYX.Plugins)
	ORYX.Plugins = new Object();
	
/**
 * This plugin provides methodes to serialize and deserialize a BPMN 2.0 diagram.
 * 
 * @class ORYX.Plugins.Bpmn2_0WD
 * @extends ORYX.Plugins.AbstractPlugin
 * @param {Object} facade
 * 		The facade of the Editor
 */
ORYX.Plugins.BPMN2_0WD = {
	wdStencilSetExtensionNamespace: "http://b3mn.org/stencilset/bpmn2.0#",
	rsStencilSetExtensionNamespace: "http://b3mn.org/stencilset/bpmnrex#",
	bpmnSerializationHandlerUrl: ORYX.CONFIG.ROOT_PATH + "bpmn2_0wdtoyawl",
	bpmnRSSerializationHandlerUrl: ORYX.CONFIG.ROOT_PATH + "bpmn2_0rstoyawl",
	rpimExtensionUrl : ORYX.CONFIG.ROOT_PATH + "rpimExtension",
	rsmExtensionUrl : ORYX.CONFIG.ROOT_PATH + "rsmExtension", 
	
	importedRSM : "",
	importedRPIM : "",

	resources		    : "[]", 
	resourceParameters	: "[]", 
	
	roleDefinitions     : "[]", 
	taskPrivilegeDefinitions : "[]",
	resolutionConstraintDefinitions : "[]",
	
	resourceClassifierDefinitions : "[]",
	humanResourceDefinitions : "[]",
	resourcePrivilegeDefinitions : "[]",
	relationshipDefinitions : "[]",
	classificationDefinitions : "[]",
	valuedParameterDefinitions : "[]",
	

	construct: function(facade) {
	
		this.facade = facade;
		
		this.active = false;
        this.raisedEventIds = [];
		
		//this.facade.registerOnEvent(ORYX.CONFIG.EVENT_PROPWINDOW_PROP_CHANGED, this.handlePropertyChanged.bind(this));
	
		/* BPMN 2.0 XML */
		
		this.facade.offer({
			'name'				: ORYX.I18N.Bpmn2_0WD.createAnnotation,
			'functionality'		: this.createAnnotation.bind(this),
			'group'				: 'Export',
            'dropDownGroupIcon' : ORYX.PATH + "images/export2.png",
			'icon' 				: ORYX.PATH + "images/source.png",
			'description'		: ORYX.I18N.Bpmn2_0WD.createAnnotationDesc,
			'index'				: 0,
			'minShape'			: 0,
			'maxShape'			: 0,
			'isEnabled': 		this._isStencilSetExtensionLoaded.bind(this)
		});
		
		this.facade.offer({
			'name': ORYX.I18N.BPMN2YAWLMapper.name,
			'functionality': this.bpmn2WDToYAWL.bind(this),
			'group': ORYX.I18N.BPMN2YAWLMapper.group,
			'icon': ORYX.PATH + 'images/door.png',
			'description': ORYX.I18N.BPMN2YAWLMapper.desc,
            dropDownGroupIcon: ORYX.PATH + "images/export2.png",
			'index': 1,
			'minShape': 0,
			'maxShape': 0,
			'isEnabled': 		this._isStencilSetExtensionLoaded.bind(this)

		});
		
		this.facade.offer({
			'name': 'Convert to YAWL RS',
			'functionality': this.bpmn2RSToYAWL.bind(this),
			'group': ORYX.I18N.BPMN2YAWLMapper.group,
			'icon': ORYX.PATH + 'images/door.png',
			'description': ORYX.I18N.BPMN2YAWLMapper.desc,
            dropDownGroupIcon: ORYX.PATH + "images/export2.png",
			'index': 2,
			'minShape': 0,
			'maxShape': 0,
			'isEnabled': 		this._isStencilSetExtensionLoaded.bind(this)

		});
		
		this.facade.offer({
			'name': 'Import RPIM',
			'functionality': this.showRPIM.bind(this),
			'group': 'Export',
			'icon': ORYX.PATH + 'images/door.png',
			'description': 'Shows the list of RPIM in the repository',
            dropDownGroupIcon: ORYX.PATH + "images/export2.png",
			'index': 3,
			'minShape': 0,
			'maxShape': 0,
			'isEnabled': 		this._isStencilSetExtensionLoaded.bind(this)

		});
		
		this.facade.offer({
			'name': 'Import RSM',
			'functionality': this.showRSM.bind(this),
			'group': 'Export',
			'icon': ORYX.PATH + 'images/door.png',
			'description': 'Shows the list of RSM in the repository',
            dropDownGroupIcon: ORYX.PATH + "images/export2.png",
			'index': 4,
			'minShape': 0,
			'maxShape': 0,
			'isEnabled': 		this._isStencilSetExtensionLoaded.bind(this)

		});
		
		this.facade.offer({
			'name': 'Tag as BPLM',
			'functionality': this.tagBPLM.bind(this),
			'group': 'Export',
			'icon': ORYX.PATH + 'images/door.png',
			'description': 'Tags the model as a BPLM',
            dropDownGroupIcon: ORYX.PATH + "images/export2.png",
			'index': 5,
			'minShape': 0,
			'maxShape': 0,
			'isEnabled': 		this._isStencilSetExtensionLoaded.bind(this)

		});
		
		this.facade.offer({
			'name': 'Tag as PI-EPM',
			'functionality': this.tagPIEPM.bind(this),
			'group': 'Export',
			'icon': ORYX.PATH + 'images/door.png',
			'description': 'Tags the model as a PI-EPM',
            dropDownGroupIcon: ORYX.PATH + "images/export2.png",
			'index': 6,
			'minShape': 0,
			'maxShape': 0,
			'isEnabled': 		this._isStencilSetExtensionLoaded.bind(this)

		});
		
		this.facade.offer({
			'name': 'Tag as PS-EPM',
			'functionality': this.tagPSEPM.bind(this),
			'group': 'Export',
			'icon': ORYX.PATH + 'images/door.png',
			'description': 'Tags the model as a PS-EPM',
            dropDownGroupIcon: ORYX.PATH + "images/export2.png",
			'index': 7,
			'minShape': 0,
			'maxShape': 0,
			'isEnabled': 		this._isStencilSetExtensionLoaded.bind(this)

		});
		
		this.facade.offer({
			'name': 'Initialize PI-EPM',
			'functionality': this.selectBPLM.bind(this),
			'group': 'Export',
			'icon': ORYX.PATH + 'images/door.png',
			'description': 'Initializes a PI-EPM with base on a BPLM in the repository',
            dropDownGroupIcon: ORYX.PATH + "images/export2.png",
			'index': 8,
			'minShape': 0,
			'maxShape': 0,
			'isEnabled': 		this._isStencilSetExtensionLoaded.bind(this)

		});
		
		this.facade.offer({
			'name': 'Initialize PS-EPM',
			'functionality': this.selectPIEPM.bind(this),
			'group': 'Export',
			'icon': ORYX.PATH + 'images/door.png',
			'description': 'Initializes a PI-EPM with base on a BPLM in the repository',
            dropDownGroupIcon: ORYX.PATH + "images/export2.png",
			'index': 9,
			'minShape': 0,
			'maxShape': 0,
			'isEnabled': 		this._isStencilSetExtensionLoaded.bind(this)

		});
		
		this.facade.offer({
			'name': 'Initialize PS-RSM',
			'functionality': this.selectPIRSM.bind(this),
			'group': 'Export',
			'icon': ORYX.PATH + 'images/door.png',
			'description': 'Initializes a PI-EPM with base on a BPLM in the repository',
            dropDownGroupIcon: ORYX.PATH + "images/export2.png",
			'index': 10,
			'minShape': 0,
			'maxShape': 0,
			'isEnabled': 		this._isStencilSetExtensionLoaded.bind(this)

		});
		
		if(this.facade.getCanvas().properties["oryx-rpim"] != "" && this.facade.getCanvas().properties["oryx-rpim"] != undefined){
			this.importRPIM(this.facade.getCanvas().properties["oryx-rpim"]);		
		}
		
		if(this.facade.getCanvas().properties["oryx-rsm"] != "" && this.facade.getCanvas().properties["oryx-rsm"] != undefined){
			this.importRSM(this.facade.getCanvas().properties["oryx-rsm"]);
		}
	},
	
	tagBPLM: function(option) {
		this.tag("BPLM");
	},
	
	tagPIEPM: function(option) {
		this.tag("PI-EPM");
	},
	
	tagPSEPM: function(option) {
		this.tag("PS-EPM");
	},
	
	tag: function(tag) {
		var id = this.getModelId();
		new Ajax.Request('/backend/poem/model/'+id+'/tags', {
			method: 'POST',
			asynchronous: false,
			parameters: {
				tag_name: tag
			},
			onSuccess: function(request){
				resp = request.responseText.evalJSON();
				returnValue = resp;
			}.bind(this)
    	});
	},
	
	/*handlePropertyChanged : function(event) {
		if (event["key"] == "oryx-privileges") {
			var shape = event.elements[0];
			
			var privileges = shape.getLabels().find(
				function(label) { return label.id == (shape.id + "privilegesText") }
			);
			
			var arrPrivileges = JSON.parse(event.value).items;
			var strPrivileges = "Privileges: ";
			var coma = "";
			for(var i=0; i<arrPrivileges.length; i++){
					strPrivileges += coma + arrPrivileges[i].privilege;
					if((i+1)%3 == 0){
						coma = ",\n "; 
					}else{
						coma = ", "; 
					}
			}

			privileges.text(strPrivileges);
			
		}
	},*/
	
	setExtensionValues : function(items){
	
		if(items.resources != undefined){
			this.resources = JSON.stringify(items.resources);
		}
		
		if(items.parameterOptions != undefined){
			this.resourceParameters = JSON.stringify(items.parameterOptions);
		}
	
		if(items.roleOptions != undefined){
			this.roleDefinitions = JSON.stringify(items.roleOptions);
		}
		
		if(items.taskPrivilegeOptions != undefined){
			this.taskPrivilegeDefinitions = JSON.stringify(items.taskPrivilegeOptions);
		}
		
		if(items.resolutionConstraintOptions != undefined){
			this.resolutionConstraintDefinitions  = JSON.stringify(items.resolutionConstraintOptions);
		}
		
		if(items.resourceClassifierOptions != undefined){
			this.resourceClassifierDefinitions  = JSON.stringify(items.resourceClassifierOptions);
		}
		
		if(items.humanResourceOptions != undefined){
			this.humanResourceDefinitions  = JSON.stringify(items.humanResourceOptions);
		}
		
		if(items.resourcePrivilegeOptions != undefined){
			this.resourcePrivilegeDefinitions  = JSON.stringify(items.resourcePrivilegeOptions);
		}
		
		if(items.relationshipOptions != undefined){
			this.relationshipDefinitions  = JSON.stringify(items.relationshipOptions);
		}
		
		if(items.classificationOptions != undefined){
			this.classificationDefinitions  = JSON.stringify(items.classificationOptions);
		}
		
		if(items.valuedParameterOptions != undefined){
			this.valuedParameterDefinitions  = JSON.stringify(items.valuedParameterOptions);
		}
	},
	
	loadExtension : function(){
		var strExtension = "";
		
		var stencil = this.facade.getStencilSets()[this.wdStencilSetExtensionNamespace];
		if(stencil == undefined){
			stencil = this.facade.getStencilSets()[this.rsStencilSetExtensionNamespace];
			strExtension = this.getRSExtension();
			stencil.addExtensionDirectly(strExtension);		
		}else{
			strExtension = this.getWDExtension();
			stencil.addExtensionDirectly(strExtension);		
		}
		
	
	},
	
	getWDExtension : function(){
		var extension = '{\"title\":\"RPIM Extension\",\"namespace\":\"http://www.cidisi.org/bpmn/extensions/rpim#\",\"description\":\"Extend the model with implementation definition elements.\",\"extends\":\"http://b3mn.org/stencilset/bpmn2.0#\",\"propertyPackages\" : [],\"stencils\":[], \"properties\" : [{\"roles\" : [\"Task\"], \"properties\" : [{\"id\":\"revokedPrivileges\",\"type\":\"Complex\",\"title\":\"RevokedPrivileges\",\"value\":\"\",\"description\":\"\",\"readonly\":false,\"optional\":true,\"popular\":true,\"complexItems\": [{\"id\":\"taskPrivilege\",\"name\":\"taskPrivilege\",\"type\":\"Choice\",\"value\":\"\",\"width\":100,\"optional\":true,##taskPrivileges##}]},{\"id\":\"resources\",\"type\":\"Complex\",\"title\":\"Resources\",\"value\":\"\",\"description\":\"\",\"readonly\":false,\"optional\":true,\"popular\":true,\"complexItems\": [{\"id\":\"name\",\"name\":\"Name\",\"type\":\"String\",\"value\":\"\",\"width\":100,\"optional\":true},{\"id\":\"documentation\",\"name\":\"Documentation\",\"type\":\"String\",\"value\":\"\",\"width\":100,\"optional\":true},{\"id\":\"resourceRef\",\"name\":\"ResourceRef\",\"type\":\"Choice\",\"value\":\"\",\"width\":100,\"optional\":true,##resources##},{\"id\":\"resourceParameterBinding\",\"name\":\"RresourceParameterBinding\",\"type\":\"Choice\",\"value\":\"\",\"width\":100,\"optional\":true,##resourceParameters##},{\"id\":\"resourceAssignmentExpression\",\"name\":\"ResourceAssignmentExpression\",\"type\":\"String\",\"value\":\"\",\"width\":100,\"optional\":true},{\"id\":\"resolutionConstraint\",\"name\":\"ResolutionConstraint\",\"type\":\"Choice\",\"value\":\"\",\"width\":100,\"optional\":true,##resolutionConstraints##},{\"id\":\"trigger\",\"name\":\"Trigger\",\"type\":\"String\",\"value\":\"\",\"width\":100,\"optional\":true},{\"id\":\"escalation\",\"name\":\"Escalation\",\"type\":\"String\",\"value\":\"\",\"width\":100,\"optional\":true},{\"id\":\"definition\",\"name\":\"Definition\",\"type\":\"Choice\",\"value\":\"\",\"width\":100,\"optional\":true,##definition_items##}]}]}]}';
		
		extension = extension.replace("##definition_items##", '"items":'+this.roleDefinitions);
		extension = extension.replace("##taskPrivileges##", '"items":'+this.taskPrivilegeDefinitions);
		extension = extension.replace("##resources##", '"items":'+this.resources);
		extension = extension.replace("##resourceParameters##", '"items":'+this.resourceParameters);
		extension = extension.replace("##resolutionConstraints##", '"items":'+this.resolutionConstraintDefinitions);
		
		extension = this.cleanExtension(extension);
		console.log(extension);
		return extension;
	},
	
	getRSExtension : function(){
		var extension = '{\"title\":\"BPMN 2.0 Work Distribution Extension\",\"namespace\":\"http://www.cidisi.org/bpmn/extensions/resourcestructure#\",\"description\":\"Extension to BPMN-REX with RPIM elements.\",\"extends\":\"http://b3mn.org/stencilset/bpmnrex#\",\"propertyPackages\" : [],\"stencils\":[], \"properties\" : [{\"roles\" : [\"resourceParameter\"],\"properties\":[{\"id\":\"definition\",\"type\":\"Choice\",\"title\":\"Definition\",\"value\":\"\",\"description\":\"RPIM definition\",\"readonly\":false,\"optional\":true,\"popular\":true,##resourceParameterDefinitions##}]},{\"roles\" : [\"resourceClassifier\"],properties : [{\"id\":\"definition\",\"name\":\"Definition\",\"type\":\"Choice\",\"value\":\"\",popular: true,\"width\":80,\"optional\":true,##resourceClassifierDefinitions##},{\"id\":\"privileges\",\"type\":\"Complex\",\"title\":\"Privileges\",\"value\":\"\",\"description\":\"Resource Privileges\",\"readonly\":false,\"optional\":true,\"popular\":true,\"complexItems\": [{\"id\":\"privilege\",\"name\":\"Privilege\",\"type\":\"Choice\",\"value\":\"\",\"width\":100,\"optional\":true,##resourceClassifierPrivilegeDefinitions##}]}]},{\"roles\" : [\"humanResource\"],properties : [{\"id\":\"definition\",\"name\":\"Definition\",\"type\":\"Choice\",\"value\":\"\",popular: true,\"width\":80,\"optional\":true,##humanResourceDefinitions##},{\"id\":\"privileges\",\"type\":\"Complex\",\"title\":\"Privileges\",\"value\":\"\",\"description\":\"Resource Privileges\",\"readonly\":false,\"optional\":true,\"popular\":true,\"complexItems\": [{\"id\":\"privilege\",\"name\":\"Privilege\",\"type\":\"Choice\",\"value\":\"\",\"width\":100,\"optional\":true,##humanResourcePrivilegeDefinitions##}]}]},{\"roles\" : [\"resourceReference\"],properties : [{\"id\":\"definition\",\"title\":\"Definition\",\"description\":\"Platform specific definition of the reference.\",\"popular\":true,\"type\":\"Choice\",\"value\":\"\",\"optional\":true,##relationshipDefinitions##}]},{\"roles\" : [\"resourceClassification\"],properties : [{\"id\":\"definition\",\"title\":\"Definition\",\"description\":\"Platform specific definition of the classification.\",\"popular\":true,\"type\":\"Choice\",\"value\":\"\",\"optional\":true,##classificationDefinitions##}]},{\"roles\" : [\"Diagram\"],properties : [{\"id\":\"rpim\",\"title\":\"RPIM\",\"description\":\"Implementation Definition Model\",\"type\":\"String\",\"value\":\"\",\"readonly\":false,\"optional\":true,\"popular\":true}]}]}';
		
		extension = extension.replace("##resourceClassifierDefinitions##", '"items":'+this.resourceClassifierDefinitions);
		extension = extension.replace("##resourceClassifierPrivilegeDefinitions##", '"items":'+this.resourcePrivilegeDefinitions);
		
		extension = extension.replace("##humanResourceDefinitions##", '"items":'+this.humanResourceDefinitions);
		extension = extension.replace("##humanResourcePrivilegeDefinitions##", '"items":'+this.resourcePrivilegeDefinitions);
		
		extension = extension.replace("##resourceParameterDefinitions##", '"items":'+this.valuedParameterDefinitions);
		
		
		
		extension = extension.replace("##relationshipDefinitions##", '"items":'+this.relationshipDefinitions);
		extension = extension.replace("##classificationDefinitions##", '"items":'+this.classificationDefinitions);
		
		extension = this.cleanExtension(extension);
		
		console.log(extension);
		
		return extension;
	},
	
	cleanExtension : function(strExtension){
		while(strExtension.indexOf('"[') > 0){
			strExtension = strExtension.replace('"[', '[');
		}
		
		while(strExtension.indexOf(']"') > 0){
			strExtension = strExtension.replace(']"', ']');
		}
		
		while(strExtension.indexOf('"[') > 0){
			strExtension = strExtension.replace('"[', '[');
		}
		
		while(strExtension.indexOf(' \\"') > 0){
			strExtension = strExtension.replace(' \\"', '"');
		}
		
		while(strExtension.indexOf('\\"') > 0){
			strExtension = strExtension.replace('\\"', '"');
		}
		
		while(strExtension.indexOf(': "') > 0){
			strExtension = strExtension.replace(': "', ':"');
		}
		
		return strExtension;
	},
	
	_isStencilSetExtensionLoaded: function() {
		var result = /*this.isStencilSetExtensionLoaded(this.wdStencilSetExtensionNamespace) || */this.isStencilSetExtensionLoaded(this.rsStencilSetExtensionNamespace);
		
		return true;
	},
	
	createAnnotation: function() {	
		//var options = JSON.stringify({action : 'transform'});		
		
		var selectedElements = this.facade.getSelection();
		
		if(selectedElements.length == 1){ 
			var task = selectedElements[0]; 
			if(task.getStencil().id().endsWith("Task")){
				var optionAnnotation = {
					type:"http://b3mn.org/stencilset/bpmn2.0#TextAnnotation",
					position:{
						x:task.bounds.upperLeft().x + 200,
						y:task.bounds.upperLeft().y
					},
					namespace:task.getStencil().namespace(),
					parent:task.parent
				};
				var annotation = this.facade.createShape(optionAnnotation);
				
				var ssn 	= this.facade.getStencilSets().keys()[0];
				var stencil = ORYX.Core.StencilSet.stencil(ssn + "Association_Undirected");
			
				association=new ORYX.Core.Edge({
					'eventHandlerCallback':this.facade.raiseEvent
				},stencil);
				
				association.dockers.first().setDockedShape(task);
				association.dockers.first().setReferencePoint({
					x: task.bounds.width() / 2.0,
					y: task.bounds.height() / 2.0
				});
				
				association.dockers.last().setDockedShape(annotation);
				association.dockers.last().setReferencePoint({
					x: 0,
					y: annotation.bounds.height() / 2.0
				});
				
				var resources = JSON.parse(task.properties["oryx-resources"]);
				
				if(resources["totalCount"] != undefined){
					var text = "";
					if(resources.items[0].definition != ""){
						text +=	"\n- Definition: "+resources.items[0].definition;
					}else{
						if(resources.items[0].name != ""){
							text +=	"- Role: "+resources.items[0].name;
						}
						if(resources.items[0].documentation != ""){
							text +=	"\n- Documentation: "+resources.items[0].documentation;
						}
					}
					
					if(resources.items[0].resourceRef != ""){
						var resourceName = resources.items[0].resourceRef;
						if(resourceName.indexOf(" | ") > 0){
							resourceName = resourceName.substr(0, resourceName.indexOf(" | "));
						}
						text +=	"\n- Ref: //resource[@type='ResourceClassifier' and @name='"+resourceName+"']";
						if(resources.items[0].resourceParameterBinding != ""){
							text +=	"\n- PB: resourceParameter[@name='"+resources.items[0].resourceParameterBinding+"']"+resources.items[0].resourceAssignmentExpression;
						}
					}else if(resources.items[0].resourceAssignmentExpression != ""){
						text +=	"\n- RAE: "+resources.items[0].resourceAssignmentExpression;
					}
					if(resources.items[0].resolutionConstraint != ""){
						text +=	"\n- Constraint: "+resources.items[0].resolutionConstraint;
					}
					if(resources.items[0].trigger != ""){
						text +=	"\n- Trigger: "+resources.items[0].trigger;
					}
					if(resources.items[0].escalation != ""){
						text +=	"\n- Escalation: "+resources.items[0].escalation;
					}
					
					annotation.setProperty("oryx-text", text);
				}
				
				
				this.facade.getCanvas().add(association);
				
				this.facade.getCanvas().update();
			}
		}
	},
	
	bpmn2WDToYAWL: function(button, pressed){
		this.mapBPMNWDtoYAWL();
	},
	
    mapBPMNWDtoYAWL: function(options){
		Ext.applyIf(options || {}, {
	          showErrors: true,
	          ononMappingSucceeded: Ext.emptyFn,
	          onErrors: Ext.emptyFn,
	          onFailure: Ext.emptyFn
	        });
		
		var data = this.facade.getSerializedJSON();

		var _this = this;
		
		new Ajax.Request(this.bpmnSerializationHandlerUrl, {
			method: 'POST',
			asynchronous: false,
			parameters : {
				rsm : _this.importedRSM,
				data: data
			},
			onSuccess: function(request){
				alert(request.responseText);
			}.bind(this),
			onFailure: function(){
				Ext.Msg.hide();
				options.onFailure();
			}
		});
	},
	
	bpmn2RSToYAWL: function(button, pressed){
		this.mapBPMNRStoYAWL();
	}, 
	
	mapBPMNRStoYAWL: function(options){
		Ext.applyIf(options || {}, {
	          showErrors: true,
	          ononMappingSucceeded: Ext.emptyFn,
	          onErrors: Ext.emptyFn,
	          onFailure: Ext.emptyFn
	        });
		
		var data = this.facade.getSerializedJSON();

		new Ajax.Request(this.bpmnRSSerializationHandlerUrl, {
			method: 'POST',
			asynchronous: false,
			parameters : {
				data: data
			},
			onSuccess: function(request){
				alert(request.responseText);
			}.bind(this),
			onFailure: function(){
				Ext.Msg.hide();
				options.onFailure();
			}
		});
	},
	
	getModelId : function(){
		var erdf = this.facade.getERDF();
		erdf = erdf.substring(erdf.indexOf('/model/'));
		erdf = erdf.substring(7, erdf.indexOf('"'));
		
		return erdf;
	},
	
	getModelIdsByTag : function(tag){
		var returnValue;
		new Ajax.Request('/backend/poem/filter', {
			method: 'GET',
			asynchronous: false,
			parameters: {
				access: "",
				sort: "lastChange",
				tags: tag
			},
			onSuccess: function(request){
				resp = request.responseText.evalJSON();
				for(var i=0; i<resp.length; i++){
					resp[i] = resp[i].replace('/model/', '');
				}
				returnValue = resp;
			}.bind(this)
    	});
		return returnValue;
	},
	
	getModelMeta : function(id) {
		var returnValue;
		new Ajax.Request('/backend/poem/model/'+id+'/meta', {
			method: 'GET',
			asynchronous: false,
			parameters: {
			},
			onSuccess: function(request){
				resp = request.responseText.evalJSON();
				resp["jsonUri"] = new String(resp["pngUri"]).replace("png", "json");
				resp["id"] = id;
				returnValue = resp;
			}.bind(this)
    	});
		return returnValue;
	},
	
	getListModelsByTag : function(tag){
		var returnValue = new Array();
		var ids = this.getModelIdsByTag(tag);
		
		for(var i=0; i<ids.length; i++){
			returnValue[i] = this.getModelMeta(ids[i]);
		}
		
		return returnValue;
	},
	
	showRPIM : function (){
		var arrRPIM = this.getListModelsByTag("RPIM");
		
		var arrColumns = new Array();
		for(var i=0; i< arrRPIM.length; i++){
			var rpim = arrRPIM[i];
			arrColumns.push([rpim['id'], rpim['title'], rpim['jsonUri']]);
		}
		
		// Create a new Selection Model
        var sm = new Ext.grid.CheckboxSelectionModel();
		
		var grid = new Ext.grid.GridPanel({
        	deferRowRender: false,
            id: 'oryx_rpim_grid',
            store: new Ext.data.SimpleStore({
                fields: ['id', 'title', 'jsonUri']
            }),
            cm: new Ext.grid.ColumnModel([sm, {
                header: "Select an RPIM",
                width: 200,
                sortable: true,
                dataIndex: 'title'
            }]),
            sm: sm,
            frame: true,
            width: 200,
            height: 200,
            iconCls: 'icon-grid',
            listeners: {
                "render": function(){
                    this.getStore().loadData(arrColumns);
                    //selectItems.defer(1);
                }
            }
        });
		
		// Create a new Panel
        var panel = new Ext.Panel({
            items: [{
                xtype: 'label',
                text: 'Select the RPIM you want to load',
                style: 'margin:10px;display:block'
            }, grid],
            frame: true,
            buttons: [{
                text: 'Import',
                handler: function(){
                    var selectionModel = Ext.getCmp('oryx_rpim_grid').getSelectionModel();
                    var result = selectionModel.selections.items.collect(function(item){
                        return item.data;
                    });
					
                    this.facade.getCanvas().setProperty('oryx-rpim', result[0].jsonUri);
					this.importRPIM(result[0].jsonUri);
					
					//panel.close();
                    Ext.getCmp('oryx_new_rpim_window').close();
                    //successCallback(result);
                }.bind(this)
            }, {
                text: ORYX.I18N.SSExtensionLoader.labelCancel,
                handler: function(){
                    Ext.getCmp('oryx_new_rpim_window').close();
                }.bind(this)
            }]
        })
        
        // Create a new Window
        var window = new Ext.Window({
            id: 'oryx_new_rpim_window',
            width: 227,
            title: "Select an RPIM",
            floating: true,
            shim: true,
            modal: true,
            resizable: false,
            autoHeight: true,
            items: [panel]
        })
        
        // Show the window
        window.show();
	},
	
	importRPIM : function(jsonUri){		
		var rpimJSON = "";
		new Ajax.Request(jsonUri, {
			method: 'GET',
			asynchronous: false,
			parameters : {
			},
			onSuccess: function(request){
				rpimJSON = request.responseText;
			}.bind(this),
			onFailure: function(){
				rpimJSON = "";
			}
		});
		
		this.importedRPIM = rpimJSON;
		
		var _this = this;
		new Ajax.Request(this.rpimExtensionUrl, {
			method: 'POST',
			asynchronous: false,
			parameters : {
				data: rpimJSON
			},
			onSuccess: function(request){
				var items = JSON.parse(request.responseText);
				
				_this.setExtensionValues(items);
				_this.loadExtension();
				
			}.bind(this),
			onFailure: function(){
				Ext.Msg.hide();
				options.onFailure();
			}
		});
		
	},
	
	showRSM : function (){
		var arrRSM = this.getListModelsByTag("RSM");

		var arrColumns = new Array();
		for(var i=0; i< arrRSM.length; i++){
			var rsm = arrRSM[i];
			arrColumns.push([rsm['id'], rsm['title'], rsm['jsonUri']]);
		}
		
		// Create a new Selection Model
        var sm = new Ext.grid.CheckboxSelectionModel();
		
		var grid = new Ext.grid.GridPanel({
        	deferRowRender: false,
            id: 'oryx_rsm_grid',
            store: new Ext.data.SimpleStore({
                fields: ['id', 'title', 'jsonUri']
            }),
            cm: new Ext.grid.ColumnModel([sm, {
                header: "Select an RSM",
                width: 200,
                sortable: true,
                dataIndex: 'title'
            }]),
            sm: sm,
            frame: true,
            width: 200,
            height: 200,
            iconCls: 'icon-grid',
            listeners: {
                "render": function(){
                    this.getStore().loadData(arrColumns);
                    //selectItems.defer(1);
                }
            }
        });
		
		// Create a new Panel
        var panel = new Ext.Panel({
            items: [{
                xtype: 'label',
                text: 'Select the RSM you want to load',
                style: 'margin:10px;display:block'
            }, grid],
            frame: true,
            buttons: [{
                text: 'Import',
                handler: function(){
                    var selectionModel = Ext.getCmp('oryx_rsm_grid').getSelectionModel();
                    var result = selectionModel.selections.items.collect(function(item){
                        return item.data;
                    });
					
                    this.facade.getCanvas().setProperty('oryx-rsm', result[0].jsonUri);
					this.importRSM(result[0].jsonUri);
					
					//panel.close();
                    Ext.getCmp('oryx_new_rsm_window').close();
                    //successCallback(result);
                }.bind(this)
            }, {
                text: ORYX.I18N.SSExtensionLoader.labelCancel,
                handler: function(){
                    Ext.getCmp('oryx_new_rsm_window').close();
                }.bind(this)
            }]
        })
        
        // Create a new Window
        var window = new Ext.Window({
            id: 'oryx_new_rsm_window',
            width: 227,
            title: "Select an RSM",
            floating: true,
            shim: true,
            modal: true,
            resizable: false,
            autoHeight: true,
            items: [panel]
        })
        
        // Show the window
        window.show();
	},
	
	importRSM : function(jsonUri){		
		var rsmJSON = "";
		new Ajax.Request(jsonUri, {
			method: 'GET',
			asynchronous: false,
			parameters : {
			},
			onSuccess: function(request){
				rsmJSON = request.responseText;
			}.bind(this),
			onFailure: function(){
				rsmJSON = "";
			}
		});
		
		this.importedRSM = rsmJSON;
		
		var _this = this;
		new Ajax.Request(this.rsmExtensionUrl, {
			method: 'POST',
			asynchronous: false,
			parameters : {
				data: rsmJSON
			},
			onSuccess: function(request){
				var items = JSON.parse(request.responseText);
				_this.setExtensionValues(items);
				_this.loadExtension();
			}.bind(this),
			onFailure: function(){
				Ext.Msg.hide();
				options.onFailure();
			}
		});
	},
	
	selectBPLM : function(){
		this.select("BPLM");
	},
	
	selectPIEPM : function(){
		this.select("PI-EPM");
	},
	
	selectPIRSM : function(){
		this.select("PI-RSM");
	},
	
	select : function(tag){
		var arr = this.getListModelsByTag(tag);

		var arrColumns = new Array();
		for(var i=0; i< arr.length; i++){
			var row = arr[i];
			arrColumns.push([row['id'], row['title'], row['jsonUri']]);
		}
		
		// Create a new Selection Model
        var sm = new Ext.grid.CheckboxSelectionModel();
		
		var grid = new Ext.grid.GridPanel({
        	deferRowRender: false,
            id: 'oryx_model_grid',
            store: new Ext.data.SimpleStore({
                fields: ['id', 'title', 'jsonUri']
            }),
            cm: new Ext.grid.ColumnModel([sm, {
                header: "Select a "+tag,
                width: 200,
                sortable: true,
                dataIndex: 'title'
            }]),
            sm: sm,
            frame: true,
            width: 200,
            height: 200,
            iconCls: 'icon-grid',
            listeners: {
                "render": function(){
                    this.getStore().loadData(arrColumns);
                    //selectItems.defer(1);
                }
            }
        });
		
		// Create a new Panel
        var panel = new Ext.Panel({
            items: [{
                xtype: 'label',
                text: 'Select the '+tag+' you want to load',
                style: 'margin:10px;display:block'
            }, grid],
            frame: true,
            buttons: [{
                text: 'Import',
                handler: function(){
                    var selectionModel = Ext.getCmp('oryx_model_grid').getSelectionModel();
                    var result = selectionModel.selections.items.collect(function(item){
                        return item.data;
                    });
					
                    this.facade.getCanvas().setProperty('oryx-rsm', result[0].jsonUri);
					this.initialize(result[0].jsonUri);
					
					//panel.close();
                    Ext.getCmp('oryx_new_model_window').close();
                    //successCallback(result);
                }.bind(this)
            }, {
                text: ORYX.I18N.SSExtensionLoader.labelCancel,
                handler: function(){
                    Ext.getCmp('oryx_new_model_window').close();
                }.bind(this)
            }]
        })
        
        // Create a new Window
        var window = new Ext.Window({
            id: 'oryx_new_model_window',
            width: 227,
            title: "Select a "+tag,
            floating: true,
            shim: true,
            modal: true,
            resizable: false,
            autoHeight: true,
            items: [panel]
        })
        
        // Show the window
        window.show();
	},
	
	initialize : function(jsonUri){
		var strJSON = "";
		new Ajax.Request(jsonUri, {
			method: 'GET',
			asynchronous: false,
			parameters : {
			},
			onSuccess: function(request){
				strJSON = request.responseText;
			}.bind(this),
			onFailure: function(){
				strJSON = "";
			}
		});
		
		this.facade.importJSON(JSON.parse(strJSON));
	}
};

ORYX.Plugins.BPMN2_0WD = ORYX.Plugins.AbstractPlugin.extend(ORYX.Plugins.BPMN2_0WD);