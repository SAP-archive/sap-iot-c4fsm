/*
/*
 * The integration developer needs to create the method processData 
 * This method takes Message object of package com.sap.gateway.ip.core.customdev.util
 * which includes helper methods useful for the content developer:
 * 
 * The methods available are:
    public java.lang.Object getBody()
    
    //This method helps User to retrieve message body as specific type ( InputStream , String , byte[] ) - e.g. message.getBody(java.io.InputStream)
    public java.lang.Object getBody(java.lang.String fullyQualifiedClassName)

    public void setBody(java.lang.Object exchangeBody)

    public java.util.Map<java.lang.String,java.lang.Object> getHeaders()

    public void setHeaders(java.util.Map<java.lang.String,java.lang.Object> exchangeHeaders)

    public void setHeader(java.lang.String name, java.lang.Object value)

    public java.util.Map<java.lang.String,java.lang.Object> getProperties()

    public void setProperties(java.util.Map<java.lang.String,java.lang.Object> exchangeProperties) 

 * 
 */

importClass(com.sap.gateway.ip.core.customdev.util.Message);
importClass(java.util.HashMap);

 
function processData(message) {
	var body = message.getBody( new java.lang.String().getClass() );
	var bodyJSON = JSON.parse(body);
	var equipment_id = "";
	var problemType = "";
	var inputPayload = "";
	equipment_id = bodyJSON.serviceCall.equipments[0].id;
	problemType = bodyJSON.serviceCall.problemType;
	inputPayload = JSON.stringify(bodyJSON);
	
    var headers = message.getHeaders();
    var map = headers.get("CamelHttpQuery");
    var headers_map = parseParms(map);
  
// message.setHeader("account",headers_map.get("account") );
    
    message.setHeader("account",headers_map.account);
    message.setHeader("company",headers_map.company);
    message.setHeader("user",headers_map.user);
    
	message.setProperty("equipment_id",equipment_id);
	message.setProperty("problemType",problemType);
	message.setProperty("inputPayload",inputPayload);
	//message.setProperty("map",map);
	return message;

}

    function parseParms(str) {
    var pieces = str.split("&"), data = {}, i, parts;
    // process each query pair
    for (i = 0; i < pieces.length; i++) {
        parts = pieces[i].split("=");
        if (parts.length < 2) {
            parts.push("");
        }
        data[decodeURIComponent(parts[0])] = decodeURIComponent(parts[1]);
    }
    return data;
}
