// load the page that will display the content of document specified
function documentList(docRef, page){
	data = loadContext();

	if ( typeof page == 'number' ) {
		data.page = page;
	}
	data.pageSize = prefs.getString("pageSize");

	// set new value of docRef
	data.docRef = docRef;

	loadContent(getBasePath() + '/' + "documentList", data);
}

// delete specified document from repository
function deleteDocument(targetRef){
	data = loadContext();
	data.targetRef = targetRef;
	loadContent(getBasePath() + '/' + "deleteDocument", data);
}

// publish targetDocument
function publishDocument(targetRef){
	data = loadContext();
	data.targetRef = targetRef;
	loadContent(getBasePath() + '/' + "publishDocument", data);
}


// load navigation info from context form
function loadContext() {
    context = {};
	jQuery.each(jQuery('[name="contextInfoForm"]').serializeArray(), function(i, field){
		context[field.name]=field.value;
	});
	return context;
}

// used from popup ( eg for create folder)
function submitForm(element) {
	form = element.form;
	data = jQuery(form).serializeArray();
	loadContent(jQuery(form).attr("action"),data);
	jQuery.fancybox.close();
}

function loadContent(path, data) {
	jQuery.post(
		path,
		data,
		contentLoadedHandler
	);
}


//
function contentLoadedHandler(data){
	// set the new content in "content" element
	jQuery("#content").html(data);

	addPopupBoxTo(jQuery(".addPopup"));

	// intercept forms submit event and add custom behavior
	jQuery("form").submit(
		function(event){
		    event.preventDefault();
		 	data = jQuery(this).serializeArray();
		 	data.push({
		 		name: 'pageSize' ,
		 		value : prefs.getString("pageSize")
		 	});
		 	loadContent(jQuery(this).attr("action"),data);
		}
	);

}


// return the path to access social webengine module
function getBasePath() {
	return basePath = top.nxContextPath + '/site/social';
}

function buildPageRequestData(docRef, page, queryText) {
	if ( typeof page == 'undefined' ) {
		page = 0;
	}
	data = {
		docRef: docRef,
		pageSize: prefs.getString("pageSize"),
		page: page
	}
	if ( !(typeof queryText == "undefined")) {
		data.queryText = queryText;
	}

	return data;
}

function addPopupBoxTo(a) {
      jQuery(a).fancybox({
        'width'             : '100%',
        'height'            : '100%',
        'autoScale'         : false,
        'transitionIn'      : 'none',
        'transitionOut'     : 'none',
        'type'              : 'iframe',
        'enableEscapeButton': true,
        'centerOnScroll'	: true,
        'showCloseButton'	: false,
        'padding'			: 0,
        'margin'			: 0,
        'overlayShow'		: false
      });
}

// called when gadget is load first time
function loadInitialContent() {
	documentList(getTargetContextPath());
}

gadgets.util.registerOnLoadHandler(loadInitialContent);