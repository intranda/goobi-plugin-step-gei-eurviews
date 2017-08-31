var SEARCH_OPTION_INDEX_IDENTIFIER = 0;
var SEARCH_OPTION_INDEX_PERSON = 1;
var SEARCH_OPTION_INDEX_CORPORATION = 2;



var simpleTinyMceConfig = {
	selector : '.editor',
	setup : function(editor) {
		editor.on('change', function() {
			tinymce.triggerSave();
		});
	},
	valid_elements : 'p,strong,em,span[!style<text-decoration: underline;],sup,',
	statusbar : true,
	theme : 'modern',
	height : 250,
	plugins: [
		'print code preview fullscreen'
	],
	menu: {},
	toolbar: false,
	toolbar: 'undo redo | bold italic underline | superscript | code ',
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
	extended_valid_elements: 'p,span[!style<text-decoration: underline;?text-decoration: line-through;]',
	statusbar : true,
	theme : 'modern',
	height : 400,
	menu: {},
	plugins : [
			'advlist autolink link image lists charmap print preview hr anchor pagebreak spellchecker',
			'searchreplace wordcount visualblocks visualchars code fullscreen insertdatetime media nonbreaking',
			'save table contextmenu directionality emoticons template paste' ],
	content_css : 'css/content.css',
	toolbar : 'undo redo | styleselect | bold italic underline strikethrough | bullist numlist | image table | fullscreen code',  
	spellchecker_languages: 'English=en_US,German=de_DE_frami',
//	spellchecker_rpc_url: 'spellchecker.php',
	 spellchecker_callback: function(method, text, success, failure) {
	      tinymce.util.JSONRequest.sendRPC({
	        url: "template/js/plugins/tinymce/js/tinymce/plugins/spellchecker/spellchecker.php",
	        method: "spellcheck",
	        params: {
	          lang: this.getLanguage(),
	          words: text.match(this.getWordCharPattern())
	        },
	        success: function(result) {
	          success(result);
	        },
	        error: function(error, xhr) {
	        	console.log(error, xhr);
	          failure("Spellcheck error:" + xhr.status);
	        }
	    });
	  },
	style_formats: [
    	{title: 'Headings', items: [
			{title: 'Heading 1', format: 'h1'},
			{title: 'Heading 2', format: 'h2'},
			{title: 'Heading 3', format: 'h3'},
			{title: 'Heading 4', format: 'h4'},
			{title: 'Heading 5', format: 'h5'},
			{title: 'Heading 6', format: 'h6'}
		]},
		{title: 'Blocks', items: [
			{title: 'Paragraph', format: 'p'},
			{title: 'Blockquote', format: 'blockquote'},
			{title: 'Div', format: 'div'},
		]},
	],
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
		editor.on('change', function() {
			tinymce.triggerSave();
		});
	}

};

//init
$(document).ready(function() {
})

function prepareNormdataSearch(element) {
	console.log("set values on ", element);
	var index = $(element).attr('data-row');
	var type = $(element).attr("data-type");
	var database = $(element).attr("data-db");
	var option = getSearchOptionIndex($(element).attr("data-option"));
	console.log("row index = ", index);
	console.log("row Type = ", type);
	console.log("search Database = ", database);
	console.log("option = ", option);
	$('#rowIndex').val(index);
	$('#rowType').val(type);
	$('#searchDatabase').val(database);
	$('#normdataSearchSettings input').trigger("change");
	console.log("row index = ", $('#rowIndex').val());
	console.log("row Type = ", $('#rowType').val());
	console.log("search Database = ", $('#searchDatabase').val());
	prepareNormdataModal(database, option);
}

function showCreateRecordState(data) {
	switch(data.status) {
		case "begin":
			break;
		case "success":
			PF('createRecordResult').show();
			break;
		case "complete":
			break;
	}
}

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
	if(typeof tinyMCE !== 'undefined') {			
		if (ajaxData === undefined || ajaxData.status == "begin") {
			for (edId in tinyMCE.editors) {
				try {
					tinyMCE.editors[edId].remove();
					console.log("Removed editor " + edId);
				} catch (error) {
					console.log("Error occured during removing editors; ", error);
				}
			}
		}
		if (ajaxData === undefined || ajaxData.status == "success") {
			initTinyMce(ajaxData);
			resizeReferenceFields();
		}
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

function prepareNormdataModal(normdataAuthority, searchOptionSelectIndex) {
	if(normdataAuthority == "edu.experts") {
		$("#searchBox").find("#normdata_search_title_gnd").hide();
		$("#searchBox").find("#normdata_search_text_gnd").hide();
		$("#searchBox").find("#normdata_search_title_eduexperts").show();
		$("#searchBox").find("#normdata_search_text_eduexperts").show();
	} else {
		$("#searchBox").find("#normdata_search_title_gnd").show();
		$("#searchBox").find("#normdata_search_text_gnd").show();
		$("#searchBox").find("#normdata_search_title_eduexperts").hide();
		$("#searchBox").find("#normdata_search_text_eduexperts").hide();
	}
//	var normdataId = $(valueSelector).find("input:first").val();
//	if(normdataId) {
//		$("#searchBox").find("#input").val(normdataId);
//		$("#searchOptionSelect").prop("selectedIndex", SEARCH_OPTION_INDEX_IDENTIFIER);
//	} else {							
		$("#searchOptionSelect").prop("selectedIndex",searchOptionSelectIndex);
//	}
}

function getSearchOptionIndex(option) {
	switch(option) {
		case "person":
			return SEARCH_OPTION_INDEX_PERSON;
		case "corporation":
			return SEARCH_OPTION_INDEX_CORPORATION;
		default:
			return SEARCH_OPTION_INDEX_IDENTIFIER;
	}
}
