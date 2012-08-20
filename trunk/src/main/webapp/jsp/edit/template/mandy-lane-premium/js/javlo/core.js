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
	
	jQuery('a.needconfirm').click(function(){
		var link = jQuery(this);
		var href = link.attr("href");
		jConfirm(i18n.confirm, i18n.validate, function(r) {
			if (r) {
				window.location=href;
			}
		});
		return false;
	});
	
	jQuery('input.needconfirm').click(function(){
		var jsInput = this;
		var input = jQuery(this);		
		jConfirm(i18n.confirm, i18n.validate, function(r) {
			if (r) {
				jsInput.form.submit();
			}
		});
		return false;
	});
	
	var notifCount = jQuery("#notification-count");
	if(notifCount.text() == "0") {
		notifCount.hide();
	} else {
		notifCount.show();
	}
	
	var touchedAutogrow = "touched-autogrow"
	jQuery(".autogrow")
		.not("."+touchedAutogrow)
		.addClass(touchedAutogrow)
		.css("resize", "none")
		.elastic();
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

//IM
var IM_QUERY_UNREAD_COUNT_TIME_INTERVAL = 10000;
var IM_QUERY_NEW_MESSAGES_TIME_INTERVAL = 2000;
var MAX_SCROLL_HEIGHT = 99999999;
var imInProgress = false;
var imTimeout = null;
jQuery(".im-form").live("submit", function(e) {
	e.preventDefault();
	queryIM(true);
});
jQuery(".im-messages .user").live("click", function(e) {
	e.preventDefault();
	jQuery(".im-form [name=receiver]").val(jQuery(this).text());
});
jQuery(function(){
	queryIM();
});
function onIMLoad() { // Called from im.jsp
	if (!imInProgress) {
		jQuery("a.messagenotify .count").text(0).toggle(false);
		jQuery(".im-messages").scrollTop(MAX_SCROLL_HEIGHT);
	}
}
function queryIM(submitted) {
	if (imInProgress) {
		return;
	}
	imInProgress = true;
	if (imTimeout) {
		clearTimeout(imTimeout);
		imTimeout = null;
	}
	var form = jQuery(".im-form");
	var imNotify = jQuery(".messagenotify");
	var url = imNotify.attr('href');
	var datas;
	if (submitted) {
		datas = form.serializeArray();
	} else {
		datas = {
			lastMessageId : form.find("[name=lastMessageId]").val() || -1
		};
	}
	jQuery.ajax({
		type : 'POST',
		url : url,
		data : datas,
		dataType : 'html',
		success : function(data) {
			var dom = jQuery("<div/>").html(data);
			var newMessages = dom.find(".im-messages li");
			if(newMessages.length > 0) {
				jQuery(".im-messages").append(newMessages).scrollTop(MAX_SCROLL_HEIGHT);
			}
			var form = jQuery(".im-form");
			if(form.length == 0) {
				jQuery("a.messagenotify .count").text(newMessages.length)
					.toggle(newMessages.length > 0);
			} else {
				if (submitted) {
					form.find("[name=message]").val("");
				}
				form.find("[name=lastMessageId]").val(dom.find("[name=lastMessageId]").val());
				var receiver = form.find("[name=receiver]");
				var receiverValue = receiver.val();
				receiver.children().remove();
				receiver.append(dom.find("[name=receiver] option"));
				receiver.val(receiverValue);
			}
			imInProgress = false;
			imTimeout = setTimeout(queryIM, (form.length == 0 ? IM_QUERY_UNREAD_COUNT_TIME_INTERVAL : IM_QUERY_NEW_MESSAGES_TIME_INTERVAL));
		}
	});
}