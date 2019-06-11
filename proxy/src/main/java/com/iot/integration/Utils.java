package com.iot.integration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iot.integration.model.Destination;
import com.iot.integration.model.ThingDetail;
import com.iot.integration.model.TokenModel;
import com.iot.integration.model.Token;
import com.iot.integration.model.WebContainerModel;
import com.iot.integration.odata.ODataRequest;


public class Utils {

	private static Map<String, Destination> destinations;
	private static Map<String, String> applications;
	private static JsonObject tokenDetails;
	private static Map<String, String> localEnvironment;

	private static boolean localAccess = false;
	
	private static void readEnvironment() {
		
		if ( destinations == null ) {
			JsonArray jsonArray = null;
			JsonParser jsonParser = new JsonParser();
	
			if(getEnvironment("destinations") != null) {
				jsonArray = jsonParser.parse(getEnvironment("destinations")).getAsJsonArray();
				
				Iterator<JsonElement> iterator = jsonArray.iterator();
				destinations = new HashMap<String, Destination>();
				while(iterator.hasNext()) {
					JsonObject json = iterator.next().getAsJsonObject();
					String key = json.get("name").getAsString();
					//JsonObject value = json.get("value").getAsJsonObject();
					destinations.put(key, Destination.fromJSON(json));
				}
			}
			
			if(getEnvironment("HTML5_APPLICATIONS") != null) {
				jsonArray = jsonParser.parse(getEnvironment("HTML5_APPLICATIONS")).getAsJsonArray();
				
				Iterator<JsonElement> iterator = jsonArray.iterator();
				applications = new HashMap<String, String>();
				while(iterator.hasNext()) {
					JsonObject json = iterator.next().getAsJsonObject();
					String key = json.get("thingType").getAsString();
					String value = json.get("appId").getAsString();
					applications.put(key, value);
				}
			}
			
			if(getEnvironment("uaa") != null) {
				tokenDetails = jsonParser.parse(getEnvironment("uaa")).getAsJsonObject();
			}
			
		}
	}
	
	public static JsonObject getServiceDetails(String serviceName,boolean userProvidedService) {
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = jsonParser.parse(getEnvironment("VCAP_SERVICES")).getAsJsonObject();
		Set<String> serviceInstances = jsonObject.keySet();
		
		for ( String instanceName : serviceInstances ) {
			if ( (userProvidedService && instanceName.equalsIgnoreCase("user-provided")) || ( !userProvidedService && !instanceName.equalsIgnoreCase("user-provided") ) ) {
				JsonArray instancesObj = jsonObject.get(instanceName).getAsJsonArray();
				
				for ( int  i = 0; i <instancesObj.size(); i++ ) {
					JsonObject instanceObj = instancesObj.get(i).getAsJsonObject();
					
					if ( instanceObj.has("credentials") ) {
						JsonObject credentials = instanceObj.get("credentials").getAsJsonObject();
						
						if ( credentials.has("sap.cloud.service") && credentials.get("sap.cloud.service").getAsString().equalsIgnoreCase(serviceName)) {
							return credentials;
						}
					}
					
				}
			}
		}
		return null;
	}
	
	private static JsonElement getEnvironmentObject(String name) {
		JsonParser jsonParser = new JsonParser();
		String myEnvironment = getEnvironment(name);
		return jsonParser.parse(myEnvironment);
	}
	
	private static String getEnvironment(String name) {
		if ( localAccess ) {
			return null;
		} else {
			return System.getenv(name);
		}
	}

	public static Destination getDestination(String destinationName) {
		readEnvironment();
		if ( destinations != null ) {
			return destinations.get(destinationName);
		}
		return null;
	}
	
	public static String getApplication(String applicationName) {
		readEnvironment();
		if ( applications != null ) {
			return applications.get(applicationName);
		}
		return null;
	}
	
	public static String getAppId(String thingType) {
		return getApplication(thingType);
	}
	
	public static boolean validateFSMDetails(WebContainerModel model,JsonObject fsmCredentials) throws Exception {
		//System.out.println("FSM Credentials : "+fsmCredentials.toString());
		if ( model.getAuthenticationKey() == null || model.getAuthenticationKey().equalsIgnoreCase("") )
			return false;
		
		if ( model.getAuthToken() == null || model.getAuthToken().equalsIgnoreCase("") )
			return false;
		

		
		if ( !model.getCloudAccount().equalsIgnoreCase(fsmCredentials.get("cloudAccount").getAsString())) {
			return false;
		}
		
		if ( !model.getUserName().equalsIgnoreCase(fsmCredentials.get("user").getAsString())) {
			return false;
		}
		
		if ( !model.getUserPassword().equalsIgnoreCase(fsmCredentials.get("password").getAsString())) {
			return false;
		}
		
		if ( !model.getCompanyName().equalsIgnoreCase(fsmCredentials.get("companyName").getAsString())) {
			return false;
		}
		return true;
	}
	
	public static String getAccessToken(String clientSecret) {
		readEnvironment();
		if ( clientSecret == null || clientSecret.equalsIgnoreCase("") ) {
			return null;
		}
		
		JsonObject uaaObj = tokenDetails;
		if ( uaaObj == null )
			return null;
		
		String tokenURL = uaaObj.get("url").getAsString()+"/oauth/token";
		String clientId = uaaObj.get("clientid").getAsString();
		
		String token = null;
		
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    
    MultiValueMap<String, String> map =
				   new LinkedMultiValueMap<String, String>();
			   map.add("client_id", clientId);
			   map.add("client_secret", clientSecret);
			   map.add("grant_type", "client_credentials");
			   map.add("response_type", "token");


			   HttpEntity<MultiValueMap<String, String>> request =
				   new HttpEntity<MultiValueMap<String, String>>(map, headers);

		ResponseEntity<TokenModel> responseToken = restTemplate.exchange(tokenURL , HttpMethod.POST,request , TokenModel.class );
		
		if(responseToken.getStatusCode()== HttpStatus.OK) {
			   token = responseToken.getBody().getAccess_token();
		}
		//System.out.println("-------Token : "+token);
		return token;
  	}
	
	public static ThingDetail getThingDetailsFromEquipment(String equipment,String token) {
		
		ODataRequest request = new ODataRequest();
		Destination destionationJson = getDestination("approuter");
		request.setUrl(destionationJson.getUrl()+"/com.sap.leonardo.iot/appiot-mds");
		request.setEntitySet("Things");
		request.setSelect("_id,_thingType");
		request.setTop("1");
		request.setFilter("_alternateId eq '"+equipment+"'");
		request.setFormat("json");
		request.setToken(token);
		request.setAuthorizationHeaderName("x-approuter-authorization");
		
		List<ThingDetail> things = HTTPUtility.executeODataRead(request, ThingDetail.class, "value");
		
		if (things != null && things.size() > 0 )
			return things.get(0);
		
		return null;
	}
	
}
