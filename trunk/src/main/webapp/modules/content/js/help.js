jQuery(document).ready(function() {
	showHelpButton();
});

function showHelp() {
	addToolTips("#breadcrumbs", "help.breadcrumbs", "left");
	addToolTips("#tools", "help.tools");
	addToolTips(".sidebox", "help.component");
	addToolTips("#form-component-list", "help.component", "left");	
	addToolTips(".add-page", "help.add-page", "right");
}
