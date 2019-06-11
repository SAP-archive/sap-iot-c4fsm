## Leonardo IoT Artifacts

NOTE : In this document, "TENANT-PACKAGE-NAMESPACE" refers to the tenant specific package name space as explained [SAP Help Portal](https://help.sap.com/viewer/080fabc6cae6423fb45fca7752adb61e/1905a/en-US/462b49382316427aa59fe671a75fa39e.html).

### Package
	<<TENANT-PACKAGE-NAMESPACE>>.fsm.demo

### Property Set Types

|Name|DefaultImagePropertySet||||
|---|---|---|---|---|
|Full Name			|&lt;&lt;TENANT-PACKAGE-NAMESPACE&gt;&gt;.fsm.demo:DefaultImagePropertySet	||||
|Data Category | Basic Data||||
|Properties|||||
|Name|Description|Unit Of Measure|Type|Length|||
|DefaultImageProperty|Default IOT property for thing type images||String|127|



|Name|ROBOT_METADATA||||
|---|---|---|---|---|
|Full Name			|&lt;&lt;TENANT-PACKAGE-NAMESPACE&gt;&gt;.fsm.demo:ROBOT_METADATA			||||
|Data Category | Basic Data||||
|Properties|||||
|Name|Description|Unit Of Measure|Type|Length|||
|DEVICE_ID|Manufacturer Serial Number||String|127|
|MODEL_TYPE|Model Type||String|127|
|OPERATING_MODE|Operating Mode||String|127|


|Name|ROBOT_BATTERY_MAIN||||
|---|---|---|---|---|
|Full Name			|&lt;&lt;TENANT-PACKAGE-NAMESPACE&gt;&gt;.fsm.demo:ROBOT_BATTERY_MAIN		||||
|Data Category | Measured Values||||
|Properties|||||
|Name|Description|Unit Of Measure|Type|Length|||
|MODE|Battery Mode||Integer|127|
|VOLTAGE|Battery Voltage|V|Decimal Number|10,4|


|Name|ROBOT_TEMP_VIBRATION||||
|---|---|---|---|---|
|Full Name			|&lt;&lt;TENANT-PACKAGE-NAMESPACE&gt;&gt;.fsm.demo:ROBOT_TEMP_VIBRATION		||||
|Data Category | Measured Values||||
|Properties|||||
|Name|Description|Unit Of Measure|Type|Length|||
|ROBOT_TEMPERATURE|Temperature of robot|°C|Float||
|ROBOT_VIBRATION|Vibration of robot|HZ|Float||


### Thing Types

|Name|&lt;&lt;TENANT-PACKAGE-NAMESPACE&gt;&gt;.fsm.demo:Robot4000|
|----|----|
|Property Set|Property Set Type|
|DefaultImagePropertySet|&lt;&lt;TENANT-PACKAGE-NAMESPACE&gt;&gt;.fsm.demo:DefaultImagePropertySet|
|ROBOT_METADATA|&lt;&lt;TENANT-PACKAGE-NAMESPACE&gt;&gt;.fsm.demo:ROBOT_METADATA|
|ROBOT_BATTERY_MAIN|&lt;&lt;TENANT-PACKAGE-NAMESPACE&gt;&gt;.fsm.demo:ROBOT_BATTERY_MAIN|
|ROBOT_TEMP_VIBRATION|&lt;&lt;TENANT-PACKAGE-NAMESPACE&gt;&gt;.fsm.demo:ROBOT_TEMP_VIBRATION|

### Rule Contexts
	Robot_4000_Main_Battery

Similarly any Rule context can be created from the Rule Context tab which has all the properties of a single Property Set Type.

### Rule Definition 

|Name|Robot_4000_Low_Voltage_Rule|
|---|---|
|Rule Context|Robot_4000_Main_Battery|
|Rule Type|Scheduled|
|Condition|VOLTAGE_MIN of the ROBOT_BATTERY_MAIN_aggregate is less than 16|
|Windowing|10 minutes|
|Schedule|Every 10 Minutes|

### iFlow Definitions

Refer to [iFlow Definitions](01-iflow-setup.md) to setup iFlow definitions and obtain the iFlow Full URL.


### FSM Destination Configuration


A destination configuration to be added in the cloud foundry subaccount in order to auto create service call from Leonardo IoT.

A typical destination entry definition can be as per below.

```
Name : "FSM"
Description : "SAP Field Field Service Management"
URL : "<<IFLOW_FULLY_FORMED_URL>>"
Proxy Type : "Internet"
Authentication : "BasicAuthentication"
User : "<<CPI_USER>>"
Password : "<<CPI_PASSWORD>>"
```

Replace the place holders as explained below.

| Place holder Name                            | Description                                    |
|----------------------------------------------|------------------------------------------------|
| &lt;&lt;IFLOW_FULLY_FORMED_URL&gt;&gt;                              | Full url of iFlow endpoint                    |
| &lt;&lt;CPI_USER&gt;&gt;                                | Username of Cloud Platform Integration         |                                                   | &lt;&lt;CPI_PASSWORD&gt;&gt;                            | Password of Cloud Platform Integration     |

### Action Definition

|Name| Robot Voltage Low Service call|
|---|---|
|Triggered By|Event from Rule|
|Rule|Robot_4000_Low_Voltage_Rule|
|ThingType|&lt;&lt;TENANT-PACKAGE-NAMESPACE&gt;&gt;.fsm.demo:Robot4000|
|Type|HTTP|
|Destination|FSM|
|Invocation Type|Auto|
|Method|POST|
|Request Body|
```json
{
	"serviceCall": {
		"createPerson": {
			"id": "<<FSM_PERSON>>"
		},
		"equipments": [{
			"id": "${thing.alternateId}"
		}],
		"origin": "-4",
		"priority": "MEDIUM",
		"problemType": "-4",
		"remarks": "Recharge does not meet target voltage of 16V. Current voltage reading is ${ROBOT_BATTERY_MAIN.VOLTAGE}V. Replace main battery.",
		"resolution": null,
		"responsibles": [{ "id" :"<<FSM_RESPONSIBLE_PERSON>>"}],
		"status": "-5",
		"subject": "[Urgent]: Equipment ${thing.name} main battery is defective.The model is ${thing.description}" 
	}
}
```
|

Replace <<FSM_PERSON>> with a person id from FSM system,<<FSM_RESPONSIBLE_PERSON>> with a responsible person id from FSM,

NOTE: Refer to the [postman collection](04-postman-setup.md) to understand the iFlow HTTP Request details.

## IoT Service Artifacts

### Capabilities
|Name|ROBOT_BATTERY_MAIN_ROBOT4000||
|---|---|---|
|Properties|||
|Name|DataType|Unit Of Measure|
|VOLTAGE|Double|V|
|MODE|Integer||

|Name|ROBOT_TEMP_VIBRATION_ROBOT4000||
|---|---|---|
|Properties|||
|Name|DataType|Unit Of Measure|
|ROBOT_TEMPERATURE|Double|°C|
|ROBOT_VIBRATION|Double|HZ|

### Sensor Types

|Name|Robot4000|
|---|---|
|Capabilities||
|ROBOT_BATTERY_MAIN_ROBOT4000||
|ROBOT_TEMP_VIBRATION_ROBOT4000||

## Leonardo IoT & IoT Service Artifact Mappings

### Thing Type to Sensor Type Flexible Mapping

Create a Flexible mapping called "ROBOT4000_MAPPING" for thing Type "&lt;&lt;TENANT-PACKAGE-NAMESPACE&gt;&gt;.fsm.demo:Robot4000" as per below.

|Mapping Type|Named Property Set|Named Property Set Property|Sensor Type|Capability|Capability Property|
|---|---|---|---|---|---|
|Measure Mappings|ROBOT_BATTERY_MAIN|VOLTAGE|Robot4000|ROBOT_BATTERY_MAIN_ROBOT4000|VOLTAGE|
|Measure Mappings|ROBOT_BATTERY_MAIN|MODE|Robot4000|ROBOT_BATTERY_MAIN_ROBOT4000|MODE|
|Measure Mappings|ROBOT_TEMP_VIBRATION|ROBOT_TEMPERATURE|Robot4000|ROBOT_TEMP_VIBRATION_ROBOT4000|ROBOT_TEMPERATURE|
|Measure Mappings|ROBOT_TEMP_VIBRATION|ROBOT_VIBRATION|Robot4000|ROBOT_TEMP_VIBRATION_ROBOT4000|ROBOT_VIBRATION|

Note Down the generated Mapping guid as this is needed while onboarding a thing.


### On-boarding Things
1. Onboard an Equipment in Field Service Management and obtain the Equipment Id
2. Onboard a Thing in Leonardo IoT with Alternate Id as Equipment Id.
3. Onboard a Device in IoT Service and add a sensor of type "Robot4000"
4. Assign the Thing from Leonardo IoT to the device in IoT Service.

Step No : 2,3,4 can be done together as well using Thing onboarding API. Refer to section "**10.3.1.5 Example 5 – Create a thing along with the mapping
to sensors**" in [SAP Help Portal](https://help.sap.com/doc/a48fdbd924724b378d6f71c54c9f35b5/1905b/en-US/leoAPI.pdf)
	
### Data Ingestion
* Ingest data for the devices through IoT Service in which the battery voltage should be less than 16, then a Service Call should be created within next 10 minutes as per the schedule in the Rule definition.
