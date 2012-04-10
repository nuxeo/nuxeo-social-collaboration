var prefs = new gadgets.Prefs();

var socialWorkspacePath = getTargetContextPath();


var NXRequestParams = {
  operationId:'SocialWorkspace.UserStatus',
  operationParams:{
    contextPath:socialWorkspacePath
  },
  entityType:'blob',
  operationContext:{},
  operationCallback:displayStatus
};


function disableButton() {
  _gel("statusMessage").innerHTML = "__MSG_label.request.registered__";
  _gel("joinButton").style.display = 'none';
}

function displaySocialWorkspaceDescription(entries) {
  var msg = ""
  if (entries.length > 0) {
    msg = entries[0].properties["dc:description"];
  }
  _gel("socialWorkspaceDescription").innerHTML = msg;
  gadgets.window.adjustHeight();
}

function displayStatus(response, nxParams) {
  var status = response.data["status"];
  _gel("socialWorkspaceDescription").innerHTML = response.data["description"];
  if ("REQUEST_PENDING" == status) {
    _gel("statusMessage").innerHTML = prefs.getMsg("label.request.alreadyRegistered");
    _gel("joinButton").style.display = 'none';
  }
  if ("REQUEST_ACCEPTED" == status) {
    _gel("statusMessage").innerHTML = prefs.getMsg("label.request.accepted");
    _gel("joinButton").style.display = 'none';
  }
  if ("REQUEST_REJECTED" == status) {
    _gel("statusMessage").innerHTML = prefs.getMsg("label.request.rejected");
    _gel("joinButton").style.display = 'none';
  }
  if ("MEMBER" == status) {
    var html = '';
    html += '<p>';
    html += prefs.getMsg('label.request.alreadyMember1');

    var locationHref = window.parent.location.href;
    var index = locationHref.indexOf('?');
    if (index > -1) {
      locationHref = locationHref.substring(0, index);
    }
    locationHref = locationHref.substring(locationHref.indexOf(NXGadgetContext.clientSideBaseUrl)
        + NXGadgetContext.clientSideBaseUrl.length);
    var link = NXGadgetContext.clientSideBaseUrl + 'logout?requestedUrl=' + encodeURIComponent(encodeURIComponent(locationHref.replace(NXGadgetContext.clientSideBaseUrl, '')));
    html += ' <a href="' + link + '" target="_top">' + prefs.getMsg('label.request.alreadyMember2') + '</a> ';
    html += prefs.getMsg('label.request.alreadyMember3');
    html += '</p>';

    _gel("statusMessage").innerHTML = html;
    _gel("joinButton").style.display = 'none';
  }
  gadgets.window.adjustHeight();
}

// execute automation request onload
gadgets.util.registerOnLoadHandler(function () {
  doAutomationRequest(NXRequestParams);
  gadgets.window.adjustHeight();
});

var NXJoinRequestParams = {
  operationId:'Social.Join',
  operationParams:{
    contextPath:socialWorkspacePath
  },
  operationContext:{},
  operationCallback:function () {
    doAutomationRequest(NXRequestParams);
  }
};

function joinSocialWorkspace() {
  doAutomationRequest(NXJoinRequestParams);
}

