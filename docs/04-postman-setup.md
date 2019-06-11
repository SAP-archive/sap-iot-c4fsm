# Using Postman Collection
We have added a postman collection which can be helpful to create service call through the iFlow. 

# Prerequisites
* [Postman](https://www.getpostman.com/products)

## Download
You can download the postman collection from [postman](../postman) folder.

## Installation
* Install Postman Tool
* Import both "Service Ticket (iFlow).postman_collection.json" and "iFlow.postman_environment.json" files into postman. You should be a new Collection Called "Service Ticket (iFlow)" in the list and an environment "iFlow".

## Configuration

 and replace the place holders as explained below.

| Place holder Name                            | Description                                    | More Information                                                                                                                |
|----------------------------------------------|------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| &lt;&lt;FSM_ACCOUNT_NAME&gt;&gt;                              | FSM Account Name                    |                                                                                                                                 |
| &lt;&lt;FSM_COMPANY&gt;&gt;                                | FSM Company Name         |                                                                                                                                 |
| &lt;&lt;FSM_USER&gt;&gt;                            | FSM Username     |                                                                                                                                 |
| &lt;&lt;IFLOW_URL&gt;&gt;                 | Fully formed iFlow URL                               | Refer [iFlow Definitions](01-iflow-setup.md) to obtain the iFlow URL |
| &lt;&lt;CPI_USER&gt;&gt; | Cloud Platform Integration Username |                                                                                                                                 |
| &lt;&lt;CPI_PASSWORD&gt;&gt;                              | Cloud Platform Integration User password                   |                                                                                                                                 |

## Create Service Call through Postman
* Navigate to the request "Creation of Service Ticket through iFlow" inside "Service Ticket (iFlow)"
* Choose "iFlow" as environment
* Modify the "request body" as needed and click "Send"
* Service Call should have been created.