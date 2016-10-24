var editorMouseX = 0;
var editorMouseY = 0;
var cornerMoved = null;
var deltaX = 0;
var deltaY = 0;
var REFERENCE_SIZE = 10000;

jQuery(window).load(function() {	
	jQuery( window ).mousemove(function( event ) {
		editorMouseX = event.pageX;
		editorMouseY = event.pageY;		
		if (cornerMoved != null) {
			if (cornerMoved.hasClass('crop-zone')) {
				jQuery('.jeditor .corner').hide();
				wrapper = cornerMoved.parent();				
				cornerMoved.css('left', cornerMoved.offset().left-wrapper.offset().left+(editorMouseX-cornerMoved.offset().left-deltaX));
				cornerMoved.css('top', cornerMoved.offset().top-wrapper.offset().top+(editorMouseY-cornerMoved.offset().top-deltaY));
				deltaX = editorMouseX-cornerMoved.offset().left;
				deltaY = editorMouseY-cornerMoved.offset().top;				
				if (cornerMoved.offset().left < wrapper.offset().left) {
					cornerMoved.css('left', 0);
				}
				if (cornerMoved.offset().top < wrapper.offset().top) {
					cornerMoved.css('top', 0);
				}
				if (cornerMoved.offset().left+cornerMoved.outerWidth() > wrapper.offset().left+wrapper.outerWidth()) {
					cornerMoved.css('left', wrapper.outerWidth()-cornerMoved.outerWidth());
				}
				if (cornerMoved.offset().top+cornerMoved.outerHeight() > wrapper.offset().top+wrapper.outerHeight()) {
					cornerMoved.css('top', wrapper.outerHeight()-cornerMoved.outerHeight());
				}
			} else {
				wrapper = cornerMoved.parent().parent();				
				x = editorMouseX-wrapper.offset().left;
				if (x < 0) {
					x=0;
				} else if(x>wrapper.outerWidth()) {
					x = wrapper.outerWidth();
				}
				y = editorMouseY-wrapper.offset().top;
				if (y < 0) {
					y=0;
				} else if(y>wrapper.outerHeight()) {
					y = wrapper.outerHeight();
				}
				
				cornerMoved.css('top', y-cornerMoved.outerHeight()/2);
				cornerMoved.css('left', x-cornerMoved.outerWidth()/2);				
			}
			moveCorner();
		}
	});	
	editor(jQuery('.jeditor'));
});

function moveCorner() {	
	jQuery('.jeditor .corner').hide();
	cornerMoved.show();
	rect = jQuery('.jeditor .crop-zone');
	wrapper = rect.parent();
	dec = cornerMoved.outerHeight()/2	
	right = rect.offset().left+rect.outerWidth();
	bottom = rect.offset().top+rect.outerHeight();
	if (cornerMoved.hasClass('topleft')) {
		rect.css('left', cornerMoved.offset().left-wrapper.offset().left+dec);
		rect.css('top', cornerMoved.offset().top-rect.parent().offset().top+dec);				
		rect.css('width', right-rect.offset().left);
		rect.css('height', bottom-rect.offset().top);
	} else if (cornerMoved.hasClass('bottomleft')) {								
		rect.css('left', cornerMoved.offset().left-wrapper.offset().left+dec);
		rect.css('height', cornerMoved.offset().top-rect.offset().top+dec);
		rect.css('width', right-rect.offset().left);
	} else if (cornerMoved.hasClass('topright')) {							
		rect.css('top', cornerMoved.offset().top-rect.parent().offset().top+dec);
		rect.css('height', bottom-rect.offset().top);
		rect.css('width', cornerMoved.offset().left-rect.offset().left+dec);
	} else if (cornerMoved.hasClass('bottomright')) {		
		rect.css('height', cornerMoved.offset().top-rect.offset().top+dec);
		rect.css('width', cornerMoved.offset().left-rect.offset().left+dec);
	}
	jQuery('.jeditor .crop-shadow-1').css('width', rect.css('left'));
	jQuery('.jeditor .crop-shadow-2').css('width', rect.css('width'));
	jQuery('.jeditor .crop-shadow-2').css('height', rect.offset().top-wrapper.offset().top);
	jQuery('.jeditor .crop-shadow-2').css('left', rect.css('left'));
	jQuery('.jeditor .crop-shadow-3').css('width', rect.css('width'));
	jQuery('.jeditor .crop-shadow-3').css('height', wrapper.outerHeight()-(rect.outerHeight()+rect.offset().top-wrapper.offset().top));
	jQuery('.jeditor .crop-shadow-3').css('left', rect.css('left'));
	jQuery('.jeditor .crop-shadow-4').css('width', rect.css('right'));
}

