/* global QUnit */
QUnit.config.autostart = false;

sap.ui.getCore().attachInit(function () {
	"use strict";

	sap.ui.require([
		"sap/iot/noah/fsm/detailpage/test/integration/AllJourneys"
	], function () {
		QUnit.start();
	});
});