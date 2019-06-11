/* global QUnit */
QUnit.config.autostart = false;

sap.ui.getCore().attachInit(function () {
	"use strict";

	sap.ui.require([
		"sap/iot/noah/fsm/detailpage/test/unit/AllTests"
	], function () {
		QUnit.start();
	});
});