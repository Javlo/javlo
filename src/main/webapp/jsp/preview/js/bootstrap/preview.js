var editPreview = editPreview||{};

+function($,jQuery,pjq) {

function handleDragStart(e) {
  this.style.opacity = '0.4';  // this / e.target is the source node.
}

editPreview.layerOver = function(item, title) {	
	var layer = pjq("#preview-layer");
	
	var insideLayer = pjq("#preview-layer span");	
	if (item == null) {		
		layer.css("z-index", -1);
		layer.css("display", "none");		
		layer.data("compType", null);
		layer.data("sharedContent", null);
		layer.attr("title", " ");
	} else {
		if (item.getAttribute("data-name") != null && item.getAttribute("data-name").length > 0) {
			pjq("#preview-layer h4").html(item.getAttribute("data-name"));
			pjq("#preview-layer").removeClass("nocommand");
		} else {
			pjq("#preview-layer h4").html(i18n_first_component);
			pjq("#preview-layer").addClass("nocommand");
		}
		
		var comp = pjq(item);
		
		layer.css("z-index", 10010);
		layer.css("display", "block");		
		
		layer.css("top", comp.offset().top);
		layer.css("left", comp.offset().left);
		
		var width = comp.outerWidth(false);
		if (width > comp.parent().outerWidth(false)) {
			width = comp.parent().outerWidth(false);
		}
		layer.css("width", width);			
		layer.css("height", comp.outerHeight(false));
		
		layer.data("comp", comp);
	}
}

editPreview.openModal = function (title, url) {	
	editPreview.layerOver(null);	
	pjq('#preview-modal-frame').attr("src", url);	
	pjq('#previewModalTitle').html(title);	
	pjq('#preview-modal').modal('show');
	pjq('#preview-modal').on('hidden.bs.modal', function (e) {
		pjq('#previewModalTitle').html('');
		pjq('#preview-modal-frame').attr("src", pjq('#preview-modal-frame').data("wait"));
	});
}

editPreview.initPreview = function() {
	
  /** prepare preview * */
	
	if (pjq("#preview-layer").length == 0) {
		pjq("body").append('<div id="preview-layer"><div class="commands btn-group btn-group-sm" role="group"><button class="delete btn btn-primary"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span>delete</button></div><h4></h4><div class="main">&nbsp;</span></div>');
		pjq("#preview-layer").css("position", "absolute");
		pjq("#preview-layer").on('mouseleave', function (event) {	    	
			editPreview.layerOver(null);
	    });
		pjq("#preview-layer").on('click', function (event) {
			var compId = pjq(this).data("comp").attr("id").substring(3);
			var editURL = editPreviewURL + "&comp_id=" + compId;
			editPreview.openModal(i18n_preview_edit, editURL);			
			pjq('#preview-layer .delete').on('click', function (e) {				
				var subComp = pjq(this).parent().parent().data("comp");
				var compId = subComp.attr("id").substring(3);
				var ajaxURL = editPreview.addParam(currentURL,"webaction=edit.delete&id=" + compId);
				editPreview.ajaxPreviewRequest(ajaxURL, null, null);
				editPreview.layerOver(null);
				return false;
			});			
	    });
		
		/** drag and drop layer **/
		var drop = document.querySelectorAll('#preview-layer'), el = null;
		el = drop[0];	
		el.addEventListener('dragover', function (event) {
	    	event.preventDefault();    	
	    	return false;
	    });
		el.setAttribute('draggable', 'true');  
		el.addEventListener('dragstart', function (event) {
			var subComp = pjq(this).data("comp");			
			event.dataTransfer.setData('compId', subComp.attr("id").substring(3));
			pjq(".free-edit-zone").addClass("open");
		});
		el.addEventListener('dragend', function (event) {
			pjq(".free-edit-zone").removeClass("open");
		});
		el.addEventListener('drop', function (event) {
			event.preventDefault();			
			var compType = event.dataTransfer.getData("type");			
			var compId = event.dataTransfer.getData("compId");
			if (compType != null && compType.length > 0) { // insert new component
				var subComp = pjq(this).data("comp");		
				var previewId = subComp.attr("id").substring(3);		
				var area = subComp.parent().attr("id");		
				var ajaxURL = editPreview.addParam(currentURL,"webaction=edit.insert&type=" + compType + "&previous=" + previewId + "&area=" + area+ "&render-mode=3&init=true");
				editPreview.ajaxRequest(ajaxURL, null, null);
			} else if (compId != null) { // move component
				var subComp = pjq(this).data("comp");
				var previewId = subComp.attr("id").substring(3);				
				var area = subComp.parent().attr("id");		
				var ajaxURL = editPreview.addParam(currentURL,"webaction=edit.moveComponent&comp-id=" + compId + "&previous=" + previewId + "&area=" + area+ "&render-mode=3&init=true");
				editPreview.ajaxPreviewRequest(ajaxURL, null);
			}
			return false;	
		})
	}	

	/** drag and drop **/	
	var drag = document.querySelectorAll('#preview_command .component'), el = null;
	for (var i = 0; i < drag.length; i++) {
		el = drag[i];    
		el.setAttribute('draggable', 'true');  
		el.addEventListener('dragstart', function (event) {
			event.dataTransfer.setData('type', this.getAttribute("data-type"));
			pjq(".free-edit-zone").addClass("open");
		});
		el.addEventListener('dragend', function (event) {
			pjq(".free-edit-zone").removeClass("open");
		});
		el.addEventListener('drop', function (event) { 
			event.preventDefault();    	
		});    
	}
	var drop = document.querySelectorAll('.editable-component'), el = null;
	for (var i = 0; i < drop.length; i++) {
		el = drop[i]; 
	    el.addEventListener('dragover', function (event) {
	    	event.preventDefault();
	    	editPreview.layerOver(this);
	    	return false;
	    });
	    el.addEventListener('mouseover', function (event) {	    	
	    	editPreview.layerOver(this);
	    });
	}
	var drop = document.querySelectorAll('.editable-component'), el = null;
	for (var i = 0; i < drop.length; i++) {
		el = drop[i]; 
	    el.addEventListener('dragover', function (event) {
	    	event.preventDefault();
	    	editPreview.layerOver(this);
	    	return false;
	    });
	    el.addEventListener('mouseover', function (event) {	    	
	    	editPreview.layerOver(this);
	    });
	}
	var drop = document.querySelectorAll('._empty_area'), el = null;
	for (var i = 0; i < drop.length; i++) {
		el = drop[i]; 
	    el.addEventListener('dragover', function (event) {
	    	event.preventDefault();
	    	pjq(this).addClass("drop-selected");
	    	return false;
	    });
	    el.addEventListener('dragleave', function (event) {
	    	event.preventDefault();
	    	pjq(this).removeClass("drop-selected");
	    	return false;
	    });
	    el.addEventListener('drop', function (event) {	    	
	    	var compType = event.dataTransfer.getData("type");			
			var compId = event.dataTransfer.getData("compId");
			var area = pjq(this).parent().attr("id");
			if (compType != null && compType.length > 0) { // insert new component
				pjq(this).removeClass("drop-selected");
				var subComp = pjq(this).data("comp");										
				var ajaxURL = editPreview.addParam(currentURL,"previewEdit=true&webaction=edit.insert&type=" + compType + "&previous=0&area=" + area+ "&render-mode=3&init=true");
				editPreview.ajaxPreviewRequest(ajaxURL, null);
			} else if (compId != null) { // move component
				var subComp = pjq(this).data("comp");										
				var ajaxURL = editPreview.addParam(currentURL,"previewEdit=true&webaction=edit.moveComponent&comp-id=" + compId + "&previous=0&area=" + area+ "&render-mode=3&init=true");
				editPreview.ajaxPreviewRequest(ajaxURL, null);
			}
			return false;	
	    });
	}
	
editPreview.reloadPreviewPage = function() {
		var doc = document.documentElement, body = document.body;
		var topScroll = (doc && doc.scrollTop || body && body.scrollTop || 0);
		var currentURL = window.location.href;
		if (currentURL.indexOf("_scrollTo") >= 1) {
			currentURL = currentURL.substring(0,
					currentURL.indexOf("_scrollTo") - 1);
		}
		if (currentURL.indexOf("?") < 0) {
			currentURL = currentURL + "?" + "_scrollTo=" + topScroll;
		} else {
			currentURL = currentURL + "&" + "_scrollTo=" + topScroll;
		}
		window.location.href = currentURL;
	}
	
	
editPreview.ajaxPreviewRequest = function(url, doneFunction) {	
		if (url.indexOf("/edit-")>=0) {
			url = url.replace("/edit-", "/ajax-");
		} else {
			url = url.replace("/edit/", "/ajax/");
			if (url.indexOf("/preview-")>=0) {
				url = url.replace("/preview-", "/ajax-");
			} else {
				url = url.replace("/preview/", "/ajax/");
			}
		}	
		var data=null;
		var formDataSpecific = undefined;		
		jQuery.ajax({
			url : url,
			cache : false,
			contentType: formDataSpecific,
			processData: formDataSpecific,
			data : data,
			type : "post",
			dataType : "json"
		}).done(function(jsonObj) {			
			if (jsonObj.data != null) {
				if (jsonObj.data["need-refresh"]) {
					editPreview.reloadPreviewPage();
				}
			}
			jQuery.each(jsonObj.zone, function(xhtmlId, xhtml) {
				if (xhtmlId.indexOf("#") < 0 && xhtmlId.indexOf("#") < 0 && xhtmlId.indexOf(" ") < 0 ) { // if allready select don't add '#'
					xhtmlId = "#"+xhtmlId;
				}
				var item = jQuery(xhtmlId);			
				if (item != null) {
					jQuery(xhtmlId).replaceWith(xhtml);
				} else {
					jQuery.each(jsonObj.data, function(key, value) {				
				});
					if (console) {
						console.log("warning : component "+xhtmlId+" not found for zone.");
					}
				}
			});
			jQuery.each(jsonObj.insideZone, function(xhtmlId, xhtml) {
				if (xhtmlId.indexOf("#") < 0 && xhtmlId.indexOf(".") < 0 && xhtmlId.indexOf(" ") < 0 ) { // if allready select don't add '#'				
					xhtmlId = "#"+xhtmlId;
				}			
				var item = jQuery(xhtmlId);
				if (item != null) {
					item.html(xhtml);	
				} else {
					if (console) {
						console.log("warning : component "+xhtmlId+" not found for insideZone.");
					}
				}
			});			
			jQuery(document).trigger("ajaxUpdate");		
			try {			
				editPreview.initPreview();			
			} catch (ex) {
				if (console) {
					console.log("Exception when calling initPreview()", ex);
				}
			}
			if (doneFunction != null) {			
				doneFunction();
			}
		});	
	}	
}

editPreview.addParam  = function(url, params) {
	if (url.indexOf("?") < 0) {
		url = url + "?" + params;
	} else {
		url = url + "&" + params;
	}
	return url;
}

pjq(document).ready(function() {
	editPreview.initPreview();
});

/** ************ */
/** bootstrap * */
/** ************ */

/** tab * */

/*
 * ========================================================================
 * Bootstrap: tab.js v3.3.2 http://getbootstrap.com/javascript/#tabs
 * ========================================================================
 * Copyright 2011-2015 Twitter, Inc. Licensed under MIT
 * (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ========================================================================
 */


+function ($) {
  'use strict';

  // TAB CLASS DEFINITION
  // ====================

  var Tab = function (element) {
    this.element = $(element)
  }

  Tab.VERSION = '3.3.2'

  Tab.TRANSITION_DURATION = 150

  Tab.prototype.show = function () {
    var $this    = this.element
    var $ul      = $this.closest('ul:not(.dropdown-menu)')
    var selector = $this.data('target')

    if (!selector) {
      selector = $this.attr('href')
      selector = selector && selector.replace(/.*(?=#[^\s]*$)/, '') // strip for
																	// ie7
    }

    if ($this.parent('li').hasClass('active')) return

    var $previous = $ul.find('.active:last a')
    var hideEvent = $.Event('hide.bs.tab', {
      relatedTarget: $this[0]
    })
    var showEvent = $.Event('show.bs.tab', {
      relatedTarget: $previous[0]
    })

    $previous.trigger(hideEvent)
    $this.trigger(showEvent)

    if (showEvent.isDefaultPrevented() || hideEvent.isDefaultPrevented()) return

    var $target = $(selector)

    this.activate($this.closest('li'), $ul)
    this.activate($target, $target.parent(), function () {
      $previous.trigger({
        type: 'hidden.bs.tab',
        relatedTarget: $this[0]
      })
      $this.trigger({
        type: 'shown.bs.tab',
        relatedTarget: $previous[0]
      })
    })
  }

  Tab.prototype.activate = function (element, container, callback) {
    var $active    = container.find('> .active')
    var transition = callback
      && $.support.transition
      && (($active.length && $active.hasClass('fade')) || !!container.find('> .fade').length)

    function next() {
      $active
        .removeClass('active')
        .find('> .dropdown-menu > .active')
          .removeClass('active')
        .end()
        .find('[data-toggle="tab"]')
          .attr('aria-expanded', false)

      element
        .addClass('active')
        .find('[data-toggle="tab"]')
          .attr('aria-expanded', true)

      if (transition) {
        element[0].offsetWidth // reflow for transition
        element.addClass('in')
      } else {
        element.removeClass('fade')
      }

      if (element.parent('.dropdown-menu')) {
        element
          .closest('li.dropdown')
            .addClass('active')
          .end()
          .find('[data-toggle="tab"]')
            .attr('aria-expanded', true)
      }

      callback && callback()
    }

    $active.length && transition ?
      $active
        .one('bsTransitionEnd', next)
        .emulateTransitionEnd(Tab.TRANSITION_DURATION) :
      next()

    $active.removeClass('in')
  }


  // TAB PLUGIN DEFINITION
  // =====================

  function Plugin(option) {
    return this.each(function () {
      var $this = $(this)
      var data  = $this.data('bs.tab')

      if (!data) $this.data('bs.tab', (data = new Tab(this)))
      if (typeof option == 'string') data[option]()
    })
  }

  var old = $.fn.tab

  $.fn.tab             = Plugin
  $.fn.tab.Constructor = Tab


  // TAB NO CONFLICT
  // ===============

  $.fn.tab.noConflict = function () {
    $.fn.tab = old
    return this
  }


  // TAB DATA-API
  // ============

  var clickHandler = function (e) {
    e.preventDefault()
    Plugin.call($(this), 'show')
  }

  $(document)
    .on('click.bs.tab.data-api', '[data-toggle="tab"]', clickHandler)
    .on('click.bs.tab.data-api', '[data-toggle="pill"]', clickHandler)

}(jQuery);

/*
 * ========================================================================
 * Bootstrap: modal.js v3.3.2 http://getbootstrap.com/javascript/#modals
 * ========================================================================
 * Copyright 2011-2015 Twitter, Inc. Licensed under MIT
 * (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ========================================================================
 */


+function ($) {
  'use strict';

  // MODAL CLASS DEFINITION
  // ======================

  var Modal = function (element, options) {
    this.options        = options
    this.$body          = $(document.body)
    this.$element       = $(element)
    this.$backdrop      =
    this.isShown        = null
    this.scrollbarWidth = 0

    if (this.options.remote) {
      this.$element
        .find('.modal-content')
        .load(this.options.remote, $.proxy(function () {
          this.$element.trigger('loaded.bs.modal')
        }, this))
    }
  }

  Modal.VERSION  = '3.3.2'

  Modal.TRANSITION_DURATION = 300
  Modal.BACKDROP_TRANSITION_DURATION = 150

  Modal.DEFAULTS = {
    backdrop: true,
    keyboard: true,
    show: true
  }

  Modal.prototype.toggle = function (_relatedTarget) {
    return this.isShown ? this.hide() : this.show(_relatedTarget)
  }

  Modal.prototype.show = function (_relatedTarget) {
    var that = this
    var e    = $.Event('show.bs.modal', { relatedTarget: _relatedTarget })

    this.$element.trigger(e)

    if (this.isShown || e.isDefaultPrevented()) return

    this.isShown = true

    this.checkScrollbar()
    this.setScrollbar()
    this.$body.addClass('modal-open')

    this.escape()
    this.resize()

    this.$element.on('click.dismiss.bs.modal', '[data-dismiss="modal"]', $.proxy(this.hide, this))

    this.backdrop(function () {
      var transition = $.support.transition && that.$element.hasClass('fade')

      if (!that.$element.parent().length) {
        that.$element.appendTo(that.$body) // don't move modals dom position
      }

      that.$element
        .show()
        .scrollTop(0)

      if (that.options.backdrop) that.adjustBackdrop()
      that.adjustDialog()

      if (transition) {
        that.$element[0].offsetWidth // force reflow
      }

      that.$element
        .addClass('in')
        .attr('aria-hidden', false)

      that.enforceFocus()

      var e = $.Event('shown.bs.modal', { relatedTarget: _relatedTarget })

      transition ?
        that.$element.find('.modal-dialog') // wait for modal to slide in
          .one('bsTransitionEnd', function () {
            that.$element.trigger('focus').trigger(e)
          })
          .emulateTransitionEnd(Modal.TRANSITION_DURATION) :
        that.$element.trigger('focus').trigger(e)
    })
  }

  Modal.prototype.hide = function (e) {
    if (e) e.preventDefault()

    e = $.Event('hide.bs.modal')

    this.$element.trigger(e)

    if (!this.isShown || e.isDefaultPrevented()) return

    this.isShown = false

    this.escape()
    this.resize()

    $(document).off('focusin.bs.modal')

    this.$element
      .removeClass('in')
      .attr('aria-hidden', true)
      .off('click.dismiss.bs.modal')

    $.support.transition && this.$element.hasClass('fade') ?
      this.$element
        .one('bsTransitionEnd', $.proxy(this.hideModal, this))
        .emulateTransitionEnd(Modal.TRANSITION_DURATION) :
      this.hideModal()
  }

  Modal.prototype.enforceFocus = function () {
    $(document)
      .off('focusin.bs.modal') // guard against infinite focus loop
      .on('focusin.bs.modal', $.proxy(function (e) {
        if (this.$element[0] !== e.target && !this.$element.has(e.target).length) {
          this.$element.trigger('focus')
        }
      }, this))
  }

  Modal.prototype.escape = function () {
    if (this.isShown && this.options.keyboard) {
      this.$element.on('keydown.dismiss.bs.modal', $.proxy(function (e) {
        e.which == 27 && this.hide()
      }, this))
    } else if (!this.isShown) {
      this.$element.off('keydown.dismiss.bs.modal')
    }
  }

  Modal.prototype.resize = function () {
    if (this.isShown) {
      $(window).on('resize.bs.modal', $.proxy(this.handleUpdate, this))
    } else {
      $(window).off('resize.bs.modal')
    }
  }

  Modal.prototype.hideModal = function () {
    var that = this
    this.$element.hide()
    this.backdrop(function () {
      that.$body.removeClass('modal-open')
      that.resetAdjustments()
      that.resetScrollbar()
      that.$element.trigger('hidden.bs.modal')
    })
  }

  Modal.prototype.removeBackdrop = function () {
    this.$backdrop && this.$backdrop.remove()
    this.$backdrop = null
  }

  Modal.prototype.backdrop = function (callback) {
    var that = this
    var animate = this.$element.hasClass('fade') ? 'fade' : ''

    if (this.isShown && this.options.backdrop) {
      var doAnimate = $.support.transition && animate

      this.$backdrop = $('<div class="modal-backdrop ' + animate + '" />')
        .prependTo(this.$element)
        .on('click.dismiss.bs.modal', $.proxy(function (e) {
          if (e.target !== e.currentTarget) return
          this.options.backdrop == 'static'
            ? this.$element[0].focus.call(this.$element[0])
            : this.hide.call(this)
        }, this))

      if (doAnimate) this.$backdrop[0].offsetWidth // force reflow

      this.$backdrop.addClass('in')

      if (!callback) return

      doAnimate ?
        this.$backdrop
          .one('bsTransitionEnd', callback)
          .emulateTransitionEnd(Modal.BACKDROP_TRANSITION_DURATION) :
        callback()

    } else if (!this.isShown && this.$backdrop) {
      this.$backdrop.removeClass('in')

      var callbackRemove = function () {
        that.removeBackdrop()
        callback && callback()
      }
      $.support.transition && this.$element.hasClass('fade') ?
        this.$backdrop
          .one('bsTransitionEnd', callbackRemove)
          .emulateTransitionEnd(Modal.BACKDROP_TRANSITION_DURATION) :
        callbackRemove()

    } else if (callback) {
      callback()
    }
  }

  // these following methods are used to handle overflowing modals

  Modal.prototype.handleUpdate = function () {
    if (this.options.backdrop) this.adjustBackdrop()
    this.adjustDialog()
  }

  Modal.prototype.adjustBackdrop = function () {
    this.$backdrop
      .css('height', 0)
      .css('height', this.$element[0].scrollHeight)
  }

  Modal.prototype.adjustDialog = function () {
    var modalIsOverflowing = this.$element[0].scrollHeight > document.documentElement.clientHeight

    this.$element.css({
      paddingLeft:  !this.bodyIsOverflowing && modalIsOverflowing ? this.scrollbarWidth : '',
      paddingRight: this.bodyIsOverflowing && !modalIsOverflowing ? this.scrollbarWidth : ''
    })
  }

  Modal.prototype.resetAdjustments = function () {
    this.$element.css({
      paddingLeft: '',
      paddingRight: ''
    })
  }

  Modal.prototype.checkScrollbar = function () {
    this.bodyIsOverflowing = document.body.scrollHeight > document.documentElement.clientHeight
    this.scrollbarWidth = this.measureScrollbar()
  }

  Modal.prototype.setScrollbar = function () {
    var bodyPad = parseInt((this.$body.css('padding-right') || 0), 10)
    if (this.bodyIsOverflowing) this.$body.css('padding-right', bodyPad + this.scrollbarWidth)
  }

  Modal.prototype.resetScrollbar = function () {
    this.$body.css('padding-right', '')
  }

  Modal.prototype.measureScrollbar = function () { // thx walsh
    var scrollDiv = document.createElement('div')
    scrollDiv.className = 'modal-scrollbar-measure'
    this.$body.append(scrollDiv)
    var scrollbarWidth = scrollDiv.offsetWidth - scrollDiv.clientWidth
    this.$body[0].removeChild(scrollDiv)
    return scrollbarWidth
  }


  // MODAL PLUGIN DEFINITION
  // =======================

  function Plugin(option, _relatedTarget) {
    return this.each(function () {
      var $this   = $(this)
      var data    = $this.data('bs.modal')
      var options = $.extend({}, Modal.DEFAULTS, $this.data(), typeof option == 'object' && option)

      if (!data) $this.data('bs.modal', (data = new Modal(this, options)))
      if (typeof option == 'string') data[option](_relatedTarget)
      else if (options.show) data.show(_relatedTarget)
    })
  }

  var old = $.fn.modal

  $.fn.modal             = Plugin
  $.fn.modal.Constructor = Modal


  // MODAL NO CONFLICT
  // =================

  $.fn.modal.noConflict = function () {
    $.fn.modal = old
    return this
  }


  // MODAL DATA-API
  // ==============

  $(document).on('click.bs.modal.data-api', '[data-toggle="modal"]', function (e) {
    var $this   = $(this)
    var href    = $this.attr('href')
    var $target = $($this.attr('data-target') || (href && href.replace(/.*(?=#[^\s]+$)/, ''))) // strip
																								// for
																								// ie7
    var option  = $target.data('bs.modal') ? 'toggle' : $.extend({ remote: !/#/.test(href) && href }, $target.data(), $this.data())

    if ($this.is('a')) e.preventDefault()

    $target.one('show.bs.modal', function (showEvent) {
      if (showEvent.isDefaultPrevented()) return // only register focus
													// restorer if modal will
													// actually get shown
      $target.one('hidden.bs.modal', function () {
        $this.is(':visible') && $this.trigger('focus')
      })
    })
    Plugin.call($target, option, this)
  })

}(jQuery);

}(pjq, pjq, pjq);