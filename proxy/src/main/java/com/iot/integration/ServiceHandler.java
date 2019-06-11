package com.iot.integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iot.integration.model.Destination;
import com.iot.integration.model.EquipmentModel;
import com.iot.integration.model.ThingDetail;
import com.iot.integration.model.WebContainerModel;

import ch.qos.logback.classic.pattern.Util;

@RestController
public class ServiceHandler {

	@Value("${redirectUrl}")
	String url;

	@RequestMapping(value = "/iotData", method = RequestMethod.POST)
	public void redirectToFlpWithToken(HttpServletRequest request,
			@RequestBody WebContainerModel requestModel, HttpServletResponse httpServletResponse) throws IOException {
		HttpResponse response;
		try {

			 String ui5Version = request.getHeader("sap-ui-version");

			JsonObject fsmCredentials = Utils.getServiceDetails("scenario.com.sap.leonardo.iot.fsm",true);

			if ( fsmCredentials == null ) {
				throw new Exception("FSM User Provided Service Instance not bound.");
			}
			
			boolean flag = Utils.validateFSMDetails(requestModel,fsmCredentials);
			
			if ( !flag ) {
				System.out.println("FSM System Details are invalid");
				httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
				httpServletResponse.getWriter().write("FSM System Details are invalid");
				httpServletResponse.getWriter().flush();
				httpServletResponse.getWriter().close();
			}
			
			StringBuilder queryUrl = new StringBuilder(
					requestModel.getDataCloudFullQualifiedDomainName().replace("\\/\\/", "//"));
			
			String queryEndpoint = null;
			
			try {
				queryEndpoint = fsmCredentials.get("endpoints").getAsJsonObject().get("query").getAsString();	
			} catch (Exception e) {
				throw new Exception("Invalid endpoint configuration in FSM User Provided Service Instance.");
			}
			
			
			queryUrl.append(queryEndpoint+"?");
			//System.out.println(queryUrl);
			queryUrl.append("account=" + requestModel.getCloudAccount() + "&");
			queryUrl.append("user=" + requestModel.getUserName() + "&");
			queryUrl.append("company=" + requestModel.getCompanyName() + "&dtos=Equipment.17;ServiceCall.17");
			StringBuilder body = new StringBuilder(
					"{\"query\": \"SELECT sc.equipments FROM ServiceCall sc WHERE sc.id = ");

			body.append("'" + requestModel.getCloudId() + "'  \"}");
			//System.out.println("Body : " + body);
			//System.out.println("QueryUrl: " + queryUrl);
			List<BasicNameValuePair> headers = new ArrayList<BasicNameValuePair>();

			headers.add(new BasicNameValuePair("Content-Type", "application/json"));
			headers.add(new BasicNameValuePair("Authorization", "Bearer " + requestModel.getAuthToken()));
			headers.add(new BasicNameValuePair("X-Client-Version", "1.0"));
			headers.add(new BasicNameValuePair("X-Client-ID", requestModel.getCloudId()));
			
			BasicHttpParams httpParams = new BasicHttpParams();
			HttpEntity entity = new StringEntity(body.toString());
			response = HTTPUtility.handleHTTPPost(queryUrl.toString(), entity, httpParams, headers);
			
			if (response.getStatusLine().getStatusCode() == 200) {
				JsonObject responseJson = HTTPUtility.jsonResponse(response);
				JsonArray array = responseJson.getAsJsonArray("data");
				if (array.size() == 1) {
					EquipmentModel model = new Gson().fromJson(array.get(0), EquipmentModel.class);
					if (model.getSc() != null && model.getSc().getEquipments().size() > 0) {
						// System.out.println("Equipment Id: " + model.getSc().getEquipments().get(0));
						String clientSecret = requestModel.getAuthenticationKey();
						String token = Utils.getAccessToken(clientSecret);
						// System.out.println("Token " + token);
						if (token != null) {
							ThingDetail thing = Utils.getThingDetailsFromEquipment(model.getSc().getEquipments().get(0),
									token);
							// System.out.println("thing " + thing);

							if (thing != null) {
								
								String appId = Utils.getAppId(thing.getThingType());
								
								if ( appId == null || appId.isEmpty() ) {
									httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
									httpServletResponse.getWriter().write("No Application has been not registered for "+thing.getThingType());
									httpServletResponse.getWriter().flush();
									httpServletResponse.getWriter().close();
									return;
								}
								
								//System.out.println("Thing Type :"+thing.getThingType());
								//System.out.println("appId :"+appId);
								Destination destination = Utils.getDestination("approuter");
								
								httpParams = new BasicHttpParams();
								httpParams.setParameter("appId", appId);
								headers = new ArrayList<BasicNameValuePair>();
								headers.add(new BasicNameValuePair("Authorization", "Bearer " + token));
								response = HTTPUtility.handleHTTPGet(destination.getUrl()+"/session", httpParams, headers);
								JsonObject sessionDetails = HTTPUtility.jsonResponse(response);
								String sessionKey = sessionDetails.get("sessionKey").getAsString();
								String redirectionURL = destination.getUrl()+"/cp.portal?sap-ui-app-id-hint="+appId+"&thingType="+thing.getThingType()+"&thingId="+thing.getThingId()+"&session="+sessionKey;

								if ( ui5Version != null && !ui5Version.isEmpty() ) {
									redirectionURL += "&sap-ui-version="+ ui5Version;
								}
								//System.out.println("Redirect Url : " + redirectionURL);

								httpServletResponse.setHeader("Location", redirectionURL);
								httpServletResponse.setStatus(302);
							} else {
								// System.out.println("thing not found");
								httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
								httpServletResponse.getWriter().write("Selected Equipment is not assigned to a Thing");
								httpServletResponse.getWriter().flush();
								httpServletResponse.getWriter().close();
							}
						} else {
							// System.out.println("Invalid authorization context");
							httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
							httpServletResponse.getWriter().write("Invalid Authorization Key");
							httpServletResponse.getWriter().flush();
							httpServletResponse.getWriter().close();
						}
					} else {
						// System.out.println("No Equipment found for the Service Ticket");

						httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
						httpServletResponse.getWriter().write("No Equipment found for the Service Ticket");
						httpServletResponse.getWriter().flush();
						httpServletResponse.getWriter().close();

					}
				} else {
					// System.out.println("More than one service tickets are not allowed");
					httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					httpServletResponse.getWriter().write("More than one service tickets are not allowed");
					httpServletResponse.getWriter().flush();
					httpServletResponse.getWriter().close();

				}
			} else {
				// System.out.println("FSM api call failed");
				httpServletResponse.setStatus(response.getStatusLine().getStatusCode());
				HttpEntity responseEntity = response.getEntity();
				String content = EntityUtils.toString(responseEntity);
				httpServletResponse.getWriter().write(content);
				httpServletResponse.getWriter().flush();
				httpServletResponse.getWriter().close();

			}
		}

		catch (Exception e) {
			// System.out.println("Error "+e.getMessage());
			e.printStackTrace();
			httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			httpServletResponse.getWriter().write(e.getMessage());
			httpServletResponse.getWriter().flush();
			httpServletResponse.getWriter().close();

		}

	}
	
	
	@RequestMapping(value = { "/IOTAS-DETAILS-THING-ODATA/**"}, method = RequestMethod.GET)
	public void handleIoTProxy(HttpServletRequest request, HttpServletResponse response)
	throws IOException {
		handleInternalProxy(request,response,"details-thing-sap");
	}
	
