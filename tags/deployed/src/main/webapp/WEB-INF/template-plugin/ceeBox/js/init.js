jQuery(document).ready(function(){
		debugging = true;

		$.fn.ceebox.videos.base.param.allowScriptAccess = "sameDomain" //added to kill the permissions problem
		$.extend($.fn.ceebox.videos,{
			uctv:{
				siteRgx: /uctv\.tv\/search\-details/i, 
				idRgx: /(?:showID=)([0-9]+)/i, 
				src: "http://www.uctv.tv/player/player_uctv_bug.swf",
				flashvars: {previewImage : "http://www.uctv.tv/images/programs/[id].jpg", movie : "rtmp://webcast.ucsd.edu/vod/mp4:[id]",videosize:0,buffer:1,volume:50,repeat:false,smoothing:true}
			}
		});
		//$().ceebox(); //used to test to make sure the init call works.
		//$(".ceebox").ceebox({boxColor:'#fff',borderColor:'#525252',textColor:'#333',videoJSON:"js/humor.json"});
		$(".ceebox").ceebox({borderColor:'#dcdcdc',boxColor:"#fff"});
		//$("map").ceebox({fadeOut:"slow",fadeIn:"slow",onload:function(){$("#cee_box").animate({backgroundColor:"#F00"},function(){$(this).animate({backgroundColor:"#fff"})});}});		
		$("map").ceebox();		
		$(".ceebox2").ceebox({unload:function(){$("body").css({background:"#ddf"})}});
		//window.console.log($.fn.ceebox.videos.colbertFull)
		//$("body").ceebox(); //uncomment and every link on the page is in one gallery
		var testhtml = "<a href='http://balsaman.org' title='Balsa Man'>Balsa Man</a>"
		var testhtml2 = "<div style='padding:20px;text-align:center'><h2>Hi I am some content built as a javascript variable!</h2><p><a href='#' class='cee_close'>Close Me</a></p></div>"
		$("#testlink").click(function(){
			$.fn.ceebox.overlay();
			$.fn.ceebox.popup(testhtml,{onload:true,htmlWidth:600,htmlHeight:450});
			return false;		  
		});
		$("#testlink2").click(function(){
			$.fn.ceebox.overlay();
			$.fn.ceebox.popup(testhtml2,{width:600,height:400});
			return false;
		});
		//$.fn.ceebox.popup(testhtml,{onload:true,htmlWidth:600,htmlHeight:450});
	});