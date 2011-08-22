var prefs = new gadgets.Prefs();

var socialWorkspacePath = getTargetContextPath();
var currentMiniMessages = [];
var waitingMiniMessages = [];

function displayMiniMessages() {
  var htmlContent = '';

  for (var i = 0; i < currentMiniMessages.length; i++) {
    var cssClass = 'miniMessage';
    if (currentMiniMessages[i].isCurrentUserMiniMessage) {
      cssClass += ' owner';
    }

    htmlContent += '<div class="' + cssClass + '">';
    htmlContent += '<div>';
    htmlContent += '<span class="username">' + currentMiniMessages[i].displayActor + '</span>';
    htmlContent += '<span class="timestamp">' + currentMiniMessages[i].publishedDate + '</span>';
    htmlContent += '</div>';
    htmlContent += '<div class="message">';
    htmlContent += currentMiniMessages[i].message;
    htmlContent += '</div>';
    htmlContent += '</div>';
  }

  _gel('miniMessagesContainer').innerHTML = htmlContent;
  gadgets.window.adjustHeight();
}

function loadMiniMessages() {
  var NXRequestParams= { operationId : 'Services.GetSocialWorkspaceMiniMessages',
    operationParams: {
      language: prefs.getLang(),
      contextPath: socialWorkspacePath
    },
    operationContext: {},
    operationCallback: function(response, params) {
      currentMiniMessages = response.data;
      displayMiniMessages();
    }
  };

  doAutomationRequest(NXRequestParams);
}

function pollMiniMessages() {
var NXRequestParams= { operationId : 'Services.GetSocialWorkspaceMiniMessages',
  operationParams: {
    language: prefs.getLang(),
    contextPath: socialWorkspacePath
  },
  operationContext: {},
  operationCallback: function(response, params) {
    var newMiniMessages = response.data;
    if (newMiniMessages.length> 0 && currentMiniMessages[0].id !== newMiniMessages[0].id) {
      // there is at least one new mini message
      waitingMiniMessages = newMiniMessages;
      addNewMiniMessagesBar();
      gadgets.window.adjustHeight();
    }
  }
};

doAutomationRequest(NXRequestParams);
}

function addNewMiniMessagesBar() {
var bar = document.createElement('div');
bar.id = 'newMiniMessagesBar';
bar.innerHTML = prefs.getMsg('label.show.new.mini.messages');
bar.onclick = showNewMiniMessages;
var container = _gel('miniMessagesContainer');
container.insertBefore(bar, container.firstChild);
}

function showNewMiniMessages() {
currentMiniMessages = waitingMiniMessages;
displayMiniMessages();
}

gadgets.util.registerOnLoadHandler(function() {
  loadMiniMessages();
  window.setInterval(pollMiniMessages, 30*1000);
});
