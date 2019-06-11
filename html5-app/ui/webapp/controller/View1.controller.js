sap.ui.define([
	"sap/ui/model/odata/v2/ODataModel", "sap/ui/core/util/MockServer"
], function(ODataModel, MockServer) {
	"use strict";

	return sap.ui.controller("sap.iot.noah.fsm.detailpage.controller.View1", {
		onInit: function() {
			this._initMockServer();

			var oModel = new ODataModel("smartmicrochart.SmartLineMicroChart/target", true);
			var oChart = this.getView().byId("idLineChart");
			var oChartTitle = this.getView().byId("chartTitle");
			oChart.setChartTitle(oChartTitle);
			oChart.setModel(oModel);
		},

		onExit: function() {
			this._oMockServerTargetCriticality.stop();
		},
		
		onAfterRendering: function() {
			var oPage = this.byId("page");
			// adding height to the section of the Page
			jQuery(oPage.getDomRef().children[1]).height("500px");
		},

		_initMockServer: function() {
			this._oMockServerTargetCriticality = new MockServer({
				rootUri: "smartmicrochart.SmartLineMicroChart/target/"
			});

			this._oMockServerTargetCriticality.simulate("./mockserver/metadata.xml", {
				sMockdataBaseUrl: "mockserver"
			});

			this._oMockServerTargetCriticality.start();
		}
	});
});
