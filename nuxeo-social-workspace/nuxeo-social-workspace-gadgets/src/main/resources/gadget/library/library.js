function loadContent(docRef, page) {
	if ( typeof page == 'undefined' ) {
		page = 0;
	}
	jQuery("#waitMessage").show();
	jQuery.get(
		top.nxContextPath + "/site/social/browse",
		{
			docRef: docRef,
			language: prefs.getLang(),
			pageSize: 5,
			page: page
		},
		function(data){
			jQuery("#content").html(data)
		});
	jQuery("#waitMessage").hide();
	//gadgets.window.adjustHeight();
}

// called when gadget is load first time
function loadInitialContent() {
	loadContent(getTargetContextPath());
}

gadgets.util.registerOnLoadHandler(loadInitialContent);