	@RequestMapping(value = { "/sap/fiori/sapsmartbusiness/sap/**"}, method = RequestMethod.GET)
	public void handleSsbRuntimeProxy(HttpServletRequest request, HttpServletResponse response)
	throws IOException {
		handleInternalProxy(request,response,"ssbruntime-sap");
	}
	
	@RequestMapping(value = { "/destinations/{name}/**","/sap/fiori/sapsmartbusiness/destinations/{name}/**" }, method = RequestMethod.GET)
	public void handleProxy(HttpServletRequest request, HttpServletResponse response, @PathVariable("name") String name)
	throws IOException {
		handleInternalProxy(request,response,name);
		
	}
	
	public void handleInternalProxy(HttpServletRequest request, HttpServletResponse response,String name)
			throws IOException {

		ResourceUrlProvider urlProvider = (ResourceUrlProvider) request
				.getAttribute(ResourceUrlProvider.class.getCanonicalName());
		String relativeURL = urlProvider.getPathMatcher().extractPathWithinPattern(
				String.valueOf(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)),
				String.valueOf(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)));

		Destination destination = Utils.getDestination(name);
		String destinationHost = destination.getUrl();

		if (destinationHost == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().write("Destination :" + name + " not found");
			response.flushBuffer();
			return;
		}

		StringBuilder builder = new StringBuilder();
		builder.append(destinationHost);
		builder.append("/");
		builder.append(relativeURL);

		System.out.println("Path :" + relativeURL);

		Map<String, String[]> parameters = request.getParameterMap();

		if (parameters.size() > 0) {
			builder.append("?");
		}

		boolean hasMore = false;
		for (String key : parameters.keySet()) {
			if (hasMore) {
				builder.append("&");
			}
			builder.append(key);
			builder.append("=");

			boolean evenMore = false;
			for (String value : parameters.get(key)) {
				if (evenMore) {
					builder.append(",");
				}
				builder.append(URLEncoder.encode(value, "UTF-8"));
				evenMore = true;
			}
			hasMore = true;
		}

		String fullURL = builder.toString();

		System.out.println("Accessing URL :" + fullURL);
		HttpGet get = new HttpGet(fullURL);

		if (destination.isForwardToken())
			get.setHeader("Authorization", request.getHeader("authorization"));

		if (request.getHeader("sap-iot-eventtype") != null) {
			get.setHeader("sap-iot-eventtype", request.getHeader("sap-iot-eventtype"));
		}

		if (request.getHeader("sap-iot-pst") != null) {
			get.setHeader("sap-iot-pst", request.getHeader("sap-iot-pst"));
		}

		if (request.getHeader("Accept") != null) {
			get.setHeader("Accept", request.getHeader("Accept"));
		}

		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse result = client.execute(get);

		String responseString = EntityUtils.toString(result.getEntity());

		ByteArrayOutputStream io = new ByteArrayOutputStream();
		io.write(responseString.getBytes());
		response.setContentLength(io.size());
		response.setContentType(result.getEntity().getContentType().getValue());

		ServletOutputStream responseStream = response.getOutputStream();
		io.writeTo(responseStream);

		responseStream.flush();
	}

	public static JsonElement jsonElementResponse(HttpResponse response) {
		JsonElement je = null;
		String responseString = null;
		try {
			org.apache.http.HttpEntity responseEntity = response.getEntity();
			responseString = EntityUtils.toString(responseEntity);
			je = new JsonParser().parse(responseString);
		} catch (Exception ex) {
			System.out.println("Response : " + responseString);
			ex.printStackTrace();
		}
		return je;
	}

//	@RequestMapping(path="/test8", method=RequestMethod.GET )
//	public void test8(HttpServletRequest request , HttpServletResponse response) {
//		 try {
//			request.getRequestDispatcher("https://www.w3schools.in/").forward(request, response);
//		} catch (ServletException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		 return;
//	}
}
