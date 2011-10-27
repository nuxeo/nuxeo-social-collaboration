var prefs = new gadgets.Prefs();
// load the page that will display the content of document specified
function documentList(docRef, page){
  data = loadContext();

  if ( typeof page == 'number' ) {
    data.page = page;
  }
  data.limit = prefs.getString("limit");

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





function loadContent(path, data) {
  // add language
  data.lang = prefs.getLang();

  jQuery.post(
    path,
    data,
    contentLoadedHandler
  );
}


// called when iframe  is loaded ; used for multipart forms ( see create_document_form.ftl)
function iframeLoaded(iframe) {
	text=jQuery(iframe).contents().find('body').html();
	if ( !isEmpty(text) ) {
		jQuery.fancybox.close();
		contentLoadedHandler(text);
	}
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
        name: 'limit' ,
        value : prefs.getString("limit")
      });
      loadContent(jQuery(this).attr("action"),data);
    }
  );

  // add the language parameter to all links
  l = prefs.getLang();
  jQuery("a").attr('href', function(i, h) {
    if ( typeof h != 'undefined' ) {
  	  if ( h.indexOf("javascript") == 0 )  { // don't add language to href starting with javascript
  	    return h;
  	  } else {
     	return h + (h.indexOf('?') != -1 ? "&lang=" : "?lang=") + l;
      }
    }
  });

  // remove "alt" attribute from images to avoid be displayed in IE
  if ( jQuery.browser.msie ) {
  	jQuery("img").removeAttr("alt");
  }

  gadgets.window.adjustHeight();
}


// return the path to access social webengine module
function getBasePath() {
  return basePath = top.nxContextPath + '/site/social';
}


// display an confirmation dialog
// message - message that will be displayed
// code - code that will be executed(as string) if ok button is pressed
function showConfirmationPopup(message, code ) {
  t = '<div class="fancyContent">';
  t += '<div class="fancyMessage">' + message + '</div>';
  t += '<div class="center">';
  t += '<button class="border" name="ok" type="button" onclick="jQuery.fancybox.close();'+ code +'">OK</button>';
  t += '<button class="border" name="cancel" type="button" onclick="jQuery.fancybox.close()">Cancel</button>';
  t += '</div>';
  t += '</div>';
  jQuery.fancybox(
    t,
    {
      'showCloseButton'  : false,
      'autoDimensions'  : false,
      'width'           : '94%',
      'height'          : '94%',
      'padding'         : 0,
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
