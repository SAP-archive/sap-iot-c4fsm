sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/iot/noah/fsm/detailpage/model/formatter",
	'sap/ui/model/json/JSONModel',
	'sap/viz/ui5/data/FlattenedDataset',
	'sap/viz/ui5/controls/common/feeds/FeedItem',
	'sap/viz/ui5/format/ChartFormatter',
	'sap/viz/ui5/api/env/Format',
	'./InitPage'
], function (Controller, formatter, JSONModel, FlattenedDataset, FeedItem, ChartFormatter, Format, InitPageUtil) {

	"use strict";

	return Controller.extend("sap.iot.noah.fsm.detailpage.controller.ThingPage", {
		formatter: formatter,
		settingsModel: {
			chartType: {
				name: "Chart Type",
				defaultSelected: "3",
				values: [{
					key: "3",
					name: "Line Chart",
					vizType: "timeseries_line",
					json: "/column/timeAxis.json",
					value: ["Revenue"],
					dataset: {
						dimensions: [{
							name: 'Date',
							value: "{Date}",
							dataType: 'date'
						}],
						measures: [{
							name: 'Revenue',
							value: '{Revenue}'
						}],
						data: {
							path: "/milk"
						}
					},
					vizProperties: {
						plotArea: {
							window: {
								start: "firstDataPoint",
								end: "lastDataPoint"
							},
							dataLabel: {
								formatString: ChartFormatter.DefaultPattern.SHORTFLOAT_MFD2,
								visible: false
							}
						},
						valueAxis: {
							visible: true,
							label: {
								formatString: ChartFormatter.DefaultPattern.SHORTFLOAT
							},
							title: {
								visible: false
							}
						},
						timeAxis: {
							title: {
								visible: false
							},
							interval: {
								unit: ''
							},
							levels: [
								"hour",
								"day",
								"month",
							]
						},
						title: {
							visible: false
						},
						interaction: {
							syncValueAxis: false
						}
					}
				}]
			}
		},
		oVizFrame: null,
		chartTypeSelect: null,
		chart: null,


		onInit: function () {
			var oRouter = this.getOwnerComponent().getRouter();
			var oModel = new sap.ui.model.json.JSONModel();
			this.getView().setModel(oModel, "thingPageModel");
			oRouter.getRoute("landingpage").attachMatched(this._onRouteMatched, this);

			Format.numericFormatter(ChartFormatter.getInstance());

			var oModel = new JSONModel(this.settingsModel);
			this.getView().setModel(oModel, "chartSettings");

			var oVizFrame = this.oVizFrame = this.getView().byId("idVizFrame");
			oVizFrame.setVizProperties(this.settingsModel.chartType.values[0].vizProperties);

            InitPageUtil.initPageSettings(this.getView());
		},

		/** Retreive the ThingId and ThingType and do a call to the backend with the expand paramaters to bind it to the header and basic data section **/
		_onRouteMatched: function (oEvent) {
			var model = sap.ui.getCore().getModel("v-approuter-parameter-model");
			var map = model.getData();

			var thing = {};
			thing.thingId = map["thingId"];
			thing.thingType = map["thingType"];
			this.__onRouteMatched(thing);
		},

    __onRouteMatched: function (arg) {
			this.sThingId = arg.thingId;
			var sThingType = arg.thingType;
			var oSeverity = {
				iHighSeverity: arg.highSeverity,
				iMediumSeverity: arg.mediumSeverity,
				iLowSeverity: arg.lowSeverity
			};
			var oDetailsThingModel = this._findThingModel(sThingType);
			if (oDetailsThingModel) {
				this._readDetailsService(oDetailsThingModel, this.sThingId);
			} else {
				var sURL = "/destinations/details-thing-sap/CompositeThings/ThingType/v1/" + sThingType;
				var oNewThingTypeModel = new sap.ui.model.odata.ODataModel(sURL);
				this._readDetailsService(oNewThingTypeModel, this.sThingId);
			}
			//Render the Measured Values Control
			var oContext = {
				ThingId: this.sThingId,
				ThingType: sThingType
			};

			// this.byId("idMeasuringPoints").doReload(oContext);
			// this.byId("idMeasuringPoints").rerender();
			
			// Call the events service for rendering timeline and eventList control
			this._readAnalyticsService(this.sThingId);
			this._readEventsService(this.sThingId);

			this.getView().getModel("thingPageModel").setProperty("/severity", oSeverity);
			if (this.byId("idSemanticBarHBox").getDomRef()) {
				this._renderSemanticBar(oSeverity.iHighSeverity, oSeverity.iMediumSeverity, oSeverity.iLowSeverity);
			}
		},

		_renderSemanticBar: function (urgent, important, information) {
			var oHeaderImage = this.byId("ObjectPageLayoutHeaderTitle").getAggregation("_objectImage");
			if (!oHeaderImage) {
				oHeaderImage = {};
				oHeaderImage.aCustomStyleClasses = [];
			}
			for (var i = 0; i <= oHeaderImage.aCustomStyleClasses.length; i++) {
				oHeaderImage.aCustomStyleClasses.pop();
			}
			if (urgent > 0) {
				$(".objectPageHeaderImage").css({
					"border-left-color": "#bb0000",
					"border-left-style": "solid",
					"border-left-width": ".5rem"
				});
				$(".headerImage").css({
					"border-left-color": "#bb0000",
					"border-left-style": "solid",
					"border-left-width": ".5rem",
					"color": "white"
				});
				$(".objectSematicBar").css({
					"background-color": "#bb0000",
					"margin": "0rem"
				});
				//$(".sapUxAPObjectPageHeaderIdentifier .sapUxAPObjectPageHeaderObjectImageForce .sapUxAPObjectPageHeaderStickied .sapUxAPObjectPageHeaderObjectImage").css({"border-left-color": "red", "border-left-style": "solid", "border-left-width": ".5rem"});
				oHeaderImage.aCustomStyleClasses.push("thingPageRedSematic");
			} else if (important > 0) {
				$(".objectPageHeaderImage").css({
					"border-left-color": "#e78c07",
					"border-left-style": "solid",
					"border-left-width": ".5rem"
				});
				$(".headerImage").css({
					"border-left-color": "#e78c07",
					"border-left-style": "solid",
					"border-left-width": ".5rem"
				});
				$(".objectSematicBar").css({
					"background-color": "#e78c07",
					"margin": "0rem"
				});
				oHeaderImage.aCustomStyleClasses.push("thingPageOrangeSematic");
			} else if (information > 0) {
				$(".objectPageHeaderImage").css({
					"border-left-color": "#2b7d2b",
					"border-left-style": "solid",
					"border-left-width": ".5rem"
				});
				$(".headerImage").css({
					"border-left-color": "#2b7d2b",
					"border-left-style": "solid",
					"border-left-width": ".5rem"
				});
				$(".objectSematicBar").css({
					"background-color": "#2b7d2b",
					"margin": "0rem"
				});
				oHeaderImage.aCustomStyleClasses.push("thingPageGreenSematic");
			} else {
				$(".objectPageHeaderImage").css({
					"border-left-color": "#d3d7d9",
					"border-left-style": "solid",
					"border-left-width": ".5rem"
				});
				$(".headerImage").css({
					"border-left-color": "#d3d7d9",
					"border-left-style": "solid",
					"border-left-width": ".5rem"
				});
				$(".objectSematicBar").css({
					"background-color": "#d3d7d9",
					"margin": "0rem"
				});
				oHeaderImage.aCustomStyleClasses.push("thingPageGreySematic");
			}
			oHeaderImage.aCustomStyleClasses.push("sapUxAPObjectPageHeaderObjectImage");
		},

		_findThingModel: function (sThingType) {
			//Create a loop and just check how many thingModels are created and break if there is no thingModel
			for (var i = 1; i < 100; i++) {
				if (this.getOwnerComponent().getModel("thingModel" + i)) {
					//Compare the thingType with the thingModel thingtype , if it matches then return that thingModel
					var sServiceURL = this.getOwnerComponent().getModel("thingModel" + i).sServiceUrl;
					var matchedThingType = sServiceURL.substring(sServiceURL.lastIndexOf("/") + 1);
					if (sThingType === matchedThingType) {
						return this.getOwnerComponent().getModel("thingModel" + i);
					}
				} else {
					jQuery.sap.log.error(
						"The thingType has not matched with the ThingModel created in the Manifest file , hence need to create a new oData Model for this thingType"
					);
					break;
				}
			}
		},

		_readDetailsService: function (oDetailsModel, sThingId) {
			var that = this;
			oDetailsModel.read("/Things('" + sThingId + "')", {
				urlParameters: {
					"$expand": 	"DYN_ENT_<<TENANT-PACKAGE-NAMESPACE-UNDERSCORE-WITHOUT-DOTS>>_fsm_demo__DefaultImagePropertySet,DYN_ENT_<<TENANT-PACKAGE-NAMESPACE-UNDERSCORE-WITHOUT-DOTS>>_fsm_demo__ROBOT_METADATA," + 
								"DYN_ENT_<<TENANT-PACKAGE-NAMESPACE-UNDERSCORE-WITHOUT-DOTS>>_fsm_demo__ROBOT_BATTERY_MAIN,DYN_ENT_<<TENANT-PACKAGE-NAMESPACE-UNDERSCORE-WITHOUT-DOTS>>_fsm_demo__ROBOT_TEMP_VIBRATION"
				},
				success: function (oData) {
					that.getView().getModel("thingPageModel").setProperty("/detailsData", oData);
					var oThingImage = that.byId("idHeaderImage");
					oThingImage.attachError(that.onImageLoadError, that);
					// what is the point of assigning image in the view, if it is done this way in the controller ? Commenting this code
					// oThingImage.setSrc("/backend-image/things/" + that.sThingId);
					sap.ui.getCore().byId("idBusy").close();
				},
				error: function (oError) {
					jQuery.sap.log.error(oError);
					sap.ui.getCore().byId("idBusy").close();
				}
			});

		},

		/**
		 * Event handler for Error during Image Loading
		 * 
		 * @public
		 * @param {object} oEvent Event Handler
		 */
		onImageLoadError: function () {
			this.byId("ObjectPageLayout").getHeaderTitle().setObjectImageURI("sap-icon://machine");
			this.byId("idHeaderImage").setVisible(false);
			this.byId("idHeaderIcon").setVisible(true);
		},

		onAfterRendering: function () {
			var aData = "Min Error,Min Warning,Max Error,Max Warning,Last Measured";
			// this.byId("idMeasuringPoints").setSelectColumns(aData);
			var oSeverity = this.getView().getModel("thingPageModel").getProperty("/severity");
			if (oSeverity) {
				this._renderSemanticBar(oSeverity.iHighSeverity, oSeverity.iMediumSeverity, oSeverity.iLowSeverity);
			}

			this.chartTypeSelect = this.getView().byId("chartTypeSelect");
		},

		showError: function (sResourceId, aParameter) {
			sap.m.MessageBox.error(jQuery.sap.formatMessage(this.getResourceBundle().getText(sResourceId), aParameter));
		},

		handleNavBackPress: function () {
			window.history.back();
			if (this.getOwnerComponent().isTimedOut) {
				this.getOwnerComponent().showTimeoutMessage();
			}
		},

		_readAnalyticsService: function (sThingId) {
			var that = this;
			var oAnalyticsModel = this.getOwnerComponent().getModel("analyticsModel");
			var oFilter = new sap.ui.model.Filter("id", sap.ui.model.FilterOperator.EQ, sThingId);
			var fromDate = new Date();
			fromDate.setDate(fromDate.getDate() - 60);
			var oTimeFilter = new sap.ui.model.Filter("time", sap.ui.model.FilterOperator.BT, fromDate.toISOString(), new Date().toISOString());
			var oDimensionFilter = new sap.ui.model.Filter("duration", sap.ui.model.FilterOperator.EQ, "T10M");
			var oSorter = new sap.ui.model.Sorter("time", true); // sort descending
			oAnalyticsModel.read("/aggregates", {
				filters: [oFilter, oTimeFilter, oDimensionFilter],
				sorters: [oSorter],
				urlParameters: {
					"$select": "time,VOLTAGE_AVG",
					"$top": "100",
					"$skip": "0"
				},
				success: function (oData) {
					that.getView().getModel("thingPageModel").setProperty("/data", oData.results);
				},
				error: function (oError) {
					jQuery.sap.log.error(oError);
				}
			});
		},

		_readEventsService: function (sThingId) {
			var that = this;
			this.byId("idEventList").setThingId(sThingId);
			this.byId("idEventList").doReloadControl = true;
			var oEventsModel = this.getOwnerComponent().getModel("events");
			var oFilter = new sap.ui.model.Filter("ThingId", sap.ui.model.FilterOperator.EQ, sThingId);
			var oSorter = new sap.ui.model.Sorter("BusinessTimestamp", true); // sort descending
			oEventsModel.read("/Events", {
				filters: [oFilter],
				sorters: [oSorter],
				urlParameters: {
					"$top": "6",
					"$skip": "0"
				},
				success: function (oData) {
					that.getView().getModel("thingPageModel").setProperty("/eventsData", oData.results);
				},
				error: function (oError) {
					jQuery.sap.log.error(oError);
				}
			});
		},

		onEventListSelect: function (oEvent) {
			if (this.getOwnerComponent().isTimedOut) {
				this.getOwnerComponent().showTimeoutMessage();
			} else {
				sap.ui.getCore().byId("idBusy").open();
			}
			var oEventContext = oEvent.getParameter("event");
			var oModel = this.getView().getModel("thingPageModel");
			oModel.setProperty("/eventsData", oEventContext);
			sap.ui.getCore().setModel(oModel, "eventsModel");
			this.getOwnerComponent().getRouter().navTo("analysispage", {
				thingId: this.sThingId,
				navFrom: "events"
			});
		},

		onMeasuredValueSelect: function (oEvent) {
			if (this.getOwnerComponent().isTimedOut) {
				this.getOwnerComponent().showTimeoutMessage();
			} else {
				sap.ui.getCore().byId("idBusy").open();
			}
			var oModel = this.getView().getModel("thingPageModel");
			var oMpContext = oEvent.getParameter("context");
			oModel.setProperty("/mpData", oMpContext);
			var oProperty = oMpContext.getObject(oMpContext.getPath()).measuredValue;
			this.getOwnerComponent().getRouter().navTo("analysispage", {
				thingId: this.sThingId,
				navFrom: "measuredValues",
				headerTitle: " ",
				subHeaderTitle: " ",
				mpPath: oProperty
			});
		},

		/**
		 * Getter for the resource bundle.
		 * @public
		 * @returns {sap.ui.model.resource.ResourceModel} the resourceModel of the component
		 */
		getResourceBundle: function () {
			var ownerComponent = this.getOwnerComponent();
			return ownerComponent ? ownerComponent.getModel("i18n").getResourceBundle() : {
				getText: function (key) {
					return "No resource bundle available";
				}
			};
		}
	});
});