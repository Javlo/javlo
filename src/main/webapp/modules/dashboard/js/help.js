jQuery(document).ready(function() {
	showHelpButton();
});

function showHelp() {
	addToolTips("#info", "help.info");
	addToolTips("#charge-wrapper", "help.charge");
	addToolTips("#visits", "help.visits");
	addToolTips("#referers", "help.referers");
	addToolTips("#notification", "help.notification");
	addToolTips("#languages", "help.languages");
	addToolTips("#transformation", "help.transformation");
}
