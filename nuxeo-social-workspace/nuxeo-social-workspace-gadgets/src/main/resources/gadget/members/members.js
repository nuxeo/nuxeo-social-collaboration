var prefs = new gadgets.Prefs();

var pageMax = -1;
var page = 0;
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
  var query = _gel('query').value;
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
    _gel("message").style.display = 'block';
    _gel("list").style.display = 'none';
    _gel("message").innerHTML = prefs.getMsg('label.gadget.members.description');
  } else if ( users.length == 0 ) {
    _gel("message").style.display = 'block';
    _gel("list").style.display = 'none';
    _gel("message").innerHTML = prefs.getMsg('label.gadget.members.no.result');
  } else {
    _gel("message").style.display = 'none';
    _gel("list").style.display = 'block';

    _gel("pageInfo").innerHTML = (page + 1) + '/' + pageMax;
    _gel("listData").innerHTML = buildList();

    $(".user").click(function(){
      window.parent.location = $(this).find("a").attr("href");
      return false;
    });
  }
  gadgets.window.adjustHeight();
}

function buildList() {
  var html = '<div class="usersList">';
  for (var i = 0; i < users.length; i++) {
    html += '<div class="user">';
    html += '<span class="avatar">';
    html += '<a target="_top" href="' + users[i]['profileURL'] + '">';
    html += '<img src="' + users[i]['avatarURL'] + '">';
    html += '</a>';
    html += '</span>';
    html += '<span>' + users[i]['firstName'] + '</span>';
    html += '<span>' + users[i]['lastName'] + '</span>';
    html += '</div>';
    html += '</div>';
  }
  return html;
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
