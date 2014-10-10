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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


/**
 * This servlet provides the access point to the BPMN 2.0 + RS Extension to Bonita Transformation
 * 
 * @author Luis Stroppi
 * 
 */

public class BPMN2_0RSToBonita {
	
	JSONObject oryxStencil;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        String js = req.getParameter("data");
        
        res.setContentType("application/xml");
        
        try {
        	res.setStatus(200);
			res.getWriter().print(this.getOrgData(js));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getOrgData(String js){
		String spec = "";
		
		try {
			this.oryxStencil = new JSONObject(js);
			
			spec += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			spec += "<organization:Organization xmlns:organization=\"http://documentation.bonitasoft.com/organization-xml-schema\">\n";
			spec += "<customUserInfoDefinitions/>\n";
			spec += "\t<users>\n";
			spec += this.getUsers()+"\n";
			spec += "\t</users>\n";
			spec += "\t<roles>\n";
			spec += this.getRoles()+"\n";
			spec += "\t</roles>\n";
			spec += "\t<groups>\n";
			spec += this.getGroups()+"\n";
			spec += "\t</groups>\n";
			spec += "\t<memberships>\n";
			spec += this.getMemberships()+"\n";
			spec += "\t</memberships>\n";
			spec += "</customUserInfoDefinitions>\n";
			spec += "</organization:Organization>\n";
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return spec;
	}
	
	private String getUsers(){
		String result = "";
		
		try {
		
			JSONArray users = this.getResourcesByStencilId(this.oryxStencil, "humanResource");

			for(int i=0; i<users.length(); i++){
				JSONObject participant = users.getJSONObject(i);
				if(participant.getJSONObject("properties").getString("definition").equals("User")){
					
					result += "\t<user userName=\""+participant.getJSONObject("properties").getString("name")+"\">";
					result += "\t\t<firstName>"+participant.getJSONObject("properties").getString("name")+"</firstName>\n";
					result += "\t\t<lastName>"+participant.getJSONObject("properties").getString("name")+"</lastName>\n";
					result += "\t\t<manager>"+participant.getJSONObject("properties").getString("manager")+"</manager>\n";
					result += "\t\t<personalData/>\n";
					result += "\t\t<professionalData/>\n";
					result += "\t\t<customUserInfoValues/>\n";
					result += "\t\t<password encrypted=\"false\">bpm</password>\n";
					result += "\t</user>\n";
					
				}
				
			}
		} catch (JSONException e) {
		}
		
		return result;
	}
	
	private String getRoles(){
		String result = "";
		
		try {
		
			JSONArray users = this.getResourcesByStencilId(this.oryxStencil, "resourceClassifier");

			for(int i=0; i<users.length(); i++){
				JSONObject participant = users.getJSONObject(i);
				if(participant.getJSONObject("properties").getString("definition").equals("Role")){
					
					result += "\t<role name=\""+participant.getJSONObject("properties").getString("name")+"\">\n";
					result += "\t\t<displayName>"+participant.getJSONObject("properties").getString("name")+"</displayName>\n";
					result += "\t</role>\n";
					
				}
				
			}
		} catch (JSONException e) {
		}
		
		return result;
	}
	
	private String getGroups(){
		String result = "";
		
		try {
		
			JSONArray users = this.getResourcesByStencilId(this.oryxStencil, "resourceClassifier");

			for(int i=0; i<users.length(); i++){
				JSONObject participant = users.getJSONObject(i);
				if(participant.getJSONObject("properties").getString("definition").equals("Group")){
					
					result += "\t<group name=\""+participant.getJSONObject("properties").getString("name")+"\">\n";
					result += "\t\t<displayName>"+participant.getJSONObject("properties").getString("name")+"</displayName>\n";
					result += "\t</group>\n";
					
				}
				
			}
		} catch (JSONException e) {
		}
		
		return result;
	}
	
	private String getMemberships(){
		String result = "";
		
		try {
		
			JSONArray memberships = this.getResourcesByStencilId(this.oryxStencil, "resourceClassification");

			for(int i=0; i<memberships.length(); i++){
				JSONObject membership = memberships.getJSONObject(i);
				if(membership.getJSONObject("properties").getString("definition").equals("Membership")){
					
					result += "\t<membership>\n";
					result += "\t\t<userName>"+this.getResourcesByStencilId(oryxStencil, membership.getJSONArray("incoming").
												getJSONObject(0).getJSONObject("properties").getString("id")).
												getJSONObject(0).getJSONObject("properties").getString("name")+"" +
							  "</userName>\n";
					result += "<roleName>"+this.getResourcesByStencilId(oryxStencil, membership.getJSONArray("outgoing").
										   getJSONObject(0).getJSONObject("properties").getString("id")).
										   getJSONObject(0).getJSONObject("properties").getString("name")+"" +
							  "</roleName>\n";
					result += "\t</membership>\n";
					result += "<groupParentPath>/"+this.getResourcesByStencilId(oryxStencil, membership.getJSONArray("outgoing").
							   getJSONObject(0).getJSONObject("properties").getString("id")).
							   getJSONObject(0).getJSONObject("properties").getString("name")+
							   "</groupParentPath>";					
				}
				
			}
		} catch (JSONException e) {
		}
		
		return result;
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
	
	private JSONArray getClassifiers(JSONObject humanResource){
		JSONArray result = new JSONArray();
		
		JSONArray outgoing;
		try {
			outgoing = humanResource.getJSONArray("outgoing");
			
			for(int i=0; i<outgoing.length(); i++){
				JSONObject classification = this.getResourceById(this.oryxStencil, outgoing.getJSONObject(i).getString("resourceId"));
				JSONObject classifier = this.getResourceById(this.oryxStencil, classification.getJSONArray("outgoing").getJSONObject(0).getString("resourceId"));
				
				result.put(classifier);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
private JSONObject getResourceById(JSONObject jo, String resourceId){
		
		try {
			if(jo.getString("resourceId").equals(resourceId)){
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

}