function drawImageScaled(img, canvas) {
   ctx = canvas.getContext('2d');
   hRatio = canvas.width  / img.width    ;
   vRatio =  canvas.height / img.height  ;
   ratio  = Math.min ( hRatio, vRatio );
   ctx.clearRect(0,0,canvas.width, canvas.height);
   ctx.drawImage(img, 0,0, img.width, img.height,0,0,img.width*ratio, img.height*ratio);  
}

function absoluteWidth(w) {
	rect = jQuery('.jeditor .crop-zone');
	wrapper = rect.parent();
	return Math.round(w*REFERENCE_SIZE/wrapper.outerWidth());
}

function absoluteHeight(h) {
	rect = jQuery('.jeditor .crop-zone');
	wrapper = rect.parent();
	return Math.round(h*REFERENCE_SIZE/wrapper.outerHeight());
}

function resetCorner() {
	rect = jQuery('.jeditor .crop-zone');
	wrapper = rect.parent();
	dec = jQuery('.jeditor .corner.topleft').outerHeight()/2
	jQuery('.jeditor .corner.topleft').css('top', rect.offset().top-wrapper.offset().top-dec);
	jQuery('.jeditor .corner.topleft').css('left', rect.offset().left-wrapper.offset().left-dec);
	jQuery('.jeditor .corner.bottomleft').css('top', rect.offset().top+rect.outerHeight()-wrapper.offset().top-dec);
	jQuery('.jeditor .corner.bottomleft').css('left', rect.offset().left-wrapper.offset().left-dec);
	jQuery('.jeditor .corner.topright').css('top', rect.offset().top-wrapper.offset().top-dec);
	jQuery('.jeditor .corner.topright').css('left', rect.offset().left+rect.outerWidth()-wrapper.offset().left-dec);
	jQuery('.jeditor .corner.bottomright').css('top', rect.offset().top+rect.outerHeight()-wrapper.offset().top-dec);
	jQuery('.jeditor .corner.bottomright').css('left', rect.offset().left+rect.outerWidth()-wrapper.offset().left-dec);
	jQuery('.jeditor .corner').show();	
	jQuery('.jeditor [name=crop-left]').val(absoluteWidth(rect.offset().left-wrapper.offset().left));
	jQuery('[name=crop-top]').val(absoluteHeight(rect.offset().top-wrapper.offset().top));
	jQuery('[name=crop-width]').val(absoluteWidth(rect.outerWidth()));
	jQuery('[name=crop-height]').val(absoluteHeight(rect.outerHeight()));
}

function editor(inEditor) {	
	jQuery('document, body, .jeditor .corner, .jeditor .crop-zone, .jeditor .image-wrapper, .jeditor .image-wrapper canvas').mouseup(function () {
		resetCorner();
		cornerMoved = null;
    });
	jQuery('document').mouseout(function() {
		resetCorner();
		cornerMoved = null;		
	});
	jQuery('.jeditor .corner, .jeditor .crop-zone').mousedown(function (){		 
		cornerMoved = jQuery(this);
		deltaX = editorMouseX-cornerMoved.offset().left;
		deltaY = editorMouseY-cornerMoved.offset().top;
    });	
	jQuery('.jeditor .btn-flip').click(function (){		 
		var image = jQuery('.jeditor img');
		if (image.hasClass('flipped')) {
			jQuery('#flip').val('false');
			image.removeClass('flipped');
		} else {
			jQuery('#flip').val('true');
			image.addClass('flipped');
		}
		return false;
    });	
	resetCorner();
}