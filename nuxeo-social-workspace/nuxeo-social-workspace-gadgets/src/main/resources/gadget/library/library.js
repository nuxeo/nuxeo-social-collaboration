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

function confirmDeleteDocument(targetRef, targetTitle){
	message = 'Delete "' +  targetTitle + '" ?';
	code = 'deleteDocument( \'' + targetRef + '\' );' ;
	showConfirmationPopup(message, code);
}

// delete specified document from repository
function deleteDocument(targetRef){
	data = loadContext();
	data.targetRef = targetRef;
	loadContent(getBasePath() + '/' + "deleteDocument", data);
}

function confirmPublishDocument(targetRef, targetTitle, public){
	if ( public ) {
		message = 'Make the document "' +  targetTitle + '" public?';
	} else {
		message = 'Restrict the document "' +  targetTitle + '" to the social workspace?';
	}
	code = 'publishDocument( \'' + targetRef + '\', ' + public + ' );' ;
	showConfirmationPopup(message, code);
}

// publish targetDocument
function publishDocument(targetRef, public){
	data = loadContext();
	data.targetRef = targetRef;
	if ( typeof public != 'undefined' ) {
		data.public = public;
	}
	loadContent(getBasePath() + '/' + "publishDocument", data);
}

function goToDocument(path) {
	window.parent.location = top.nxContextPath + "/nxpath/" + getTargetRepository() + path + "@view_documents";
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


// display an confirmation dialog
// message - message that will be displayed
// code - code that will be executed(as string) if ok button is pressed
function showConfirmationPopup(message, code ) {
	content = '<h3>' + message + '</h3>';
	content += '<button class="border" name="ok" type="button" onclick="jQuery.fancybox.close();'+ code +'">OK</button>';
	content += '<button class="border" name="cancel" type="button" onclick="jQuery.fancybox.close()">Cancel</button>';
	jQuery.fancybox(
		content,
		{
			'showCloseButton'	: false,
        	'autoDimensions'	: false,
			'width'         	: 350,
			'height'        	: 'auto',
			'transitionIn'		: 'none',
			'transitionOut'		: 'none'
		}
	);
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