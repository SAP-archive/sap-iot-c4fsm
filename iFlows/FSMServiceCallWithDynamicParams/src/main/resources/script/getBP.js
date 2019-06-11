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
	var business_partner = "";
	var equipment_id = "";
	//var modifiedPayload = "";
	var now = new Date();
    var dueDate = (new Date(now.setDate(now.getDate() + 1))).toISOString();
    
	business_partner = bodyJSON.data[0].equipment.businessPartner;
	equipment_id = bodyJSON.data[0].equipment.id;
	
	message.setProperty("business_partner",business_partner);
	message.setProperty("equipment_id",equipment_id);
	var modifiedPayload = message.getProperty( "inputPayload" );
	var payloadJSON = JSON.parse(modifiedPayload);
	
	//var modifiedPayload =(message.getProperty("inputPayload"));//JSON.parse(message.getProperty("inputPayload"));
	
	payloadJSON.serviceCall.businessPartner = {id: business_partner}; //"{\"id\" : \"" + business_partner + "\"}"; { name: "John", age: 30, city: "New York" };
	payloadJSON.serviceCall.dueDateTime = dueDate;
	var bodyOutput = JSON.stringify(payloadJSON);
	message.setProperty("modifiedPayload",bodyOutput);
	
	return message;
}
