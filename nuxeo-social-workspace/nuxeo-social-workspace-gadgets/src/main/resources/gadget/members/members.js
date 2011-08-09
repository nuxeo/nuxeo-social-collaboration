pageMax = -1;
page = 0;
delete users;

var NXRequestParams = {
	operationId : 'SocialWorkspace.Members',
    operationParams : {
    	contextPath : getTargetContextPath(),
    	pageSize : 5
	},
	entityType : 'blob',
	operationContext : {},
	operationCallback : operationExecutedCallback
}

function operationExecutedCallback(response, nxParams){
	users = response.data['users'];
	page = response.data['page'];
	pageMax = response.data['pageMax'];
	displayUsers();
}

function doSearch() {
	 query = _gel('query').value;
	 if ( isEmpty(query) ) {
		delete users;
		displayUsers();
		hideErrorMessage();
	 } else {
	  	page = 0;
        NXRequestParams.operationParams.pattern = query;
        getUsers();
	 }
}


function displayUsers(){
	if ( typeof users == 'undefined' ) {
	 	_gel("message").style.display = 'none';
		_gel("list").style.display = 'none';
	} else if ( users.length == 0 ) {
		_gel("message").style.display = 'block';
		_gel("list").style.display = 'none';
		_gel("message").innerHTML = 'No user found'; // to be translated
	} else {
		_gel("message").style.display = 'none';
		_gel("list").style.display = 'block';

		_gel("pageInfo").innerHTML = (page + 1) + '/' + pageMax;
		_gel("listData").innerHTML = buildList();
	}
	gadgets.window.adjustHeight();
}

function buildList() {
	data = "<table class='dataList'><tbody>";
	for ( i = 0 ; i < users.length ; i++ ) {
		data += "<tr>";
		data += "<td>";
		data += "<a href='#' onclick='alert(\"to be implemented...\");return false;'>";
		data += "<img src='" + NXGadgetContext.clientSideBaseUrl + "icons/missing_avatar.png'>";
		data += "</a>";
		data += "</td>";
		data += "<td>" + users[i]['firstName'] + "</td>";
		data += "<td>" + users[i]['lastName'] + "</td>";
		data += "</tr>\n";
	}
	data += "</tbody></table>";
	return data;
}

function getUsers() {
	NXRequestParams.operationParams.page = page;
	doAutomationRequest(NXRequestParams);
}


// called when the gadget is loaded
gadgets.util.registerOnLoadHandler(
	function() {
		_gel('navFirstPage').onclick = function(e) {
			firstPage()
    	};
    	_gel('navPrevPage').onclick = function(e) {
        	prevPage()
    	};
    	_gel('navNextPage').onclick = function(e) {
        	nextPage()
    	};
    	_gel('navLastPage').onclick = function(e) {
        	lastPage()
    	};
		gadgets.window.adjustHeight();
	}
);

function isEmpty(s) {
    return (!s || s.length == 0 );
}


function firstPage(){
	if ( users.length > 0 ) {
		page = 0 ;
		getUsers();
	}
}

function prevPage(){
	if ( users.length > 0 && page > 0) {
		page -- ;
		getUsers();
	}
}

function nextPage(){
	if ( users.length > 0 && page < (pageMax - 1)) {
		page ++ ;
		getUsers();
	}
}

function lastPage(){
	if ( page < pageMax - 1 ) {
		page = pageMax -1 ;
		getUsers();
	}
}