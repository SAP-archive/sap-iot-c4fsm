# Integration Between SAP Leonardo IoT with SAP Field Service Management

## Description

This IoT reference application is meant to make it simpler for you to build your own IoT application. It walks you through all of the steps and provides you with all configuration and source code required for you to be able to reproduce the application 1:1.

It features the use of Leonardo IoT, Field Service Management and a Web-based UI.

We used Robot4000 at SAP as the example devices to be monitored and facilitate the below usecases.

* Enabling SAP Leonardo IoT to automatically create the service call in SAP Field Service Management based on the conditions on device measurements and aggregates.
* Enabling Service Technical to access equipment specific content from SAP Leonardo IoT in SAP Field Service Management Mobile Application (iPhone) from a Service Call view.

## Prerequisites
* [SAP Cloud Platform Account](https://cloudplatform.sap.com/index.html) with a neo sub-account and a cloud foundry sub-account
* [A subscription to SAP Leonardo IoT](https://cloudplatform.sap.com/capabilities/product-info.SAP-Leonardo-Internet-of-Things.1e3dd0d0-a355-4a0a-bc3e-36285eae4cbe.html) and an instance of [iot (service) for cloud foundry](https://help.sap.com/viewer/2f1daa938df84fd090fa2a4da6e4bc05/Cloud/en-US). 
* [A subscription to SAP Cloud Platform Integration](https://cloudplatform.sap.com/capabilities/product-info.SAP-Cloud-Platform-Integration.cceaaf2b-8ceb-4773-9044-6d8dad7a12eb.html).
* HTML5 Application Repository ([SAP Help Portal](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/11d77aa154f64c2e83cc9652a78bb985.html))  
* [An optional subscription to SAP Web IDE Full-Stack](https://cloudplatform.sap.com/capabilities/technical-asset-info.SAP-Web-IDE-Full-Stack.52fdf566-8709-41ef-bfa4-2aabcd33a865.html). WebIDE can be used to build Multi Target Applications ( HTML5, FLP) in almost code less environment and to publish them to Cloud Platform.
* [A subscription to SAP Field Service Management License](https://www.sap.com/products/field-service-management.html) ([SAP Help Portal](https://help.sap.com/viewer/product/SAP%20Field%20Service%20Management/Castor/en-US))
* JDK 1.8.0 ( Java Development Kit)
## Download

[Download the files from GitHub as a zip file](../../archive/master.zip), or [clone the repository](https://help.github.com/articles/cloning-a-repository/) on your desktop.

## Installation

Refer to the [documentation](#documentation).

As the source code needs to be deployed in cloud platform account, Please follow the typical approach for managing source code, [Building MTA applications](https://help.sap.com/viewer/58746c584026430a890170ac4d87d03b/Cloud/en-US/9f778dba93934a80a51166da3ec64a05.html) and [Deploying MTA applications](https://github.com/cloudfoundry-incubator/multiapps-cli-plugin#usage) to cloud platform. 

## Configuration

We provided some example configuration for e.g. the Connected Silo in the source files that might be helpful to get you started. We also illustrated the Thing Model and you can configure it in Leonardo IoT via the Thing Modeler apps.

## Version Compatibility
##### SAP Leonardo IoT
Release : 1905

##### SAP Cloud Platform Integration
Release : 4.0 ( 1904 ).


##### SAP Field Service Management 
- Release : 1905
- Service Call API Version : V2
- Mobile Application Support : iPhone, iOS 12.3.1, Protocal Version : 5
- Coresuite 6.1.1.0
- DTO's
    - Equipment : 17
    - Service Call : 17

##### SAP UI5
* Release : 1.66.1

##### HTML5 Application Repository
- Release: 1905

## Limitations/ Boundary conditions
- Consumption of SAP Leonardo IoT content is only supported in FSM Mobile Application of iPhone ( iOS 12.3.1 ).
- All Service Technicians are expected to have same level of authorizations for consuming IoT content
- Role Collection assignments to Service Technicians at Cloud Identity Provider would not have any impact on data authorization while accessing the content from FSM Mobile Application.
- IoT Application used in the Web Container configuration can consume content only from SAP Leonardo IoT service & API's from other business services or other applications can't be consumed.


## Documentation

We provided the complete documentation inside [docs](/docs) folder.
* [Modeler Configuration](docs/00-modeler-configuration.md)
* [iFlow Definitions](docs/01-iflow-setup.md)
* [One time Setup of Proxy & Approuter](docs/02-proxy-setup.md)
* [How to build HTML5 applications from WebIDE](docs/03-build-html5-app-from-WebIDE.md)
* [Simulating Service Call creation through iFlow by Postman collection](docs/04-postman-setup.md)


## Support

The content is provided "as-is". There is no guarantee that raised issues will be answered or addressed in future releases.

## License
Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
This file is licensed under the SAP Sample Code License, v1.0 except as noted otherwise in the [LICENSE file](/LICENSE)
