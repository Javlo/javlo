jQuery(function() {
	tinymce.PluginManager.add('textlang', function(editor, url) {

		var REMOVE_VALUE = "-";

		editor.on('init', function() {
			editor.formatter.register('textlang', {
				inline : 'span',
				attributes : {
					lang : '%value'
				},
				remove : 'all'
			});
		});

		editor.addButton('textlang', function() {
			var items = [], defaultLangs = 'fr en nl';
			var langs = editor.settings.textlang_langs || defaultLangs;

			items.push({
				text : "",
				value : REMOVE_VALUE
			});
			jQuery.each((langs.split == undefined ? langs : langs.split(' ')), function() {
				var text = this, value = this;
				// Allow text=value languages.
				var values = this.split('=');
				if (values.length > 1) {
					text = values[0];
					value = values[1];
				}
				items.push({
					text : text,
					value : value
				});
			});

			return {
				type : 'listbox',
				text : 'Language',
				tooltip : 'Language',
				values : items,
				fixedWidth : true,
				onPostRender : createListBoxChangeHandler(items, 'textlang'),
				onclick : function(e) {
					if (e.control.settings.value) {
						if (e.control.settings.value == REMOVE_VALUE) {
							removeLang();
						} else {
							applyLang(e.control.settings.value);
						}
					}
				}
			};
		});

		function createListBoxChangeHandler(items, formatName) {
			return function() {
				var self = this;

				editor.on('nodeChange', function(e) {
					var formatter = editor.formatter;
					var value = null;

					jQuery.each(e.parents, function(index, node) {
						jQuery.each(items, function(index, item) {
							if (formatName) {
								if (formatter.matchNode(node, formatName, {
									value : item.value
								})) {
									value = item.value;
								}
							} else {
								if (formatter.matchNode(node, item.value)) {
									value = item.value;
								}
							}

							if (value) {
								return false;
							}
						});

						if (value) {
							return false;
						}
					});
					
					self.value(value);
				});
			};
		}

		function applyLang(lang) {
			editor.undoManager.transact(function() {
				editor.focus();
				editor.formatter.apply("textlang", {
					value : lang
				});
				editor.nodeChanged();
			});
		}

		function removeLang() {
			editor.undoManager.transact(function() {
				editor.focus();
				editor.formatter.remove("textlang", {
					value : null
				});
				editor.nodeChanged();
			});
		}

	});
	
})
