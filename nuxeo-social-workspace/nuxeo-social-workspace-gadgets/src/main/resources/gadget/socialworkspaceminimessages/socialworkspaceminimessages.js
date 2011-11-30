var prefs = new gadgets.Prefs();

var socialWorkspacePath = getTargetContextPath();
var currentMiniMessages = [];
var waitingMiniMessages = [];

var offset = 0;
var waitingOffset = 0;

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
  var NXRequestParams= { operationId : 'Services.GetSocialWorkspaceMiniMessages',
    operationParams: {
      language: prefs.getLang(),
      contextPath: socialWorkspacePath
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
var NXRequestParams= { operationId : 'Services.GetSocialWorkspaceMiniMessages',
  operationParams: {
    language: prefs.getLang(),
    contextPath: socialWorkspacePath
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
  fillToolbar();
  loadMiniMessages();
  window.setInterval(pollMiniMessages, 30*1000);
});

// fill the gadget toolbar
function fillToolbar() {
  if (isCreateMessagesActionDisplayed()) {
    createMiniMessageImg = document.createElement('img');
    createMiniMessageImg.src=top.nxContextPath + '/icons/action_add.gif';
    createMiniMessageImg.alt='create minimessage';

    createMiniMessageLink = document.createElement('a');
    createMiniMessageLink.href='#';
    createMiniMessageLink.className='toolbarActions';
    createMiniMessageLink.appendChild(createMiniMessageImg);
    createMiniMessageLink.onclick=showCreateMiniMessagePopup;

    _gel('miniMessagesToolbar').appendChild(createMiniMessageLink);

  }
}

function isCreateMessagesActionDisplayed() {
  return true;
}

function showCreateMiniMessagePopup() {
    var t = '';
    t += '<div class="formContainer">';
    t += '<form name="createMiniMessageForm" class="createMiniMessageForm">';
    t += '<textarea rows="4" name="miniMessageText" class="miniMessageText"></textarea>';
    t += '<p class="newMiniMessageActions">';
    t += '<span class="miniMessageCounter"></span>';
    t += '<button name="ok" type="button" onclick="createMiniMessage()">'+ prefs.getMsg('label.ok') +'</button>';
    t += '<button name="cancel" type="button" onclick="closePopUp()">' + prefs.getMsg('label.cancel') + '</button>';
    t += '</p>';
    t += '</form>';
    t += '</div>';

    jQuery.fancybox(t,
       {
          'width': '100%',
          'autoScale': false,
          'showCloseButton': false,
          'autoDimensions': false,
          'transitionIn': 'none',
          'transitionOut': 'none',
          'padding': 0,
          'margin': 0
       }
    );
    updateMiniMessageCounter();
    jQuery('textarea[name="miniMessageText"]').keyup(updateMiniMessageCounter);
    gadgets.window.adjustHeight(150);
}

function updateMiniMessageCounter() {
    var delta = 140 - jQuery('textarea[name="miniMessageText"]').val().length;
    var miniMessageCounter = jQuery('.miniMessageCounter');
    miniMessageCounter.text(delta);
    miniMessageCounter.toggleClass('warning', delta < 5);
    if (delta < 0) {
        jQuery('button[name="ok"]').attr('disabled', 'disabled');
    } else {
        jQuery('button[name="ok"]').removeAttr('disabled');
    }
}

function closePopUp() {
	jQuery.fancybox.close();
	gadgets.window.adjustHeight();
}

function createMiniMessage(){
  var miniMessageText = jQuery('textarea[name="miniMessageText"]').val();
  var opCallParameters = {
     operationId: 'Services.AddMiniMessage',
     operationParams: {
       message: miniMessageText,
       language: prefs.getLang(),
       contextPath: getTargetContextPath()
     },
     entityType : 'blob',
     operationContext : {},
     operationCallback : function(response, opCallParameters) {
       loadMiniMessages();
     }
  };
  doAutomationRequest(opCallParameters);
  closePopUp();
}
