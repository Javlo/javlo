// Interface


$(document).ready(function(){
	
	
	$('#example').DataTable();
	$('input[placeholder]').placeholder();
	$( "#tabs-edit").tabs(); 
	$('.fancybox').fancybox();
	$('.tooltip').tooltipster({
		offsetX: 0
	});
	
	
	
	
	// Contextual
	$("#contextual-box").draggable({
		axis: "x",
		containment: "body",
		handle:"#contextual-box-draggable",
		scroll: false
	});
	
	$("#articles .head, #clip-board .head, #drop-files .head").click(function(e){
		e.preventDefault();
		$(this).parent().toggleClass("open");
		$(this).next().slideToggle("slow");
	});
	
	
	// Sortable
	$( "#sortable" ).sortable({
		placeholder:"ui-state-highlight",
		axis:"y",
		handle:"span.move",
		items: "li:not(.ui-state-disabled)" 
	});
	
	
	$( ".table-container" ).sortable({
		placeholder:"ui-state-highlight",
		axis:"y",
		items: ".component" 
	});
	
	
	
	// Templates Active-State
	$("ul#sortable").on('click','li', function(){
		$(this).toggleClass('current').siblings().removeClass('current'); 
	});
	
	
	
		
	
	
	// Calendar
	$( "#datepicker" ).datepicker();
	
	
	
	
	
	$(".btn-delete").click(function(e){
		e.preventDefault();
		$(this).parents("div.component").hide(); 
	});
	$(".btn-copy").click(function(e){
		e.preventDefault();
		$(this).parents("div.area").toggleClass("copy");
		$('#clip-board').show(); 
		$('#clip-board .head').trigger('click');
	});
	
	$(".file-close").click(function(e){
		e.preventDefault();
		$(this).parents("div.file").hide(); 
	});
	
	
	
	
	
	
	
	// Info
	$(".info-close").click(function(){
		$("#info").slideToggle("fast");
		$(this).toggleClass("open");
		$(this).parents(".for-info").toggleClass("open"); 
	});
	
	// Bg-Image Flexslider
	$('#bg-image .flexslider').flexslider({
		animation: "fade",
		slideshowSpeed: 12000,
		slideshow: true,
		controlNav:false,
		directionNav:false,
		keyboard:true
	});
	
	// Templates-Basic Flexslider
	$('#templates-basic .flexslider').flexslider({
		animation: "slide",
		animationLoop: false,
		slideshow: false,
		itemWidth: 200,
		itemMargin: 0
	});
	
	// Templates-Custom Flexslider
	$('#templates-custom .flexslider').flexslider({
		animation: "slide",
		animationLoop: false,
		slideshow: false,
		itemWidth: 200,
		itemMargin: 0
	});
	
	
	
	// Templates Active-State
	$(".slides").on('click','li', function(){
		$(this).toggleClass('active').siblings().removeClass('active'); 
	});
	
	

});





$('.overflow').bind('scroll', function() {
     if($(this).scrollTop() >= '10')
     {
		$(this).prev(".tabs-head").addClass("scroll");
	}
	else {
		$(this).prev(".tabs-head").removeClass("scroll");
	}
});


$(window).scroll(function(){
	
	// Scroll For-Header
	if($(window).scrollTop() >= '50')		   
		{
			$('body').addClass("scroll");
		}
	else {
			$('body').removeClass("scroll");
		}
		
	// Scroll For-Contextual-Box
	if($(window).scrollTop() >= '140')		   
		{
			$('#contextual-box').addClass("fixed");
		}
	else {
			$('#contextual-box').removeClass("fixed");
		}
	
});






	

