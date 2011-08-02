var prefs = new gadgets.Prefs();
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
  message = prefs.getMsg("label.gadget.library.delete")+' "' +  targetTitle + '"'+ prefs.getMsg("label.gadget.library.interrogation.mark");
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
    message = prefs.getMsg("label.gadget.library.make.public.begining")+' "' +  targetTitle + '" '+prefs.getMsg("label.gadget.library.make.public.end");
  } else {
    message = prefs.getMsg("label.gadget.library.make.restricted.begining")+' "' +  targetTitle + '" '+prefs.getMsg("label.gadget.library.make.restricted.end");
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

function goToDocument(path, viewId) {
  window.parent.location = top.nxContextPath + "/nxpath/" + getTargetRepository() + path + "@" + viewId;
}

// load navigation info from context form
function loadContext() {
    context = {};
  jQuery.each(jQuery('[name="contextInfoForm"]').serializeArray(), function(i, field){
    context[field.name]=field.value;
  });
  return context;
}


// called when iframe  is loaded ; used for multipart forms ( see create_document_form.ftl)
function iframeLoaded(iframe) {
	text=jQuery(iframe).contents().find('body').html();
	if ( !isEmpty(text ) ) {
		 jQuery("#content").html(text);
		jQuery.fancybox.close();
	}
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
      'showCloseButton'  : false,
      'autoDimensions'  : false,
      'width'           : 350,
      'height'          : 'auto',
      'transitionIn'    : 'none',
      'transitionOut'    : 'none'
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
        'centerOnScroll'  : true,
        'showCloseButton'  : false,
        'padding'      : 0,
        'margin'      : 0,
        'overlayShow'    : false
      });
}

// called when gadget is load first time
function loadInitialContent() {
  documentList(getTargetContextPath());
}

function isEmpty(s) {
    return (!s || s.length === 0 );
}



gadgets.util.registerOnLoadHandler(loadInitialContent);