var prefs = new gadgets.Prefs();

var socialWorkspacePath = getTargetContextPath();
var currentMiniMessages = [];
var waitingMiniMessages = [];

var offset = 0;
var limit = 5;
var waitingOffset = 0;
var waitingLimit = 0;

var hasMoreMiniMessages = true;

function displayMiniMessages() {
  var htmlContent = '';

  if (currentMiniMessages.length == 0) {
    htmlContent += '<div class="noStream">' + prefs.getMsg('label.no.mini.message') + '</div>';
  } else {
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
  }

  _gel('miniMessagesContainer').innerHTML = htmlContent;
  if (hasMoreMiniMessages) {
    addMoreMiniMessagesBar();
  } else {
    addNoMoreMiniMessageText();
  }
  gadgets.window.adjustHeight();
}

function addMoreMiniMessagesBar() {
  var bar = document.createElement('div');
  bar.id = 'moreMiniMessagesBar';
  bar.className = 'moreMiniMessagesBar';
  bar.innerHTML = prefs.getMsg('label.show.more.mini.messages');
  bar.onclick = showMoreMiniMessages;
  var container = _gel('miniMessagesContainer');
  container.insertBefore(bar, null);
}

function addNoMoreMiniMessageText() {
  var bar = document.createElement('div');
  bar.id = 'moreMiniMessagesBar';
  bar.className = 'moreMiniMessagesBar noMore';
  bar.innerHTML = prefs.getMsg('label.no.more.mini.messages');
  var container = _gel('miniMessagesContainer');
  container.insertBefore(bar, null);
}

function showMoreMiniMessages() {
  var NXRequestParams= { operationId : 'Services.GetSocialWorkspaceMiniMessages',
    operationParams: {
      language: prefs.getLang(),
      contextPath: socialWorkspacePath,
      offset: offset,
      limit: limit
    },
    operationContext: {},
    operationCallback: function(response, params) {
      var newMiniMessages = response.data.miniMessages;
      if (newMiniMessages.length > 0) {
        currentMiniMessages = currentMiniMessages.concat(response.data.miniMessages);
        offset = response.data.offset;
        limit = response.data.limit;
      } else {
        hasMoreMiniMessages = false;
      }
      displayMiniMessages();
    }
  };

  doAutomationRequest(NXRequestParams);
}

function loadMiniMessages() {
  var NXRequestParams= { operationId : 'Services.GetSocialWorkspaceMiniMessages',
    operationParams: {
      language: prefs.getLang(),
      contextPath: socialWorkspacePath,
      limit: limit
    },
    operationContext: {},
    operationCallback: function(response, params) {
      currentMiniMessages = response.data.miniMessages;
      offset = response.data.offset;
      limit = response.data.limit;
      displayMiniMessages();
    }
  };

  doAutomationRequest(NXRequestParams);
}

function pollMiniMessages() {
var NXRequestParams= { operationId : 'Services.GetSocialWorkspaceMiniMessages',
  operationParams: {
    language: prefs.getLang(),
    contextPath: socialWorkspacePath,
    limit: limit
  },
  operationContext: {},
  operationCallback: function(response, params) {
    var newMiniMessages = response.data.miniMessages;
    if (newMiniMessages.length > 0 && currentMiniMessages[0].id !== newMiniMessages[0].id) {
      // there is at least one new mini message
      waitingMiniMessages = newMiniMessages;
      waitingOffset = response.data.offset;
      waitingLimit = response.data.limit;
      addNewMiniMessagesBar();
      gadgets.window.adjustHeight();
    }
  }
};

doAutomationRequest(NXRequestParams);
}

function addNewMiniMessagesBar() {
  if (document.getElementById('newMiniMessagesBar') !== null) {
    return;
  }

  var bar = document.createElement('div');
  bar.id = 'newMiniMessagesBar';
  bar.className = 'newMiniMessagesBar';
  bar.innerHTML = prefs.getMsg('label.show.new.mini.messages');
  bar.onclick = showNewMiniMessages;
  var container = _gel('miniMessagesContainer');
  container.insertBefore(bar, container.firstChild);
}

function showNewMiniMessages() {
  currentMiniMessages = waitingMiniMessages;
  offset = waitingOffset;
  limit = waitingLimit;
  displayMiniMessages();
}

gadgets.util.registerOnLoadHandler(function() {
  loadMiniMessages();
  window.setInterval(pollMiniMessages, 30*1000);
});
