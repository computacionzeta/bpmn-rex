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
 * This servlet provides the access point to the BPMN 2.0 + WD Extension to YAWL Transformation
 * 
 * @author Luis Stroppi
 * 
 */
public class BPMN2_0RSToYAWL extends HttpServlet {
	
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
			spec += "<orgdata>\n";
			spec += "\t<participants>\n";
			spec += this.getParticipants()+"\n";
			spec += "\t</participants>\n";
			spec += "\t<roles>\n";
			spec += this.getRoles()+"\n";
			spec += "\t</roles>\n";
			spec += "\t<positions>\n";
			spec += this.getPositions()+"\n";
			spec += "\t</positions>\n";
			spec += "\t<capabilities>\n";
			spec += this.getCapabilities()+"\n";
			spec += "\t</capabilities>\n";
			spec += "\t<orggroups>\n";
			spec += this.getOrgGroups()+"\n";
			spec += "\t</orggroups>\n";
			spec += "</orgdata>\n";
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return spec;
	}
	
	private String getParticipants(){
		String result = "";
		
		try {
		
			JSONArray participants = this.getResourcesByStencilId(this.oryxStencil, "humanResource");

			for(int i=0; i<participants.length(); i++){
				JSONObject participant = participants.getJSONObject(i);
				if(participant.getJSONObject("properties").getString("definition").equals("Participant")){
					JSONArray classifiers = this.getClassifiers(participant);
					JSONArray children	  = participant.getJSONArray("childShapes");
					String partId = participant.getString("resourceId").replace("oryx_", "PA-").replace("_", "-");
					
					result += "\t<participant id=\""+partId+"\">\n";
					result += "\t\t<userid>"+participant.getJSONObject("properties").getString("name")+"</userid>\n";
					result += "\t\t<password>"+this.getPassword(participant.getJSONObject("properties").getString("name"))+"</password>\n";
					result += "\t\t<firstname>"+participant.getJSONObject("properties").getString("name")+"</firstname>\n";
					result += "\t\t<lastname />\n";
					result += "\t\t<description></description>\n";
					result += "\t\t<notes />\n";
					result += "\t\t<isAdministrator>false</isAdministrator>\n";
					result += "\t\t<isAvailable>true</isAvailable>\n";
					result += "\t\t<roles>\n";
					for(int j=0; j<classifiers.length(); j++){
						JSONObject role = classifiers.getJSONObject(j);
						if(role.getJSONObject("properties").getString("definition").equals("Role")){
							String id = role.getString("resourceId").replace("oryx_", "RO-").replace("_", "-");
							result += "\t\t\t<role>"+id+"</role>\n";
						}
					}
					result += "\t\t</roles>\n";
					result += "\t\t<positions>\n";
					for(int j=0; j<classifiers.length(); j++){
						JSONObject position = classifiers.getJSONObject(j);
						if(position.getJSONObject("properties").getString("definition").equals("Position")){
							String id = position.getString("resourceId").replace("oryx_", "PO-").replace("_", "-");
							result += "\t\t\t<position>"+id+"</position>\n";
						}
					}
					result += "\t\t</positions>\n";
					result += "\t\t<capabilities>\n";
					for(int j=0; j<children.length(); j++){
						JSONObject capability = children.getJSONObject(j);
						if(capability.getJSONObject("properties").getString("definition").equals("Capability")){
							String id = capability.getString("resourceId").replace("oryx_", "CA-").replace("_", "-");
							result += "\t\t\t<capability>"+id+"</capability>\n";
						}
					}
					result += "\t\t</capabilities>\n";
					result += "\t\t<privileges>"+this.getChoose(participant)+"0"+this.getReorder(participant)+"0"+this.getViewGroup(participant)+"00</privileges>\n";
					result += "\t</participant>\n";
				}
				
			}
		} catch (JSONException e) {
		}
		
		return result;
	}
	
	private String getPassword(String password){
		try {
            MessageDigest digest = MessageDigest.getInstance("SHA");
            byte[] cryptedPW = digest.digest(password.getBytes());
            return Base64.encode(cryptedPW);
        } catch (Exception e) {
        }
        return null;
	}
	
	private String getRoles(){
		String result = "";
		
		try {
		
			JSONArray roles = this.getResourcesByStencilId(this.oryxStencil, "resourceClassifier");

			for(int i=0; i<roles.length(); i++){
				JSONObject role = roles.getJSONObject(i);
				
				if(role.getJSONObject("properties").getString("definition").equals("Role")){
					String id = role.getString("resourceId").replace("oryx_", "RO-").replace("_", "-");
					result += "<role id=\""+id+"\">\n";
					result += "<name>"+role.getJSONObject("properties").getString("name")+"</name>\n";
					result += "<description>"+role.getJSONObject("properties").getString("description")+"</description>\n";
					result += "<notes />\n";
					result += "</role>\n";
				}
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private String getPositions(){
		String result = "";
		
		try {
		
			JSONArray positions = this.getResourcesByStencilId(this.oryxStencil, "resourceClassifier");

			for(int i=0; i<positions.length(); i++){
				JSONObject position = positions.getJSONObject(i);
				
				if(position.getJSONObject("properties").getString("definition").equals("Position")){
					String id = position.getString("resourceId").replace("oryx_", "PO-").replace("_", "-");
					result += "\t<position id=\""+id+"\">\n";
					result += "\t\t<title>"+position.getJSONObject("properties").getString("name")+"</title>\n";
					result += "\t\t<positionid />\n";
					result += "\t\t<description>"+position.getJSONObject("properties").getString("description")+"</description>\n";
					result += "\t\t<notes />\n";
					result += "\t</position>\n";
		
				}
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private String getCapabilities(){
		String result = "";
		
		try {
		
			JSONArray capabilities = this.getResourcesByStencilId(this.oryxStencil, "resourceParameter");

			for(int i=0; i<capabilities.length(); i++){
				JSONObject capability = capabilities.getJSONObject(i);
				
				if(capability.getJSONObject("properties").getString("definition").equals("Capability")){
					String id = capability.getString("resourceId").replace("oryx_", "CA-").replace("_", "-");		
					
					result += "\t<capability id=\""+id+"\">\n";
					result += "\t\t<name>"+capability.getJSONObject("properties").getString("name")+"</name>\n";
					result += "\t\t<description></description>\n";
					result += "\t\t<notes />\n";
					result += "\t</capability>\n";
				}
				
			}
		} catch (JSONException e) {
		}
		
		return result;
	}
	
	private String getOrgGroups(){
		String result = "";
		
		try {
			
			JSONArray orgGroups = this.getResourcesByStencilId(this.oryxStencil, "resourceClassifier");

			for(int i=0; i<orgGroups.length(); i++){
				JSONObject group = orgGroups.getJSONObject(i);
				
				if(group.getJSONObject("properties").getString("definition").equals("OrgGroup")){
					String id = group.getString("resourceId").replace("oryx_", "OG-").replace("_", "-");
		
					result += "\t<orggroup id=\""+id+"\">\n";
					result += "\t\t<groupName>"+group.getJSONObject("properties").getString("name")+"</groupName>\n";
					result += "\t\t<groupType>GROUP</groupType>\n";
					result += "\t\t<description>"+group.getJSONObject("properties").getString("description")+"</description>\n";
					result += "\t\t<notes />\n";
					result += "\t</orggroup>\n";
					
				}
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
	
	private String getChoose(JSONObject participant){
		String result = "0";
		
		try {
			JSONArray privileges = participant.getJSONObject("properties").getJSONObject("privileges").getJSONArray("items");
			
			for(int i=0; i<privileges.length(); i++){
				if(privileges.getJSONObject(i).getString("privilege").equals("Choose")){
					result = "1";
				}				
			}
		} catch (JSONException e) {
		}
		
		return result;
	}
	
	private String getReorder(JSONObject participant){
		String result = "0";
		
		try {
			JSONArray privileges = participant.getJSONObject("properties").getJSONObject("privileges").getJSONArray("items");
			
			for(int i=0; i<privileges.length(); i++){
				if(privileges.getJSONObject(i).getString("privilege").equals("Reorder")){
					result = "1";
				}				
			}
		} catch (JSONException e) {
		}
		
		return result;
	}
	
	private String getViewGroup(JSONObject participant){
		String result = "0";
		
		try {
			JSONArray privileges = participant.getJSONObject("properties").getJSONObject("privileges").getJSONArray("items");
			
			for(int i=0; i<privileges.length(); i++){
				if(privileges.getJSONObject(i).getString("privilege").equals("ViewGroup")){
					result = "1";
				}				
			}
		} catch (JSONException e) {
		}
		
		return result;
	}
}
