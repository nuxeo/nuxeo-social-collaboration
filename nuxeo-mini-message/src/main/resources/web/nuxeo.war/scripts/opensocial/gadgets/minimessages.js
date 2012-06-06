(function() {

  /* templates */
  var templates = {};
  templates.miniMessage =
      '<div class="miniMessage {{cssClass}} jsMainActivity" data-activityid="{{id}}" ' +
          'data-allowdeletion="{{allowDeletion}}">' +
        '<div class="container">'+
          '<div class="messageHeader">' +
            '<span class="avatar"><img src="{{actorAvatarURL}}" alt="{{displayActor}}" /></span>' +
            '<span class="username">{{{displayActorLink}}}</span>' +
          '</div>' +
          '<div class="message">{{{activityMessage}}}</div>' +
          '<div class="actions jsActions">' +
            '<span class="timestamp">{{{publishedDate}}}</span>' +
          '</div>' +
        '</div>' +
      '</div>';

  templates.newMiniMessage =
      '<div class="jsNewMiniMessage">' +
        '<form name="newMiniMessageForm" class="newMiniMessageForm">' +
          '<textarea placeholder="{{placeholderMessage}}" rows="3" name="newMiniMessageText" class="miniMessageText jsMiniMessageText"></textarea>' +
          '<p class="newMiniMessageActions">' +
            '<span class="miniMessageCounter jsMiniMessageCounter"></span>' +
            '<input class="button writeMiniMessageButton disabled jsWriteMiniMessageButton" name="writeMiniMessageButton" type="button" value="{{writeLabel}}" disabled="disabled" />' +
          '</p>' +
        '</form>' +
      '</div>';

  templates.deleteActivityAction =
      '<div class="actionItem jsDelete" data-activityid="{{activityId}}">' +
        '<img src="{{deleteImageURL}}" />' +
        '<a href="#">{{deleteMessage}}</a>' +
      '</div>';

  templates.moreActivitiesBar =
    '<div class="moreActivitiesBar jsMoreActivitiesBar">{{moreActivitiesMessage}}</div>';

  templates.noMoreActivitiesBar =
      '<div class="moreActivitiesBar noMore">{{noMoreActivitiesMessage}}</div>';

  templates.newActivitiesBar =
      '<div class="newActivitiesBar jsNewActivitiesBar">{{newActivitiesMessage}}</div>';
  /* end templates */

  var prefs = new gadgets.Prefs();

  var miniMessagesStreamType = prefs.getString("miniMessagesStreamType");
  var actor = prefs.getString("actor");

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
        var currentActivity = currentMiniMessages[i];
        if (currentActivity.isCurrentUserMiniMessage) {
          currentActivity.cssClass = 'owner';
        }
        htmlContent += buildActivityHtml(templates.miniMessage, currentActivity);
      }
    }
    $('#container').html(htmlContent);

    addDeleteLinksHtml();
    registerDeleteLinksHandler();

    if (hasMoreMiniMessages) {
      addMoreActivitiesBarHtml();
      registerMoreActivityBarHandler();
    } else {
      addNoMoreActivitiesTextHtml();
    }
    gadgets.window.adjustHeight();
  }

  /* HTML building functions */
  function addNewMiniMessageHtml() {
    var htmlContent = Mustache.render(templates.newMiniMessage,
        { placeholderMessage: prefs.getMsg('label.placeholder.new.message'),
          writeLabel: prefs.getMsg('command.write') });

    $(htmlContent).insertBefore('#container');
    gadgets.window.adjustHeight();
  }

  function buildActivityHtml(template, activity) {
    return Mustache.render(template, activity);
  }

  function addDeleteLinksHtml() {
    // activities
    $('div[data-activityid][data-allowdeletion="true"]').each(function() {
      $(this).removeAttr('data-allowdeletion');
      var activityId = $(this).attr('data-activityid');
      var deleteImageURL = NXGadgetContext.clientSideBaseUrl + 'icons/delete.png'

      var actions = $(this).find('div.jsActions');
      var htmlContent = Mustache.render(templates.deleteActivityAction,
          { activityId: activityId, deleteImageURL: deleteImageURL,
            deleteMessage: prefs.getMsg('command.delete') });
      $(htmlContent).insertAfter(actions.find('.timestamp'));
    });
  }

  function addMoreActivitiesBarHtml() {
    var htmlContent = Mustache.render(templates.moreActivitiesBar,
        { moreActivitiesMessage: prefs.getMsg('label.show.more.mini.messages') });
    $('#container').append(htmlContent);
  }

  function addNoMoreActivitiesTextHtml() {
    var htmlContent = Mustache.render(templates.noMoreActivitiesBar,
        { noMoreActivitiesMessage: prefs.getMsg('label.no.more.mini.messages') });
    $('#container').append(htmlContent);
  }

  function addNewActivitiesBarHtml() {
    if ($('.jsNewActivitiesBar').length > 0) {
      return;
    }

    var htmlContent = Mustache.render(templates.newActivitiesBar,
        { newActivitiesMessage: prefs.getMsg('label.show.new.mini.messages') });
    $('#container').prepend(htmlContent);
  }
  /* end HTML building functions */

  /* handler functions */
  function registerNewMiniMessageHandler() {
    $('.jsNewMiniMessage .jsWriteMiniMessageButton').click(function() {
      if ($('.jsNewMiniMessage textarea.jsMiniMessageText').val().length > 0) {
        createMiniMessage();
      }
    });
    updateMiniMessageCounter();
    $('.jsNewMiniMessage textarea.jsMiniMessageText').keyup(function() {
      if ($(this).val().length == 0) {
        $('.jsNewMiniMessage .jsWriteMiniMessageButton').addClass('disabled');
      } else {
        $('.jsNewMiniMessage .jsWriteMiniMessageButton').removeClass('disabled');
      }
      updateMiniMessageCounter();
    });
  }

  function registerDeleteLinksHandler() {
    $('div.jsDelete[data-activityid]').click(function() {
      if (!confirmDeleteMessage()) {
        return false;
      }

      var activityId = $(this).attr("data-activityid");
      removeMiniMessage(activityId);
    });
  }

  function registerMoreActivityBarHandler() {
    $('.jsMoreActivitiesBar').click(function() {
      showMoreMiniMessages();
    });
  }

  function registerNewActivitiesBarHandler() {
    $('.jsNewActivitiesBar').click(function() {
      showNewMiniMessages();
    });
  }
  /* end handler functions */

  /* mini message */
  function updateMiniMessageCounter() {
    var delta = 140 - $('textarea.jsMiniMessageText').val().length;
    var miniMessageCounter = $('.miniMessageCounter');
    miniMessageCounter.text(delta);
    miniMessageCounter.toggleClass('warning', delta < 5);
    if (delta < 0) {
      $('.jsWriteMiniMessageButton').attr('disabled', 'disabled');
    } else {
      $('.jsWriteMiniMessageButton').removeAttr('disabled');
    }
  }

  function createMiniMessage() {
    var miniMessageText = $('textarea.jsMiniMessageText').val();
    var opCallParameters = {
      operationId: 'Services.AddMiniMessage',
      operationParams: {
        language: prefs.getLang(),
        actor: actor,
        message: miniMessageText
      },
      entityType: 'blob',
      operationContext: {},
      operationCallback: function (response, opCallParameters) {
        loadMiniMessages();
        $('.jsNewMiniMessage textarea.jsMiniMessageText').val('');
        $('.jsNewMiniMessage .jsWriteMiniMessageButton').attr('disabled', 'disabled');
        updateMiniMessageCounter();
      }
    };
    doAutomationRequest(opCallParameters);
  }

  function confirmDeleteMessage() {
    return confirm(prefs.getMsg('label.mini.message.confirmDelete'));
  }

  function removeMiniMessage(miniMessageId) {
    var opCallParameters = {
      operationId: 'Services.RemoveMiniMessage',
      operationParams: {
        miniMessageId: miniMessageId
      },
      entityType: 'blob',
      operationContext: {},
      operationCallback: function (response, opCallParameters) {
        loadMiniMessages();
      }
    };
    doAutomationRequest(opCallParameters);
  }
  /* end mini message */

  function showMoreMiniMessages() {
    var NXRequestParams = { operationId: 'Services.GetMiniMessages',
      operationParams: {
        language: prefs.getLang(),
        actor: actor,
        miniMessagesStreamType: miniMessagesStreamType,
        offset: offset,
        asActivities: true
      },
      operationContext: {},
      operationCallback: function (response, params) {
        var newMiniMessages = response.data.miniMessages;
        if (newMiniMessages.length > 0) {
          currentMiniMessages = currentMiniMessages.concat(newMiniMessages);
          offset = response.data.offset;
        } else {
          hasMoreMiniMessages = false;
        }
        displayMiniMessages();
      }
    };

    doAutomationRequest(NXRequestParams);
  }

  function showNewMiniMessages() {
    currentMiniMessages = waitingMiniMessages;
    offset = waitingOffset;
    displayMiniMessages();
  }

  // gadget initialization
  gadgets.util.registerOnLoadHandler(function() {
    var contentStyleClass = prefs.getString("contentStyleClass");
    if (contentStyleClass) {
      _gel('content').className = contentStyleClass;
    }

    if (miniMessagesStreamType == 'forActor') {
      addNewMiniMessageHtml();
      registerNewMiniMessageHandler();
    }

    loadMiniMessages();
    window.setInterval(pollMiniMessages, 30 * 1000);
  });

  function loadMiniMessages() {
    var NXRequestParams = { operationId: 'Services.GetMiniMessages',
      operationParams: {
        language: prefs.getLang(),
        actor: actor,
        miniMessagesStreamType: miniMessagesStreamType,
        asActivities: true
      },
      operationContext: {},
      operationCallback: function (response, params) {
        currentMiniMessages = response.data.miniMessages;
        offset = response.data.offset;
        displayMiniMessages();
      }
    };

    doAutomationRequest(NXRequestParams);
  }

  function pollMiniMessages() {
    var NXRequestParams = { operationId: 'Services.GetMiniMessages',
      operationParams: {
        language: prefs.getLang(),
        actor: actor,
        miniMessagesStreamType: miniMessagesStreamType,
        asActivities: true
      },
      operationContext: {},
      operationCallback: function (response, params) {
        var newMiniMessages = response.data.miniMessages;
        if (newMiniMessages.length > 0 && currentMiniMessages[0].id !== newMiniMessages[0].id) {
          // there is at least one new mini message
          waitingMiniMessages = newMiniMessages;
          waitingOffset = response.data.offset;
          addNewActivitiesBarHtml();
          registerNewActivitiesBarHandler();
          gadgets.window.adjustHeight();
        }
      }
    };

    doAutomationRequest(NXRequestParams);
  }

}());
