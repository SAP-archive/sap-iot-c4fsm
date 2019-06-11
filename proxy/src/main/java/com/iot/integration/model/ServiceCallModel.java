package com.iot.integration.model;

import java.util.ArrayList;
import java.util.List;

public class ServiceCallModel {
	List<String> equipments;

	public List<String> getEquipments() {
		if(equipments==null) {
			equipments= new ArrayList<String>();
		}
		return equipments;
	}

	public void setEquipements(List<String> equipments) {
		this.equipments = equipments;
	}
	

}
