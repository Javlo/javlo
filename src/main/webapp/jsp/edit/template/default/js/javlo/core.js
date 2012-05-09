	jQuery(document).ready(function () {
		function showTooltip(x, y, contents) {
			jQuery('<div id="tooltip" class="tooltipflot">' + contents + '</div>').css( {
				position: 'absolute',
				display: 'none',
				top: y + 5,
				left: x + 5
			}).appendTo("body").fadeIn(200);
		}

		//////////// TABS /////////////////
		jQuery( ".tabs" ).tabs();
		
		//////////// ACCORDION /////////////////
		jQuery( ".accordion" ).accordion();
		
		//////////// DATE PICKER /////////////////
		jQuery.datepicker.setDefaults( jQuery.datepicker.regional[ editLanguage ] );
		jQuery( ".datepicker" ).datepicker({maxDate: "+0D", dateFormat: dateFormat}); 

});
