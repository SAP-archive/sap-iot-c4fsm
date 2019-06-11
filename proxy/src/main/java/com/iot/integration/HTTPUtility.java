package com.iot.integration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iot.integration.model.ODataEntity;
import com.iot.integration.odata.ODataRequest;

public class HTTPUtility {

	public HTTPUtility() {
		// TODO Auto-generated constructor stub
	}
	
	public static HttpResponse handleHTTPGet(String url,HttpParams queryParams, List<BasicNameValuePair> headers) throws ClientProtocolException, IOException, URISyntaxException {
		// System.out.println("Accessing URL :"+url);
		URIBuilder uriBuilder = new URIBuilder(url);
		
		if ( queryParams != null ) {
			BasicHttpParams basicParams = (BasicHttpParams)queryParams;
			if ( basicParams != null ) {
				Set<String> names = basicParams.getNames();
				if ( names != null ) {
					for ( String pName : names ) {
						uriBuilder.setParameter(pName, (String)basicParams.getParameter(pName));	
					}
				}
			}
		}

		HttpGet get = new HttpGet(uriBuilder.build());
		for ( BasicNameValuePair header : headers ) {
			get.setHeader(header.getName(), header.getValue());
		}
		
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(get);
		int code = response.getStatusLine().getStatusCode();
		if (code == 401 ) {
			System.out
					.println("Authorization server expects Basic authentication");
		}
		return response;
	}

	public static HttpResponse handleHTTPPost(String url,HttpEntity requestBody,HttpParams queryParams,List<BasicNameValuePair> headers) throws ClientProtocolException, IOException, URISyntaxException {
		
		URIBuilder uriBuilder = new URIBuilder(url);
		
		if ( queryParams != null ) {
			BasicHttpParams basicParams = (BasicHttpParams)queryParams;
			if ( basicParams != null ) {
				Set<String> names = basicParams.getNames();
				if ( names != null ) {
					for ( String pName : names ) {
						uriBuilder.setParameter(pName, (String)basicParams.getParameter(pName));	
					}
				}
			}
		}
		HttpPost post = new HttpPost(uriBuilder.build());
		for ( BasicNameValuePair header : headers ) {
			post.setHeader(header.getName(), header.getValue());
		}

		post.setEntity(requestBody);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(post);
		int code = response.getStatusLine().getStatusCode();
		if (code == 401 ) {
			System.out
					.println("Authorization server expects Basic authentication");
		}
		return response;
	}
	
	public static JsonObject jsonResponse(HttpResponse response) {
		JsonObject json = null;
		try {
			json = jsonElementResponse(response).getAsJsonObject();
		}catch(Exception ex ) {
			ex.printStackTrace();
		}
	    return json;
	}
	
	public static JsonElement jsonElementResponse(HttpResponse response) {
		JsonElement je = null;
		String responseString = null;
		try {
			HttpEntity responseEntity = response.getEntity();
			responseString = EntityUtils.toString(responseEntity);
			je = new JsonParser().parse(responseString);
		}catch(Exception ex ) {
			// System.out.println("Response : "+ responseString);
			ex.printStackTrace();
		}
	    return je;
	}
	

	public static <T> List<T> executeODataRead(ODataRequest request,Class<T> objectTypeClass) {
		return executeODataRead(request, objectTypeClass,null);
	}
	
	public static <T> List<T> executeODataRead(ODataRequest request,Class<T> objectTypeClass,String responsePath) {
		List<T> objects = new ArrayList<>();
		try {
			
			String url = request.getRequestString();
		
			List<BasicNameValuePair> headers = new ArrayList<>();
			headers.add(new BasicNameValuePair(request.getAuthorizationHeaderName(), "Bearer " +request.getToken()));
			
			HttpResponse response = HTTPUtility.handleHTTPGet(url, null, headers);
			JsonElement json = HTTPUtility.jsonElementResponse(response);

			if ( responsePath == null ) {
				responsePath = "d.results";
			}
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200){
				if (statusCode == 400 ) {
					System.out.println("Bad request - Status Code : "+statusCode + " > for URL:" +url);
				}else if (statusCode == 401 ) {
					System.out.println("Unauthorized - Status Code : "+ statusCode + " > for URL:" +url);
				}else if (statusCode == 403 ) {
					System.out.println("Forbidden - Status Code : "+statusCode + " > for URL:" +url);
				}else if (statusCode == 500 ) {
					System.out.println("Internal server error - Status Code : "+statusCode + " > for URL:" +url);
				}else{
					System.out.println("Bad request - Status Code : "+statusCode + " > for URL:" +url);
				}
				// System.out.println(objectTypeClass);
				return objects;
			}
			String[] responsePathItems = responsePath.split("\\.");
						
			JsonElement jsonElement = json;
			if (responsePath != null && !StringUtils.trimAllWhitespace(responsePath).equalsIgnoreCase("")) {
				for ( String pathItem: responsePathItems ) {
					jsonElement = jsonElement.getAsJsonObject().get(pathItem);
				}
			}
			if ( jsonElement != null ) {
				if ( jsonElement.isJsonArray() ) {
					JsonArray objArray = jsonElement.getAsJsonArray();
					
					Iterator<JsonElement> iterator = objArray.iterator();
					
					while (iterator != null && iterator.hasNext()) {
						JsonObject objectJSON = iterator.next().getAsJsonObject();
						T obj = objectTypeClass.newInstance();
						((ODataEntity)obj).extract(objectJSON);
						objects.add(obj);
					}
				} else if ( jsonElement.isJsonObject() ) {
					JsonObject objectJSON = jsonElement.getAsJsonObject();
					T obj = objectTypeClass.newInstance();
					((ODataEntity)obj).extract(objectJSON);
					objects.add(obj);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return objects;
	}
}
