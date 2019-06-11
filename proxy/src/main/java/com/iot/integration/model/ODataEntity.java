package com.iot.integration.model;

import com.google.gson.JsonObject;

public interface ODataEntity {
	void extract(JsonObject objectJSON);
}
