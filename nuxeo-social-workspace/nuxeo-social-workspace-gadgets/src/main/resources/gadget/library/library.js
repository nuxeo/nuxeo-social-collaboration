// load the page that will display the content of document specified
function browse(docRef, page){
	loadContent("browse", buildPageRequestData(docRef, page));
}

// delete specified document from repository
function deleteDocument(docRef, page){
	loadContent("deleteDocument", buildPageRequestData(docRef, page));
}

function submitForm(submitElement) {
	form = submitElement.form;
	jQuery(form).submit( function (event) {
		 event.preventDefault();

		 jQuery.post(
		 	jQuery(this).attr("action"),
			jQuery(this).serializeArray(),
		 	contentLoadedHandler
		 );
		 jQuery.fancybox.close();
	});
}


function loadContent(path, data) {
	jQuery("#waitMessage").show();
	jQuery.get(
		getBasePath() + '/' + path,
		data,
		contentLoadedHandler
	);
}


function contentLoadedHandler(data){
	jQuery("#content").html(data);
	addPopupBoxTo(jQuery(".addPopup"));
	jQuery("#waitMessage").hide();
	//gadgets.window.adjustHeight();
}


// called when gadget is load first time
function loadInitialContent() {
	browse(getTargetContextPath());
}

// return the path to access social webengine module
function getBasePath() {
	return basePath = top.nxContextPath + '/site/social';
}

function buildPageRequestData(docRef, page) {
	if ( typeof page == 'undefined' ) {
		page = 0;
	}
	data = {
		docRef: docRef,
		pageSize: 5,
		page: page
	}
	return data;
}

function addPopupBoxTo(a) {
      jQuery(a).fancybox({
        'width'             : '80%',
        'height'            : '80%',
        'autoScale'         : true,
        'transitionIn'      : 'none',
        'transitionOut'     : 'none',
        'type'              : 'iframe',
        'enableEscapeButton': true,
        'centerOnScroll'	: true
      });
}

gadgets.util.registerOnLoadHandler(loadInitialContent);