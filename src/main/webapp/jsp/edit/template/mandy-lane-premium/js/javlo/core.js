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
	
	/////////// SORTABLE /////////////
	jQuery(".sortable").sortable({
		   placeholder: "sortable-target"
		   ,stop: function(event, ui) {
			   var url = jQuery("#form-add-page").attr("action");
			   url=url+"?webaction=movePage&page="+jQuery(ui.item).attr("id")+"&previous="+jQuery(ui.item).prev().attr("id");
			   ajaxRequest(url);
		   }
	});
	
});

function fullHeight() {
	jQuery(".full-height").each(function() {
		if(typeof  jQuery("#footer") != 'undefined' && jQuery("#footer").offset() != null) {
			var contentHeight = jQuery("#footer").offset().top - jQuery(this).offset().top;
			jQuery(this).css("height", contentHeight+"px");
		}
	});
}

jQuery(window).resize(fullHeight);

function updateLayout() {
	changeFooter();
}

