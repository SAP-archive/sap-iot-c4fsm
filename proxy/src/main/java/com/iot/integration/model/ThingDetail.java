package com.iot.integration.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ThingDetail implements ODataEntity {

	private String thingId;
	private String thingType;
	
	public ThingDetail() {
		// TODO Auto-generated constructor stub
	}
	
	

	public String getThingId() {
		return thingId;
	}



	public void setThingId(String thingId) {
		this.thingId = thingId;
	}



	public String getThingType() {
		return thingType;
	}



	public void setThingType(String thingType) {
		this.thingType = thingType;
	}



	@Override
	public void extract(JsonObject objectJSON) {
		// TODO Auto-generated method stub
		setThingId(objectJSON.get("_id").getAsString());
		try {
			JsonArray thingTypes = objectJSON.get("_thingType").getAsJsonArray();
			if ( thingTypes.size() > 0 ) {
				setThingType(thingTypes.get(0).getAsString());
			}
		}catch(Exception ex) {
			
		}
	}

}
