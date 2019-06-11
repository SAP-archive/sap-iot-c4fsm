package com.iot.integration.model;

import com.google.gson.JsonObject;

public class Destination {

	private String name;
	private String url;
	private boolean forwardToken;
	
	public Destination(String name, String url, boolean forwardToken) {
		super();
		this.name = name;
		this.url = url;
		this.forwardToken = forwardToken;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public boolean isForwardToken() {
		return forwardToken;
	}

	public static Destination fromJSON(JsonObject obj) {
		String name = obj.get("name").getAsString();
		String url = obj.get("url").getAsString();
		boolean forwardToken = obj.get("forwardAuthToken").getAsBoolean();
		Destination destination = new Destination(name, url, forwardToken);
		return destination;
	}
}
