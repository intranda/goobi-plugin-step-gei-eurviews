var simpleTinyMceConfig = {
	selector : '.editor',
	setup : function(editor) {
		editor.on('change', function() {
			tinymce.triggerSave();
		});
	},
	valid_elements : 'p',
	statusbar : true,
	theme : 'modern',
	height : 250,
	plugins: [
		'print code preview fullscreen'
	],
	menu: {},
	toolbar: 'undo redo | alignleft aligncenter alignright alignjustify alignnone | outdent indent | print preview code fullscreen',
	content_css : 'css/content.css',
	init_instance_callback : function(editor) {
		var readOnlyAttr = $("#" + editor.id.replace(":", "\\:")).attr(
				"readonly");
		if (readOnlyAttr === "readonly") {
			editor.setMode("readonly");
		}
		try {
			resizeReferenceFields();
			$(editor.getWin()).bind('resize', function() {
				resizeReferenceFields();
			});
		} catch (error) {
		}
	},
	setup : function(editor) {
		editor.on("blur", function(event, a, b) {
			editor.save();
			$("#" + editor.id.replace(":", "\\:")).trigger("change");
		});
	}

};

var extendedTinyMceConfig = {
	selector : '.editor_extended',
	setup : function(editor) {
		editor.on('change', function() {
			tinymce.triggerSave();
		});
	},
	invalid_elements : 'span',
	statusbar : true,
	theme : 'modern',
	height : 400,
	menu: {},
	plugins : [
			'advlist autolink link image lists charmap print preview hr anchor pagebreak spellchecker',
			'searchreplace wordcount visualblocks visualchars code fullscreen insertdatetime media nonbreaking',
			'save table contextmenu directionality emoticons template paste' ],
	content_css : 'css/content.css',
	toolbar : 'insertfile undo redo | styleselect | bold italic underline | superscript subscript | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image media | table | charmap hr | print preview code fullscreen',
	init_instance_callback : function(editor) {
		var readOnlyAttr = $("#" + editor.id.replace(":", "\\:")).attr(
				"readonly");
		if (readOnlyAttr === "readonly") {
			editor.setMode("readonly");
		}
		try {
			resizeReferenceFields();
			$(editor.getWin()).bind('resize', function() {
				resizeReferenceFields();
			});
		} catch (error) {
		}
	},
	setup : function(editor) {
		editor.on("blur", function(event, a, b) {
			editor.save();
			$("#" + editor.id.replace(":", "\\:")).trigger("change");
		});
	}

};

function saveAllEditors(ajaxData) {
	if (ajaxData === undefined || ajaxData.status == "begin") {
		for (edId in tinyMCE.editors)
			try {
				tinyMCE.editors[edId].save();
			} catch(error) {
					console.log("An error occured while saving editors: ", error);
			}
	}
	if (ajaxData !== undefined && ajaxData.status == "success") {
//		initTinyMce(ajaxData);
		resizeReferenceFields();
	}
}

function renderInputFields(ajaxData) {
	if (ajaxData === undefined || ajaxData.status == "begin") {
		for (edId in tinyMCE.editors)
			try {
				tinyMCE.editors[edId].remove();
				console.log("Removed editor " + edId);
			} catch (error) {
				console.log("Error occured during removing editors; ", error);
			}
	}
	if (ajaxData === undefined || ajaxData.status == "success") {
		initTinyMce(ajaxData);
		resizeReferenceFields();
	}
}

function initTinyMce() {
	console.log("Init tinyMce");
	tinymce.init(simpleTinyMceConfig);
	tinymce.init(extendedTinyMceConfig);
}

$(window).on("load", function() {
	renderInputFields()
	$(".currentLanguage .inner-form").on("resize", function() {
		console.log('resize triggered !');
		resizeReferenceFields();
	})
})

function resizeReferenceFields(ajaxData) {
	if (ajaxData === undefined || ajaxData.status == "success") {
		$(".currentLanguage .form-group").each(function(index, element) {
			var $ref = $(".referenceLanguage .form-group:eq(" + index + ")");
			var refHeight = $ref.height();
			$ref.outerHeight($(element).outerHeight());
		})
	}
}
