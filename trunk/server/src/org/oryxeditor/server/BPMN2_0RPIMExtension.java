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
public class BPMN2_0RPIMExtension extends HttpServlet {
	
	JSONObject idm;
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
			res.getWriter().print(this.getOptions(js));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getOptions(String js){
		
		JSONObject impls = new JSONObject();
		String result = "";
		
		try {
			this.idm = new JSONObject(js);
			
			impls.put("humanResourceOptions", this.getHumanResourceOptions());
			impls.put("resourceClassifierOptions", this.getResourceClassifierOptions());
			impls.put("relationshipOptions", this.getRelationshipOptions());
			impls.put("resourcePrivilegeOptions", this.getResourcePrivilegeOptions());
			impls.put("classificationOptions", this.getClassificationOptions());
			impls.put("parameterTypeOptions", this.getParameterTypeOptions());
			
			impls.put("roleOptions", this.getRoleOptions());
			impls.put("taskPrivilegeOptions", this.getTaskPrivilegeOptions());
			impls.put("resolutionConstraintOptions", this.getResolutionConstraintOptions());
			
			
			result = impls.toString();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}		
		
		return result;
	}
	
	public JSONArray getResourceClassifierOptions(){
		
		JSONArray resourceClassifierImpls = new JSONArray();
		
		try {			
			JSONArray classifierStencils = this.getResourcesByStencilId(this.idm, "resourceClassifierImpl");
			
			for(int i=0; i<classifierStencils.length(); i++){
				JSONObject option = new JSONObject();
				JSONObject impl = classifierStencils.getJSONObject(i);
				
				option.put("id", "r"+i);
				option.put("title", impl.getJSONObject("properties").getString("name"));
				option.put("value", impl.getJSONObject("properties").getString("name"));
				option.put("refToView", "");
				
				resourceClassifierImpls.put(option);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return resourceClassifierImpls;
	}
	
	public JSONArray getHumanResourceOptions(){
		
		JSONArray humanResourceImpls = new JSONArray();
		
		try {			
			JSONArray humanResourceStencils = this.getResourcesByStencilId(this.idm, "humanResourceImpl");
			
			for(int i=0; i<humanResourceStencils.length(); i++){
				JSONObject option = new JSONObject();
				JSONObject impl = humanResourceStencils.getJSONObject(i);
				
				option.put("id", "r"+i);
				option.put("title", impl.getJSONObject("properties").getString("name"));
				option.put("value", impl.getJSONObject("properties").getString("name"));
				option.put("refToView", "");
				
				humanResourceImpls.put(option);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return humanResourceImpls;
	}
	
	public JSONArray getRelationshipOptions(){
		
		JSONArray relationshipImpls = new JSONArray();
		
		try {			
			JSONArray relationshipStencils = this.getResourcesByStencilId(this.idm, "relationshipImpl");
			
			for(int i=0; i<relationshipStencils.length(); i++){
				JSONObject option = new JSONObject();
				JSONObject impl = relationshipStencils.getJSONObject(i);
				
				option.put("id", "r"+i);
				option.put("title", impl.getJSONObject("properties").getString("name"));
				option.put("value", impl.getJSONObject("properties").getString("name"));
				option.put("refToView", "");
				
				relationshipImpls.put(option);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return relationshipImpls;
	}
	
	public JSONArray getClassificationOptions(){
		
		JSONArray classificationOptions = new JSONArray();
		
		try {			
			JSONArray classificationStencils = this.getResourcesByStencilId(this.idm, "classificationImpl");
			
			for(int i=0; i<classificationStencils.length(); i++){
				JSONObject option = new JSONObject();
				JSONObject impl = classificationStencils.getJSONObject(i);
				
				option.put("id", "r"+i);
				option.put("title", impl.getJSONObject("properties").getString("name"));
				option.put("value", impl.getJSONObject("properties").getString("name"));
				option.put("refToView", "");
				
				classificationOptions.put(option);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return classificationOptions;
	}
	
	public JSONArray getRoleOptions(){
		JSONArray roleOptions = new JSONArray();
		
		try {			
			JSONArray roleImpls = this.getResourcesByStencilId(this.idm, "roleImpl");
			
			for(int i=0; i<roleImpls.length(); i++){
				JSONObject option = new JSONObject();
				JSONObject impl = roleImpls.getJSONObject(i);
				
				option.put("id", "r"+i);
				option.put("title", impl.getJSONObject("properties").getString("name"));
				option.put("value", impl.getJSONObject("properties").getString("name"));
				option.put("refToView", "");
				
				roleOptions.put(option);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return roleOptions;
	}
	
	public JSONArray getResolutionConstraintOptions(){
		JSONArray constraintOptions = new JSONArray();
		
		try {			
			JSONArray resolutionConstraintStencils = this.getResourcesByStencilId(this.idm, "resolutionConstraintImpl");
			
			for(int i=0; i<resolutionConstraintStencils.length(); i++){
				JSONObject option = new JSONObject();
				JSONObject impl = resolutionConstraintStencils.getJSONObject(i);
				
				option.put("id", "r"+i);
				option.put("title", impl.getJSONObject("properties").getString("name"));
				option.put("value", impl.getJSONObject("properties").getString("name"));
				option.put("refToView", "");
				
				constraintOptions.put(option);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return constraintOptions;
	}
	
	public JSONArray getTaskPrivilegeOptions(){
		JSONArray taskPrivilegeOptions = new JSONArray();
		
		try {			
			JSONArray taskPrivilegeStencils = this.getResourcesByStencilId(this.idm, "taskPrivilegeImpl");
			
			for(int i=0; i<taskPrivilegeStencils.length(); i++){
				JSONObject option = new JSONObject();
				JSONObject impl = taskPrivilegeStencils.getJSONObject(i);
				
				option.put("id", "r"+i);
				option.put("title", impl.getJSONObject("properties").getString("name"));
				option.put("value", impl.getJSONObject("properties").getString("name"));
				option.put("refToView", "");
				
				taskPrivilegeOptions.put(option);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return taskPrivilegeOptions;
	}
	
	public JSONArray getResourcePrivilegeOptions(){
		JSONArray resourcePrivilegeOptions = new JSONArray();
		
		try {			
			JSONArray resourcePrivilegeStencils = this.getResourcesByStencilId(this.idm, "resourcePrivilegeImpl");
			
			for(int i=0; i<resourcePrivilegeStencils.length(); i++){
				JSONObject option = new JSONObject();
				JSONObject impl = resourcePrivilegeStencils.getJSONObject(i);
				
				option.put("id", "r"+i);
				option.put("title", impl.getJSONObject("properties").getString("name"));
				option.put("value", impl.getJSONObject("properties").getString("name"));
				option.put("refToView", "");
				
				resourcePrivilegeOptions.put(option);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resourcePrivilegeOptions;
	}
	
	public JSONArray getParameterTypeOptions(){
		JSONArray parameterTypeOptions = new JSONArray();
		
		try {			
			JSONArray parameterTypeStencils = this.getResourcesByStencilId(this.idm, "parameterTypeImpl");
			
			for(int i=0; i<parameterTypeStencils.length(); i++){
				JSONObject option = new JSONObject();
				JSONObject impl = parameterTypeStencils.getJSONObject(i);
				
				option.put("id", "r"+i);
				option.put("title", impl.getJSONObject("properties").getString("name"));
				option.put("value", impl.getJSONObject("properties").getString("name"));
				option.put("refToView", "");
				
				parameterTypeOptions.put(option);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return parameterTypeOptions;
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
