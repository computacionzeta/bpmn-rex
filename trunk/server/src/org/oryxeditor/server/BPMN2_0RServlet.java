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
public class BPMN2_0RServlet extends HttpServlet {
	
	JSONObject rsm;
	JSONObject extension;

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        String js = req.getParameter("data");
        
        res.setContentType("application/json");
        
        try {
        	res.setStatus(200);
			res.getWriter().print(this.getResources(js));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getResources(String js){
		String spec = "";
		
		try {
			this.rsm = new JSONObject(js);
			
			JSONArray classifierDefinitions = this.getResourcesByStencilId(this.rsm, "resourceClassifier");
			JSONArray resourceDefinitions 	= this.getResourcesByStencilId(this.rsm, "humanResource");
			JSONArray parameterDefinitions 	= this.getResourcesByStencilId(this.rsm, "resourceParameter");
			
			JSONObject result = new JSONObject();
			
			JSONArray resourceOptions = new JSONArray();
			JSONArray parameterOptions = new JSONArray();
			
			for(int i=0; i<classifierDefinitions.length(); i++){
				JSONObject option = new JSONObject();
				JSONObject definition = classifierDefinitions.getJSONObject(i);
				
				option.put("id", "r"+resourceOptions.length());
				option.put("title", definition.getJSONObject("properties").getString("name"));
				option.put("value", definition.getJSONObject("properties").getString("name")+" | " +definition.getString("resourceId").substring(5, (definition.getString("resourceId").length())));
				option.put("refToView", "");
				
				resourceOptions.put(option);
			}
			
			for(int i=0; i<resourceDefinitions.length(); i++){
				JSONObject option = new JSONObject();
				JSONObject definition = resourceDefinitions.getJSONObject(i);
				
				option.put("id", "r"+resourceOptions.length());
				option.put("title", definition.getJSONObject("properties").getString("name"));
				option.put("value", definition.getString("resourceId"));
				option.put("refToView", "");
				
				resourceOptions.put(option);
			}
			
			result.put("resources", resourceOptions);
			
			for(int i=0; i<parameterDefinitions.length(); i++){
				JSONObject option = new JSONObject();
				JSONObject definition = parameterDefinitions.getJSONObject(i);
				
				option.put("id", "r"+parameterOptions.length());
				option.put("title", definition.getJSONObject("properties").getString("name"));
				option.put("value", definition.getJSONObject("properties").getString("name"));
				option.put("refToView", "");
				
				parameterOptions.put(option);
			}
			
			result.put("parameterOptions", parameterOptions);
			
			spec = result.toString();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return spec;
	}
	
	public String getIdmExtension(String js){
		String spec = "";
		
		try {
			this.extension = new JSONObject("{\"title\":\"IDM Extension\",\"namespace\":\"http://www.cidisi.org/bpmn/extensions/idm#\",\"description\":\"Extend the model with implementation definition elements.\",\"extends\":\"http://b3mn.org/stencilset/bpmn2.0#\",\"propertyPackages\" : [],\"stencils\":[], \"properties\" : [{\"roles\" : [\"Task\"],properties : [{\"id\":\"resources\",\"type\":\"Complex\",\"title\":\"Resources\",\"value\":\"\",\"description\":\"\",\"readonly\":false,\"optional\":true,\"popular\":true,\"complexItems\": [{\"id\":\"name\",\"name\":\"Name\",\"type\":\"String\",\"value\":\"\",\"width\":100,\"optional\":true},{\"id\":\"documentation\",\"name\":\"Documentation\",\"type\":\"String\",\"value\":\"\",\"width\":100,\"optional\":true},{\"id\":\"resourceRef\",\"name\":\"ResourceRef\",\"type\":\"String\",\"value\":\"\",\"width\":100,\"optional\":true},{\"id\":\"resourceParameterBinding\",\"name\":\"ResourceParameterBinding\",\"type\":\"String\",\"value\":\"\",\"width\":100,\"optional\":true},{\"id\":\"resourceAssignmentExpression\",\"name\":\"ResourceAssignmentExpression\",\"type\":\"String\",\"value\":\"\",\"width\":100,\"optional\":true},{\"id\":\"trigger\",\"name\":\"Trigger\",\"type\":\"String\",\"value\":\"\",\"width\":100,\"optional\":true},{\"id\":\"escalation\",\"name\":\"Escalation\",\"type\":\"String\",\"value\":\"\",\"width\":100,\"optional\":true},{\"id\":\"definition\",\"name\":\"Definition\",\"type\":\"Choice\",\"value\":\"\",\"width\":100,\"optional\":true,\"items\": []}]}]}]}");
			this.rsm = new JSONObject(js);
			
			
			JSONArray roleDefinitions = this.getResourcesByStencilId(this.rsm, "roleDefinition");
			JSONArray roleOptions = this.extension.getJSONArray("properties").
				getJSONObject(0).getJSONArray("properties").
				getJSONObject(0).getJSONArray("complexItems").
				getJSONObject(7).getJSONArray("items");
			
			for(int i=0; i<roleDefinitions.length(); i++){
				JSONObject option = new JSONObject();
				JSONObject definition = roleDefinitions.getJSONObject(i);
				
				option.put("id", "r"+i);
				option.put("title", definition.getJSONObject("properties").getString("name"));
				option.put("value", definition.getJSONObject("properties").getString("name"));
				option.put("refToView", "");
				
				roleOptions.put(option);
			}
			
			spec = this.extension.toString();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return spec;
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
					JSONObject ob = subResult.getJSONObject(j);
					result.put(ob);
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
}
