jQuery(document).bind("ajaxUpdate",function () {	
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
	jQuery( ".accordion" ).accordion( {active:".accordion .open"} );
	
	//////////// DATE PICKER /////////////////
	jQuery.datepicker.setDefaults( jQuery.datepicker.regional[ editLanguage ] );
	jQuery( ".datepicker" ).datepicker({maxDate: "+0D", dateFormat: dateFormat}); 
	fullHeight();
});

function fullHeight() {
	jQuery(".full-height").each(function() {
		var contentHeight = jQuery("#footer").offset().top - jQuery(this).offset().top;
		jQuery(this).css("height", contentHeight+"px");
	});
}

jQuery(window).resize(fullHeight);
