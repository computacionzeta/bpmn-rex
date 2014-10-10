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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * This servlet provides the access point to the BPMN 2.0 + WD Extension to Bonita Transformation
 * 
 * @author Luis Stroppi
 * 
 */
public class BPMN2_0WDToBonita extends HttpServlet {
	
	JSONObject oryxStencil;
	JSONObject rsmStencil;
	String definitions = "";

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
			res.getWriter().print(this.getActorMappings(js, rs));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getActorMappings(String js, String rsm){
		String actorMappings = "";
		
		try {
			this.oryxStencil = new JSONObject(js);
			this.rsmStencil = new JSONObject(rsm);
			
			actorMappings += "<actormapping:actorMappings xmlns:actormapping=\"http://www.bonitasoft.org/ns/actormapping/6.0\">\n";
			
			JSONArray flowElements = new JSONArray();
			flowElements.put(this.getResourcesByStencilId(this.oryxStencil, "StartNoneEvent").getJSONObject(0));
			while(flowElements.length()>0){
				for(int i=0; i<flowElements.length(); i++){
					
					JSONObject controlElement = flowElements.getJSONObject(i); 
					
					if(controlElement.getJSONObject("stencil").getString("id").equals("Task")){
						
						JSONArray resources = null;
						try {
							resources = controlElement.getJSONObject("properties").getJSONObject("resources").getJSONArray("items");
						} catch (JSONException e) {}
						
						for(int j=0; j<resources.length(); j++){
							actorMappings += this.getActorMapping(resources.getJSONObject(j));
						}
						
					}
				}
			}
			
			actorMappings += "</actormapping:actorMappings>\n";
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return actorMappings;
	}
	
	private String getActorMapping(JSONObject resourceRole){
		String actorMapping = "";

		
		try {
			
			String id = resourceRole.getString("resourceRef").substring(resourceRole.getString("resourceRef").indexOf(" | ")+3, resourceRole.getString("resourceRef").length());
			
			JSONObject resource = this.getResourceById(this.rsmStencil, id);
			
			String definition = resource.getJSONObject("properties").getString("definition");
			
			actorMapping += "\t<actorMapping name=\"Employee actor\">\n";
			actorMapping += "\t\t<users>\n";
			if(definition.equals("User")){
				actorMapping += "\t\t\t<user>"+resource.getString("resourceRef")+"</user>\n";
			}
			actorMapping += "\t\t</users>\n";
			actorMapping += "\t\t<groups>\n";
			if(definition.equals("Group")){
				actorMapping += "\t\t\t<group>"+resource.getString("resourceRef")+"</group>\n";
			}
			actorMapping += "\t\t</groups>\n";
			actorMapping += "\t\t<roles>\n";
			if(definition.equals("Role")){
				actorMapping += "\t\t\t<role>"+resource.getString("resourceRef")+"</role>\n";
			}
			actorMapping += "\t\t</roles>\n";
			actorMapping += "\t\t<memberships>\n";
			if(definition.equals("Membership")){
				actorMapping += "\t\t\t<membership>"+resource.getString("resourceRef")+"</membership>\n";
			}
			actorMapping += "\t\t</memberships>\n";
			actorMapping += "\t</actorMapping>\n";
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return actorMapping;
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

}
