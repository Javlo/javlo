jQuery.noConflict();

jQuery(document).ready(function(){
	/**
	 * Color Picker
	**/
	jQuery('#colorpicker').ColorPicker({
		onSubmit: function(hsb, hex, rgb, el) {
			jQuery(el).val(hex);
			jQuery(el).ColorPickerHide();
		},
		onBeforeShow: function () {
			jQuery(this).ColorPickerSetColor(this.value);
		}
	})
	.bind('keyup', function(){
		jQuery(this).ColorPickerSetColor(this.value);
	});
	
	jQuery('#colorselector').ColorPicker({
		color: '#0000ff',
		onShow: function (colpkr) {
			jQuery(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			jQuery(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			jQuery('#colorselector').css('backgroundColor', '#' + hex);
		}
	});
	
	/**
	 * Date picker
	**/
	jQuery( "#datepicker" ).datepicker();
	
	
	/**
	 * Growl Notification
	**/
	jQuery('.growl').click(function(){
		jQuery.jGrowl("Hello world!");
		return false;
	});
	
	jQuery('.growl2').click(function(){
		var msg = "This notification will live a little longer.";
		var position = "top-right";
		var scrollpos = jQuery(document).scrollTop();
		if(scrollpos < 50) position = "customtop-right";
		jQuery.jGrowl(msg, { life: 5000, position: position});
		return false;
	});
	
	//this will prevent growl box to show on top of the header when
	//scroll event is fired
	jQuery(document).scroll(function(){
		if(jQuery('.jGrowl').length != 0) {
			var pos = jQuery(document).scrollTop();
			if(pos < 50) jQuery('.jGrowl').css({top: '100px'}); else jQuery('.jGrowl').css({top: '0'});
		}
	});
	
	
	/**
	 * Tab
	**/
	jQuery( "#tabs" ).tabs();
	
	/**
	 * Accordion
	**/
	jQuery( ".accordion" ).accordion();
	
	
	/**
	 * Modal Alert Boxes
	**/
	jQuery('.alertboxbutton').click(function(){
		jAlert('This is a custom alert box', 'Alert Dialog');
	});
	
	jQuery('.confirmbutton').click(function(){
		jConfirm('Can you confirm this?', 'Confirmation Dialog', function(r) {
			jAlert('Confirmed: ' + r, 'Confirmation Results');
		});
	});
	
	jQuery('.promptbutton').click(function(){
		jPrompt('Type something:', 'Prefilled value', 'Prompt Dialog', function(r) {
			if( r ) alert('You entered ' + r);
		});
	});
	
	jQuery('.alerthtmlbutton').click(function(){
		jAlert('You can use HTML, such as <strong>bold</strong>, <em>italics</em>, and <u>underline</u>!');
	});
	
	
	/**
	 * Slider 
	**/
	jQuery("#slider").slider({value: 40});
	
	//Slider that snap to increments
	jQuery("#slider2").slider({
			value:100,
			min: 0,
			max: 500,
			step: 50,
			slide: function(event, ui) {
				jQuery("#amount").text("$"+ui.value);
			}
	});
	jQuery("#amount").text("$" + jQuery("#slider").slider("value"));

	
	//Slider with range
	jQuery("#slider3").slider({
		range: true,
		min: 0,
		max: 500,
		values: [ 75, 300 ],
		slide: function( event, ui ) {
			jQuery("#amount2").text("$" + ui.values[ 0 ] + " - $" + ui.values[ 1 ]);
		}
	});
	jQuery("#amount2").text("$" + jQuery("#slider3").slider("values", 0) +
			" - $" + jQuery("#slider3").slider("values", 1));
	
	// Slider with fixed minimum
	jQuery("#slider4").slider({
			range: "min",
			value: 37,
			min: 1,
			max: 100,
			slide: function( event, ui ) {
				jQuery("#amount4").text("$" + ui.value);
			}
	});
	jQuery("#amount4").text("$"+jQuery("#slider4").slider("value"));

	//Slider with fixed maximum
	jQuery("#slider5").slider({
			range: "max",
			value: 60,
			min: 1,
			max: 100,
			slide: function(event, ui) {
				jQuery("#amount5").text("$"+ui.value);
			}
	});
	jQuery("#amount5").text("$"+jQuery("#slider5").slider("value"));
	
	//Slider vertical
	jQuery("#slider6").slider({
			orientation: "vertical",
			range: "min",
			min: 0,
			max: 100,
			value: 60,
			slide: function( event, ui ) {
				jQuery("#amount6").text(ui.value);
			}
	});
	jQuery("#amount6").text( jQuery("#slider6").slider("value"));

	
	//Slider vertical with range
	jQuery("#slider7").slider({
			orientation: "vertical",
			range: true,
			values: [17, 67],
			slide: function(event, ui) {
				jQuery("#amount7").text("$"+ui.values[0]+"-$"+ui.values[1]);
			}
		});
	jQuery("#amount7").text("$"+jQuery("#slider7").slider("values",0) +
			" - $"+jQuery("#slider7").slider("values",1));


});