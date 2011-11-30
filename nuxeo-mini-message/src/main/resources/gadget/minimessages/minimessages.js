var prefs = new gadgets.Prefs();

var miniMessagesStreamType = prefs.getString("miniMessagesStreamType");
var actor = prefs.getString("actor");

var currentMiniMessages = [];
var waitingMiniMessages = [];

var offset = 0;
var waitingOffset = 0;

var hasMoreMiniMessages = true;

function displayMiniMessages() {
  displayNewMiniMessageForm();

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

function displayNewMiniMessageForm() {
  if (showMiniMessageForm()) {
    var htmlContent = '';
    htmlContent += '<form name="newMiniMessageForm" class="newMiniMessageForm">';
    htmlContent += '<textarea rows="3" name="newMiniMessageText" class="miniMessageText"></textarea>';
    htmlContent += '<p class="newMiniMessageActions">';
    htmlContent += '<span class="miniMessageCounter"></span>';
    htmlContent += '<input class="button writeMiniMessageButton" name="writeMiniMessageButton" type="button" onclick="createMiniMessage()" value="' +prefs.getMsg('command.write') + '" />';
    htmlContent += '</p>';
    htmlContent += '</form>';

    _gel('newMiniMessage').innerHTML = htmlContent;
    updateMiniMessageCounter();
    jQuery('textarea[name="newMiniMessageText"]').keyup(updateMiniMessageCounter);
    gadgets.window.adjustHeight();
  }
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
  var NXRequestParams= { operationId : 'Services.GetMiniMessages',
    operationParams: {
      language: prefs.getLang(),
      actor: actor,
      miniMessagesStreamType: miniMessagesStreamType,
      offset: offset
    },
    operationContext: {},
    operationCallback: function(response, params) {
      var newMiniMessages = response.data.miniMessages;
      if (newMiniMessages.length > 0) {
        currentMiniMessages = currentMiniMessages.concat(response.data.miniMessages);
        offset = response.data.offset;
      } else {
        hasMoreMiniMessages = false;
      }
      displayMiniMessages();
    }
  };

  doAutomationRequest(NXRequestParams);
}


function loadMiniMessages() {
  var NXRequestParams= { operationId : 'Services.GetMiniMessages',
    operationParams: {
      language: prefs.getLang(),
      actor: actor,
      miniMessagesStreamType: miniMessagesStreamType
    },
    operationContext: {},
    operationCallback: function(response, params) {
      currentMiniMessages = response.data.miniMessages;
      offset = response.data.offset;
      displayMiniMessages();
    }
  };

  doAutomationRequest(NXRequestParams);
}

function pollMiniMessages() {
var NXRequestParams= { operationId : 'Services.GetMiniMessages',
  operationParams: {
    language: prefs.getLang(),
    actor: actor,
    miniMessagesStreamType: miniMessagesStreamType
  },
  operationContext: {},
  operationCallback: function(response, params) {
    var newMiniMessages = response.data.miniMessages;
    if (newMiniMessages.length > 0 && currentMiniMessages[0].id !== newMiniMessages[0].id) {
      // there is at least one new mini message
      waitingMiniMessages = newMiniMessages;
      waitingOffset = response.data.offset;
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
  displayMiniMessages();
}

gadgets.util.registerOnLoadHandler(function() {
  loadMiniMessages();
  window.setInterval(pollMiniMessages, 30*1000);
});

function showMiniMessageForm() {
  return miniMessagesStreamType == 'forActor';
}

function updateMiniMessageCounter() {
  var delta = 140 - jQuery('textarea[name="newMiniMessageText"]').val().length;
  var miniMessageCounter = jQuery('.miniMessageCounter');
  miniMessageCounter.text(delta);
  miniMessageCounter.toggleClass('warning', delta < 5);
  if (delta < 0) {
    jQuery('.writeMiniMessageButton').attr('disabled', 'disabled');
  } else {
    jQuery('.writeMiniMessageButton').removeAttr('disabled');
  }
}

function createMiniMessage(){
  var miniMessageText = jQuery('textarea[name="newMiniMessageText"]').val();
  var opCallParameters = {
     operationId : 'Services.AddMiniMessage',
     operationParams : {
       message : miniMessageText,
       language : prefs.getLang()
     },
     entityType : 'blob',
     operationContext : {},
     operationCallback : function(response, opCallParameters) {
       loadMiniMessages();
     }
  };
  doAutomationRequest(opCallParameters);
}
