package org.oryxeditor.server;

/**
 * Copyright (c) 2013 
 * 
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

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * This servlet provides the access point to the BPMN 2.0 + WD Extension to YAWL Transformation
 * 
 * @author Luis Stroppi
 * 
 */
public class BPMN2_0WDToYAWL extends HttpServlet {
	
	JSONObject oryxStencil;
	JSONObject rsmStencil;
	String taskDecompositions = "";

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        String js = req.getParameter("data");
        String rs = req.getParameter("rsm");
        
        res.setContentType("application/xml");
        
        try {
        	res.setStatus(200);
			res.getWriter().print(this.getYAWLSpecification(js, rs));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getYAWLSpecification(String js, String rsm){
		String spec = "";
		String end = "";
		HashSet<String> translatedElements = new HashSet<String>();
		try {
			this.oryxStencil = new JSONObject(js);
			this.rsmStencil = new JSONObject(rsm);
			
			Float version = 0.1F;
			if(this.oryxStencil.getJSONObject("properties").getString("version").length() > 0){
				version = new Float(this.oryxStencil.getJSONObject("properties").getString("version"));
			}
			
			spec += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			spec += "<specificationSet xmlns=\"http://www.yawlfoundation.org/yawlschema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"2.1\" xsi:schemaLocation=\"http://www.yawlfoundation.org/yawlschema http://www.yawlfoundation.org/yawlschema/YAWL_Schema2.1.xsd\">\n";
			spec += "<specification uri=\"example\">\n";
			spec += "\t\t<metaData>\n";
			spec += "\t\t\t<creator>"+this.oryxStencil.getJSONObject("properties").getString("author")+"</creator>\n";
			spec += "\t\t\t<description>"+this.oryxStencil.getJSONObject("properties").getString("documentation")+"</description>\n";
			spec += "\t\t\t<version>"+version.toString()+"</version>\n";
			spec += "\t\t\t<persistent>false</persistent>\n";
			spec += "\t\t\t<identifier>"+this.oryxStencil.getString("resourceId")+"</identifier>\n";
			spec += "\t\t</metaData>\n";
			spec += "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" />\n";
			spec += "<decomposition id=\"New_Net_1\" isRootNet=\"true\" xsi:type=\"NetFactsType\">\n";

			spec += "\t\t\t<processControlElements>\n";
			JSONArray flowElements = new JSONArray();
			flowElements.put(this.getResourcesByStencilId(this.oryxStencil, "StartNoneEvent").getJSONObject(0));
			while(flowElements.length()>0){
				JSONArray nextFlowElements = new JSONArray();
				for(int i=0; i<flowElements.length(); i++){
					
					JSONObject controlElement = flowElements.getJSONObject(i); 
					
					if(!translatedElements.contains(controlElement.getString("resourceId"))){
					
						if(controlElement.getJSONObject("stencil").getString("id").equals("StartNoneEvent")){
							spec += this.getStartNoneEventToInputCondition(controlElement);
						}else if(controlElement.getJSONObject("stencil").getString("id").equals("Task")){
							spec += this.getTaskToTask(controlElement);
						}else if(controlElement.getJSONObject("stencil").getString("id").equals("EndNoneEvent")){
							end += this.getEndNoneEventToOutputCondition(controlElement);
						}
						
						JSONArray nextToThisElement = this.getNextControlElements(controlElement);
						for(int j=0; j<nextToThisElement.length(); j++){
							nextFlowElements.put(nextToThisElement.getJSONObject(j));
						}
						
						translatedElements.add(controlElement.getString("resourceId"));
					}
				}
				flowElements = nextFlowElements;
			}
			spec += end;
			spec += "\t\t\t</processControlElements>\n";
			spec += "\t\t</decomposition>\n";
			spec += this.taskDecompositions;
			spec += "\t</specification>\n";
			spec += "</specificationSet>\n";
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return spec;
	}
	
	private String getStartNoneEventToInputCondition(JSONObject jo){
		String js = "";
		
		try {
			JSONObject flow = this.getResourceById(this.oryxStencil, jo.getJSONArray("outgoing").getJSONObject(0).getString("resourceId"));
			JSONObject target = this.getResourceById(this.oryxStencil, flow.getJSONArray("outgoing").getJSONObject(0).getString("resourceId"));
			js += "<inputCondition id=\""+jo.getString("resourceId")+"\">\n";
			js += "\t<flowsInto>\n";
			js += "\t\t<nextElementRef id=\""+target.getString("resourceId")+"\" />\n";
	        js += "\t</flowsInto>\n";
	        js += "</inputCondition>\n"; 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return js;
	}
	
	private String getTaskToTask(JSONObject joTask){
		String js = "";
		String split = "and";
		String join  = "xor";
		try {
			
			JSONArray nextElements = this.getNextElements(joTask);
			if(nextElements.length() > 1){
				split = "xor";
			}
			
			JSONObject resourcing = null;
			try {
				resourcing = joTask.getJSONObject("properties").getJSONObject("resources").getJSONArray("items").getJSONObject(0);
			} catch (JSONException e) {}
			
			String resourcingString = "";
			if(resourcing != null){
				if(resourcing.getString("definition").equals("PotentialOwner")){
					resourcingString = this.getPotentialOwnerToSUU(resourcing);
				}else if(resourcing.getString("definition").equals("Owner")){
					resourcingString = this.getOwnerToSSU(resourcing);
				}else if(resourcing.getString("definition").equals("Executor")){
					resourcingString = this.getOwnerToSSS(resourcing);
				}
			}
			
			js += "<task id=\""+joTask.getString("resourceId")+"\">\n";
			js += "\t<name>"+joTask.getJSONObject("properties").getString("name")+"</name>\n";
			for(int i=0; i<nextElements.length(); i++){
				JSONObject target = nextElements.getJSONObject(i);
				
				js += "\t<flowsInto>\n";
				js += "\t\t<nextElementRef id=\""+target.getString("resourceId")+"\" />\n";
				js += "\t</flowsInto>\n";
			}
			js += "\t<join code=\""+join+"\" />\n";
			js += "\t<split code=\""+split+"\" />\n";
			js += resourcingString;
			js += "\t<decomposesTo id=\"decomposition_"+joTask.getString("resourceId").substring(5)+"\" />\n";
			js += "</task>\n";
			
			this.taskDecompositions += "<decomposition id=\"decomposition_"+joTask.getString("resourceId").substring(5)+"\" xsi:type=\"WebServiceGatewayFactsType\">\n";
			this.taskDecompositions += "\t<externalInteraction>manual</externalInteraction>\n";
			this.taskDecompositions += "</decomposition>\n";
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return js;
	}
	
	private String getPotentialOwnerToSUU(JSONObject potentialOwner){
		String srr = "";
		
	    try {
	    	srr += "\t<resourcing>\n";
			srr += "\t\t<offer initiator=\"system\">\n";
			srr += "\t\t\t<distributionSet>\n";
		    srr += "\t\t\t\t<initialSet>\n";
			srr += "\t\t\t\t\t<role>"+this.getResourceId(potentialOwner.getString("resourceRef"))+"</role>\n";
			srr += "\t\t\t\t</initialSet>\n";
		    srr += "\t\t\t</distributionSet>\n";
		    srr += "\t\t</offer>\n";
		    srr += "\t\t<allocate initiator=\"user\" />\n";
		    srr += "\t\t<start initiator=\"user\" />\n";
		    srr += "\t</resourcing>\n";
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return srr;
	}
	
	private String getOwnerToSSU(JSONObject owner){
		String srr = "";
		
	    try {
	    	srr += "\t<resourcing>\n";
			srr += "\t\t<offer initiator=\"system\">\n";
			srr += "\t\t\t<distributionSet>\n";
		    srr += "\t\t\t\t<initialSet>\n";
			srr += "\t\t\t\t\t<role>"+this.getResourceId(owner.getString("resourceRef"))+"</role>\n";
			srr += "\t\t\t\t</initialSet>\n";
		    srr += "\t\t\t</distributionSet>\n";
		    srr += "\t\t</offer>\n";
		    srr += "\t\t<allocate initiator=\"system\">\n";
		    srr += "\t\t\t<allocator>\n";
		    srr += "\t\t\t\t<name>"+owner.getString("resolutionConstraint")+"</name>\n";
		    srr += "\t\t\t</allocator>\n";
		    srr += "\t\t</allocate>\n";
		    srr += "\t\t<start initiator=\"user\" />\n";
		    srr += "\t</resourcing>\n";
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return srr;
	}
	
	private String getOwnerToSSS(JSONObject executor){
		String srr = "";
		
	    try {
	    	srr += "\t<resourcing>\n";
			srr += "\t\t<offer initiator=\"system\">\n";
			srr += "\t\t\t<distributionSet>\n";
		    srr += "\t\t\t\t<initialSet>\n";
			srr += "\t\t\t\t\t<role>"+this.getResourceId(executor.getString("resourceRef"))+"</role>\n";
			srr += "\t\t\t\t</initialSet>\n";
		    srr += "\t\t\t</distributionSet>\n";
		    srr += "\t\t</offer>\n";
		    srr += "\t\t<allocate initiator=\"system\">\n";
		    srr += "\t\t\t<allocator>\n";
		    srr += "\t\t\t\t<name>"+executor.getString("resolutionConstraint")+"</name>\n";
		    srr += "\t\t\t</allocator>\n";
		    srr += "\t\t</allocate>\n";
		    srr += "\t\t<start initiator=\"system\" />\n";
		    srr += "\t</resourcing>\n";
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return srr;
	}
	
	private String getEndNoneEventToOutputCondition(JSONObject jo){
		String js = "";
		
		try {
			js += "<outputCondition id=\""+jo.getString("resourceId")+"\" />\n";
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return js;
	}
	
	private JSONObject getResourceById(JSONObject jo, String resourceId){
		
		try {
			if(jo.getString("resourceId").endsWith(resourceId)){
				return jo;
			}else{
				JSONArray ja = jo.getJSONArray("childShapes");
				if(ja.length() == 0){
					return null;
				}else{
					for(int i=0; i<ja.length(); i++){
						if(ja.getJSONObject(i).getString("resourceId").equals(resourceId)){
							return ja.getJSONObject(i);
						}
					}
					for(int i=0; i<ja.length(); i++){
						JSONObject result = this.getResourceById(ja.getJSONObject(i), resourceId); 
						if(result != null){
							return result;
						}
					}
				}
				return null;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private JSONArray getResourcesByStencilId(JSONObject jo, String stencilId){
		JSONArray result = new JSONArray();
		
		try {
			if(jo.getJSONObject("stencil").getString("id").equals(stencilId)){
				result.put(jo);
			}
			
			JSONArray ja = jo.getJSONArray("childShapes");
				
			for(int i=0; i<ja.length(); i++){
				JSONArray subResult = this.getResourcesByStencilId(ja.getJSONObject(i), stencilId); 
				for(int j=0; j<subResult.length(); j++){
					result.put(subResult.getJSONObject(j));
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	private JSONArray getNextElements(JSONObject jo){
		JSONArray nextTasks = new JSONArray();
		
		JSONArray controlElements = this.getNextControlElements(jo);
		
		try {
			for(int i=0; i<controlElements.length(); i++){
				JSONObject controlElement;
				controlElement = controlElements.getJSONObject(i);
				
				if(!controlElement.getJSONObject("stencil").getString("id").equals("Exclusive_Databased_Gateway")){
					nextTasks.put(controlElement);
				}else{
					JSONArray nextToXOR = this.getNextControlElements(controlElement);
					for(int j=0; j<nextToXOR.length(); j++){
						if(!nextToXOR.getJSONObject(j).getJSONObject("stencil").getString("id").equals("Exclusive_Databased_Gateway")){
							nextTasks.put(nextToXOR.getJSONObject(j));
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return nextTasks;
	}
	
	private JSONArray getNextControlElements(JSONObject jo){
		JSONArray target = new JSONArray();
		try {
			for(int i=0; i<jo.getJSONArray("outgoing").length(); i++){
				JSONObject flow = this.getResourceById(this.oryxStencil, jo.getJSONArray("outgoing").getJSONObject(i).getString("resourceId"));
				target.put(this.getResourceById(this.oryxStencil, flow.getJSONArray("outgoing").getJSONObject(0).getString("resourceId")));
			}
		} catch (JSONException e) {
			//e.printStackTrace();
		}		
		return target;
	}
	
	private String getResourceId(String resourceRef){
		String result = "";
		
		try {
			
			String id = resourceRef.substring(resourceRef.indexOf(" | ")+3, resourceRef.length());
			
			JSONObject resource = this.getResourceById(this.rsmStencil, id);
			
			String definition = resource.getJSONObject("properties").getString("definition");
			
			if(definition.equals("Role")){
				result = resource.getString("resourceId").replace("oryx_", "RO-").replace("_", "-");
			}else if(definition.equals("Position")){
				result = resource.getString("resourceId").replace("oryx_", "PO-").replace("_", "-");
			}else if(definition.equals("Capability")){
				result = resource.getString("resourceId").replace("oryx_", "CA-").replace("_", "-");
			}else if(definition.equals("OrgGroup")){
				result = resource.getString("resourceId").replace("oryx_", "OG-").replace("_", "-");
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
}
