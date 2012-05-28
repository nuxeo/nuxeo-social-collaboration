(function() {

  /* constants */
  var constants = {
    likeOperationId: 'Services.Like',
    cancelLikeOperationId: 'Services.CancelLike',

    miniMessageVerb: 'minimessage',

    filter: {
      all: "all",
      discussions: "discussions",
      events: "events"
    }
  };
  /* end constants */

  /* templates */
  var templates = {};
  templates.tabLine =
      '<div class="tabLine">' +
        '<div class="tabLineLinks">' +
          '<a href="#" class="selected" data-filter="{{allFilter}}">{{allMessage}}</a>' +
          '<a href="#" data-filter="{{discussionsFilter}}">{{discussionsMessage}}</a>' +
          '<a href="#" data-filter="{{eventsFilter}}">{{eventsMessage}}</a>' +
        '</div>' +
        '<div class="tabLineButtons">' +
          '<a href="#" title="{{postMessage}}" class="button jsPostMessage">{{postMessage}}</a>' +
        '</div>' +
      '</div>';

  templates.activity =
      '<div class="miniMessage jsMainActivity" data-activityid="{{id}}" data-likescount="{{likeStatus.likesCount}}" data-userlikestatus="{{likeStatus.userLikeStatus}}">' +
        '<div class="container">' +
          '<div class="messageHeader">' +
            '<span class="timestamp">{{publishedDate}}</span>' +
          '</div>' +
          '<div class="message">' +
            '<span class="avatar"><img src="{{actorAvatarURL}}" alt="{{displayActor}}" /></span>' +
            '<div class="event">{{{activityMessage}}}</div>' +
          '</div>' +
          '<div class="actions jsActions"></div>' +
        '</div>' +
        '<div class="answers jsRepliesContainer">{{{repliesHtml}}}</div>' +
      '</div>';

  templates.miniMessage =
      '<div class="miniMessage jsMainActivity" data-activityid="{{id}}" data-likescount="{{likeStatus.likesCount}}" ' +
          'data-userlikestatus="{{likeStatus.userLikeStatus}}" data-allowdeletion="{{allowDeletion}}">' +
        '<div class="container">'+
          '<div class="messageHeader">' +
            '<span class="avatar"><img src="{{actorAvatarURL}}" alt="{{displayActor}}" /></span>' +
            '<span class="username">{{{displayActorLink}}}</span>' +
            '<span class="timestamp">{{{publishedDate}}}</span>' +
          '</div>' +
          '<div class="message">{{{activityMessage}}}</div>' +
          '<div class="actions jsActions"></div>' +
        '</div>' +
        '<div class="answers jsRepliesContainer">{{{repliesHtml}}}</div>' +
      '</div>';

  templates.reply =
      '<div class="miniMessage {{replyClass}}" data-replyid="{{id}}" data-likescount="{{likeStatus.likesCount}}" ' +
          'data-userlikestatus="{{likeStatus.userLikeStatus}}" data-allowdeletion="{{allowDeletion}}">' +
        '<div class="container">' +
          '<div class="message">' +
            '<span class="avatar"><img src="{{actorAvatarURL}}" alt="{{displayActor}}" /></span>' +
            '<div class="event">' +
              '<span class="username">{{{displayActorLink}}}</span>' +
              '<span class="timestamp">{{{publishedDate}}}</span>' +
              '<div class="message">{{{message}}}</div>' +
            '</div>' +
          '</div>' +
          '<div class="actions jsReplyActions"></div>' +
        '</div>' +
      '</div>';

  templates.newMiniMessage =
      '<div class="displayN jsNewMiniMessage">' +
        '<form name="newMiniMessageForm" class="newMiniMessageForm">' +
          '<textarea placeholder="{{placeholderMessage}}" rows="3" name="newMiniMessageText" class="miniMessageText jsMiniMessageText"></textarea>' +
          '<p class="newMiniMessageActions">' +
            '<span class="miniMessageCounter jsMiniMessageCounter"></span>' +
            '<input class="button writeMiniMessageButton disabled jsWriteMiniMessageButton" name="writeMiniMessageButton" type="button" value="{{writeLabel}}" disabled="disabled" />' +
          '</p>' +
        '</form>' +
      '</div>';

  templates.newActivityReply =
      '<div class="displayN jsNewActivityReply messageBlock" data-activityid="{{activityId}}">' +
        '<form>' +
          '<textarea placeholder="{{placeholderMessage}}" rows="1" class="jsActivityReplyText"></textarea>' +
          '<p class="newMiniMessageActions">' +
            '<span class="miniMessageCounter jsActivityReplyCounter"></span>' +
            '<input class="button disabled jsWriteActivityReplyButton" name="writeActivityReplyButton" type="button" value="{{writeLabel}}" />' +
          '</p>' +
        '</form>' +
      '</div>';

  templates.deleteActivityAction =
      '<div class="actionItem jsDelete" data-activityid="{{activityId}}">' +
        '<img src="{{deleteImageURL}}" />' +
        '<a href="#">{{deleteMessage}}</a>' +
      '</div>';

  templates.deleteActivityReplyAction =
      '<div class="actionItem jsDelete" data-replyid="{{replyId}}">' +
        '<img src="{{deleteImageURL}}" />' +
        '<a href="#">{{deleteMessage}}</a>' +
      '</div>';

  templates.replyAction =
      '<div class="actionItem jsReply" data-activityid="{{activityId}}">' +
        '<img src="{{replyImageURL}}" />' +
        '<a href="#">{{replyMessage}}</a>' +
      '</div>';

  templates.likeAction =
      '<div class="actionItem jsLike">' +
        '<img class="likeIcon jsLikeIcon" src="{{likeImageURL}}" />' +
        '<span class="likesCount">{{likesCount}}</span>' +
      '</div>';

    templates.moreActivitiesBar =
      '<div class="moreActivitiesBar jsMoreActivitiesBar">{{moreActivitiesMessage}}</div>';

  templates.noMoreActivitiesBar =
      '<div class="moreActivitiesBar noMore">{{noMoreActivitiesMessage}}</div>';

  templates.moreRepliesBar =
      '<div class="moreActivitiesBar jsMoreRepliesBar">{{moreRepliesMessage}}</div>';

  templates.newActivitiesBar =
      '<div class="newActivitiesBar jsNewActivitiesBar">{{newActivitiesMessage}}</div>';
  /* end templates */

  var prefs = new gadgets.Prefs();

  var activityStreamName = prefs.getString("activityStreamName");
  var documentContextPath = gadgets.util.unescapeString(prefs.getString("nuxeoTargetContextPath"));

  var wallOperationParams = {
    language: prefs.getLang(),
    contextPath: documentContextPath,
    activityStreamName: activityStreamName
  };
  var  miniMessageOperationParams = {
    language: prefs.getLang(),
    contextPath: documentContextPath
  };

  var currentActivities = [];
  var waitingActivities = [];

  var offset = 0;
  var waitingOffset = 0;

  var hasMoreActivities = true;

  var filter = constants.filter.all;

  function loadWallActivityStream() {
    var NXRequestParams = {
      operationId: 'Services.GetWallActivityStream',
      operationParams: wallOperationParams,
      operationContext: {},
      operationCallback: function(response, params) {
        currentActivities = response.data.activities;
        offset = response.data.offset;
        displayWallActivities();
      }
    };

    doAutomationRequest(NXRequestParams);
  }

  function pollWallActivityStream() {
    var NXRequestParams= { operationId : 'Services.GetWallActivityStream',
      operationParams: wallOperationParams,
      operationContext: {},
      operationCallback: function(response, params) {
        var newActivities = response.data.activities;
        if (newActivities.length > 0 && currentActivities[0].id !== newActivities[0].id) {
          // there is at least one new activity
          waitingActivities = newActivities;
          waitingOffset = response.data.offset;
          addNewActivitiesBarHtml();
          registerNewActivitiesBarHandler();
          gadgets.window.adjustHeight();
        }
      }
    };

    doAutomationRequest(NXRequestParams);
  }

  function displayWallActivities() {
    var htmlContent = '';

    if (currentActivities.length == 0) {
      htmlContent += '<div class="noStream">' + prefs.getMsg('label.no.activity') + '</div>';
    } else {
      for (var i = 0; i < currentActivities.length; i++) {
        var currentActivity = currentActivities[i];
        if (currentActivity.activityVerb == constants.miniMessageVerb && filter !== constants.filter.events) {
          htmlContent += buildActivityHtml(templates.miniMessage, currentActivity);
        } else if (currentActivity.activityVerb !== constants.miniMessageVerb && filter !== constants.filter.discussions) {
          htmlContent += buildActivityHtml(templates.activity, currentActivity);
        }
      }
    }
    $('#wall').html(htmlContent);

    addLikeStatusHtml();
    addDeleteLinksHtml();
    addReplyLinksHtml();

    registerLikeStatusHandler();
    registerDeleteLinksHandler();
    registerReplyLinksHandler();
    registerNewActivityReplyHandler();
    registerMoreRepliesHandler();

    if (hasMoreActivities) {
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

    $(htmlContent).insertBefore('#wall');
    gadgets.window.adjustHeight();
  }

  function buildActivityHtml(template, activity) {
    var repliesHtml = '';
    if (activity.replies.length > 0) {
      if (activity.replies.length > 3) {
        var moreRepliesMessage = prefs.getMsg('label.view.all') + ' ' +
          activity.replies.length + ' ' + prefs.getMsg('label.activity.replies');
          repliesHtml += Mustache.render(templates.moreRepliesBar, {
          moreRepliesMessage: moreRepliesMessage
        });
      }
      for (var i = 0; i < activity.replies.length; i++) {
        var reply = activity.replies[i];
        reply.replyClass = i < activity.replies.length - 3 ? 'displayN' : '';
        repliesHtml += Mustache.render(templates.reply, reply);
      }
    }

    repliesHtml += Mustache.render(templates.newActivityReply, {
      activityId: activity.id,
      placeholderMessage: prefs.getMsg('label.placeholder.new.activity.reply'),
      writeLabel: prefs.getMsg('command.reply') });

    activity.repliesHtml = repliesHtml;
    return Mustache.render(template, activity);
  }

  function addTabLineHtml() {
    var htmlContent = Mustache.render(templates.tabLine,
        { allFilter: constants.filter.all,
          allMessage: prefs.getMsg('label.activities.filter.all'),
          discussionsFilter: constants.filter.discussions,
          discussionsMessage: prefs.getMsg('label.activities.filter.messages'),
          eventsFilter: constants.filter.events,
          eventsMessage: prefs.getMsg('label.activities.filter.events'),
          postMessage: prefs.getMsg('label.write.message')
        });
    $(htmlContent).insertBefore('#wall');
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
      actions.prepend(htmlContent);
    });

    // activity replies
    $('div[data-replyid][data-allowdeletion="true"]').each(function() {
      $(this).removeAttr('data-allowdeletion');
      var replyId = $(this).attr('data-replyid');
      var deleteImageURL = NXGadgetContext.clientSideBaseUrl + 'icons/delete.png'

      var actions = $(this).find('div.jsReplyActions');
      var htmlContent = Mustache.render(templates.deleteActivityReplyAction,
          { replyId: replyId, deleteImageURL: deleteImageURL,
            deleteMessage: prefs.getMsg('command.delete') });
      actions.prepend(htmlContent);
    });
  }

  function addLikeStatusHtml() {
    $('div[data-activityid][data-likescount]').each(function() {
      var activityId = $(this).attr('data-activityid');
      var likesCount = $(this).attr('data-likescount');
      var userLikeStatus = $(this).attr('data-userlikestatus');
      var actions = $(this).find('div.jsActions');
      addActivityLikeStatusHtml(actions, activityId, likesCount, userLikeStatus);
    });

    $('div[data-replyid][data-likescount]').each(function() {
      var replyId = $(this).attr('data-replyid');
      var likesCount = $(this).attr('data-likescount');
      var userLikeStatus = $(this).attr('data-userlikestatus');
      var actions = $(this).find('div.jsReplyActions');
      addActivityLikeStatusHtml(actions, replyId, likesCount, userLikeStatus);
    });
  }

  function addActivityLikeStatusHtml(actions, activityId, likesCount, userLikeStatus) {
    actions.find('.jsLike').remove();

    var likeImageURL = userLikeStatus == 1
        ? NXGadgetContext.clientSideBaseUrl + 'icons/like_active.png'
        : NXGadgetContext.clientSideBaseUrl + 'icons/like_unactive.png';

    var htmlContent = Mustache.render(templates.likeAction,
        { likeImageURL: likeImageURL, likesCount: likesCount });

    var deleteAction = $(actions).find('.jsDelete');
    if (deleteAction.length > 0) {
      $(htmlContent).insertAfter(deleteAction);
    } else {
      actions.prepend(htmlContent);
    }
  }

  function addReplyLinksHtml() {
    $('div[data-activityid]').each(function() {
      var activityId = $(this).attr('data-activityid');
      var replyImageURL = NXGadgetContext.clientSideBaseUrl + 'icons/reply.png'

      var actions = $(this).find('div.jsActions');
      var htmlContent = Mustache.render(templates.replyAction,
          { activityId: activityId, replyImageURL: replyImageURL,
            replyMessage: prefs.getMsg('command.reply') });
      actions.append(htmlContent);
    });
  }

  function addMoreActivitiesBarHtml() {
    var htmlContent = Mustache.render(templates.moreActivitiesBar,
        { moreActivitiesMessage: prefs.getMsg('label.show.more.activities') });
    $('#wall').append(htmlContent);
  }

  function addNoMoreActivitiesTextHtml() {
    var htmlContent = Mustache.render(templates.noMoreActivitiesBar,
        { noMoreActivitiesMessage: prefs.getMsg('label.no.more.activities') });
    $('#wall').append(htmlContent);
  }

  function addNewActivitiesBarHtml() {
    if ($('.jsNewActivitiesBar').length > 0) {
      return;
    }

    var htmlContent = Mustache.render(templates.newActivitiesBar,
        { newActivitiesMessage: prefs.getMsg('label.show.new.activities') });
    $('#wall').prepend(htmlContent);
  }
  /* end HTML building functions */

  /* handler functions */
  function registerTabLineHandler() {
    $('a[data-filter]').click(function() {
      if (!$(this).is('.selected')) {
        $('[data-filter]').removeClass('selected');
        $(this).addClass('selected');

        filter = $(this).attr('data-filter');
        displayWallActivities();
      }
    });
  }

  function registerNewMiniMessageHandler() {
    $('.jsPostMessage').click(function() {
      $('.jsNewMiniMessage').fadeIn(300, function() {
        gadgets.window.adjustHeight();
      });
      $('.jsNewMiniMessage textarea.jsMiniMessageText').focus();
    });

    // hide the form when clicking outside
    $('body').click(function(e) {
      if ($(e.target).hasClass('jsPostMessage') || $(e.target).hasClass('jsNewMiniMessage')) {
        return;
      }
      if ($(e.target).parents('.jsNewMiniMessage').length > 0) {
        return;
      }
      if ($('.jsNewMiniMessage textarea.jsMiniMessageText').val().length == 0) {
        $('.jsNewMiniMessage').hide();
      }
    });

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

  function registerLikeStatusHandler() {
    // activities
    $('div.jsMainActivity[data-activityid]').each(function() {
      var activityId = $(this).attr('data-activityid');
      var likeIcon = $(this).find('.jsActions .jsLikeIcon');
      registerLikeStatusHandlerFor(activityId, likeIcon);
    });
    // replies
    $('div[data-replyid]').each(function() {
      var replyId = $(this).attr('data-replyid');
      var likeIcon = $(this).find('.jsReplyActions .jsLikeIcon');
      registerLikeStatusHandlerFor(replyId, likeIcon);
    });
  }

  function registerLikeStatusHandlerFor(activityId, likeIcon) {
    likeIcon.click(function() {
      var userLikeStatus = likeIcon.parents('div[data-userlikestatus]')
        .attr('data-userlikestatus');
      var operationId = userLikeStatus == 1
        ? constants.cancelLikeOperationId
        : constants.likeOperationId;

      var NXRequestParams= { operationId : operationId,
        operationParams: {
          activityId: activityId
        },
        operationContext: {},
        operationCallback: function(response, params) {
          var likeStatus = response.data;

          if ($('div.jsMainActivity[data-activityid="' + activityId + '"]').length > 0) {
            $('div.jsMainActivity[data-activityid="' + activityId + '"]').each(function() {
              $(this).attr('data-likescount', likeStatus.likesCount);
              $(this).attr('data-userlikestatus', likeStatus.userLikeStatus);
              var actions = $(this).find('div.jsActions');
              addActivityLikeStatusHtml(actions, activityId,
                likeStatus.likesCount, likeStatus.userLikeStatus);
              registerLikeStatusHandlerFor(activityId, actions.find('.jsLikeIcon'));
            });
          } else {
            // reply
            $('div[data-replyid="' + activityId + '"]').each(function() {
              $(this).attr('data-likescount', likeStatus.likesCount);
              $(this).attr('data-userlikestatus', likeStatus.userLikeStatus);
              var actions = $(this).find('div.jsReplyActions');
              addActivityLikeStatusHtml(actions, activityId,
                likeStatus.likesCount, likeStatus.userLikeStatus);
              registerLikeStatusHandlerFor(activityId, actions.find('.jsLikeIcon'));
            });
          }
        }
      };
      doAutomationRequest(NXRequestParams);
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

    $('div.jsDelete[data-replyid]').each(function() {
      handleDeleteActivityReply($(this));
    });
  }

  function handleDeleteActivityReply(deleteLink) {
    deleteLink.click(function() {
      if (!confirmDeleteReply()) {
        return false;
      }

      var replyId = $(deleteLink).attr('data-replyid');
      var activityId = $(deleteLink).parents('div[data-activityid]').attr('data-activityid');
      removeActivityReply(activityId, replyId);
    });
  }

  function registerReplyLinksHandler() {
    $('.jsReply').click(function() {
      var activityId = $(this).attr('data-activityid');
      var newActivityReply = $('.jsNewActivityReply[data-activityid="' + activityId + '"]');
      updateActivityReplyMessageCounter(newActivityReply);
      newActivityReply.show();
      newActivityReply.find('textarea.jsActivityReplyText').focus();
      gadgets.window.adjustHeight();
    });
  }

  function registerNewActivityReplyHandler() {
    $('.jsActivityReplyText').keyup(function () {
      var writeButton = $(this).siblings('.newMiniMessageActions').find('.jsWriteActivityReplyButton');
      if ($(this).val().length == 0) {
        writeButton.addClass('disabled');
      } else {
        writeButton.removeClass('disabled');
      }

      var newActivityReply = $(this).parents('.jsNewActivityReply');
      updateActivityReplyMessageCounter(newActivityReply);
    });

    $('.jsNewActivityReply').each(function() {
      var newActivityReply = $(this);
      var activityId = $(this).attr('data-activityid');
      var writeButton = $(this).find('.jsWriteActivityReplyButton');
      writeButton.attr('data-activityid', activityId);
      writeButton.click(function() {
        if (newActivityReply.find('textarea.jsActivityReplyText').val().length > 0) {
          createActivityReply(newActivityReply);
        }
      });
    });
  }

  function registerMoreRepliesHandler() {
    $('.jsMoreRepliesBar').click(function() {
      var repliesContainer = $(this).parents('.jsRepliesContainer');
      repliesContainer.find('div[data-replyid]').each(function() {
        $(this).show();
      });
      repliesContainer.find('.jsMoreRepliesBar').each(function() {
        $(this).remove();
      });
      gadgets.window.adjustHeight();
    });
  }

  function registerMoreActivityBarHandler() {
    $('.jsMoreActivitiesBar').click(function() {
      showMoreActivities();
    });
  }

  function registerNewActivitiesBarHandler() {
    $('.jsNewActivitiesBar').click(function() {
      showNewActivities();
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
    var newOperationParams = jQuery.extend(true, {}, miniMessageOperationParams);
    newOperationParams.message = miniMessageText;
    var opCallParameters = {
      operationId: 'Services.AddMiniMessage',
      operationParams: newOperationParams,
      entityType: 'blob',
      operationContext: {},
      operationCallback: function (response, opCallParameters) {
        loadWallActivityStream();
        $('.jsNewMiniMessage').hide();
        $('.jsNewMiniMessage textarea.jsMiniMessageText').val('');
        $('.jsNewMiniMessage .jsWriteMiniMessageButton').attr('disabled', 'disabled');
        updateMiniMessageCounter();
      }
    };
    doAutomationRequest(opCallParameters);
  }

  function confirmDeleteMessage() {
    return confirm(prefs.getMsg('label.wall.message.confirmDelete'));
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
        loadWallActivityStream();
      }
    };
    doAutomationRequest(opCallParameters);
  }
  /* end mini message */

  /* activity replies */
  function updateActivityReplyMessageCounter(newActivityReply) {
    var delta = 140 - newActivityReply.find('textarea.jsActivityReplyText').val().length;
    var miniMessageCounter = newActivityReply.find('.jsActivityReplyCounter');
    miniMessageCounter.text(delta);
    miniMessageCounter.toggleClass('warning', delta < 5);
    if (delta < 0) {
      newActivityReply.find('.jsWriteActivityReplyButton').attr('disabled', 'disabled');
    } else {
      newActivityReply.find('.jsWriteActivityReplyButton').removeAttr('disabled');
    }
  }

  function createActivityReply(newActivityReply) {
    var activityReplyText = newActivityReply.find('textarea.jsActivityReplyText').val();
    var activityId = newActivityReply.attr('data-activityid');
    var opCallParameters = {
      operationId: 'Services.AddActivityReply',
      operationParams: {
        message: activityReplyText,
        language: prefs.getLang(),
        activityId: activityId
      },
      entityType: 'blob',
      operationContext: {},
      operationCallback: function (response, opCallParameters) {
        newActivityReply.find('.jsActivityReplyText').val('');
        updateActivityReplyMessageCounter(newActivityReply);
        newActivityReply.hide();

        var repliesContainer = $('div[data-activityId="' + activityId + '"]')
          .find('.jsRepliesContainer');
        var reply = response.data;
        reply.likeStatus = {};
        reply.likeStatus.likesCount = 0;
        reply.likeStatus.userLikeStatus = 0;
        var replyHtml = Mustache.render(templates.reply, reply);
        $(replyHtml).insertBefore(repliesContainer.find('.jsNewActivityReply'));

        repliesContainer.find('div[data-replyid="' + reply.id + '"]').each(function() {
          // like status
          var replyId = $(this).attr('data-replyid');
          var likesCount = $(this).attr('data-likescount');
          var userLikeStatus = $(this).attr('data-userlikestatus');
          var replyActions = $(this).find('div.jsReplyActions');
          addActivityLikeStatusHtml(replyActions, replyId, likesCount, userLikeStatus);
          registerLikeStatusHandlerFor(replyId, replyActions.find('.jsLikeIcon'));

          // delete link
          var allowDeletion = $(this).attr('data-allowdeletion');
          if (allowDeletion) {
            $(this).removeAttr('data-allowdeletion');
            var deleteImageURL = NXGadgetContext.clientSideBaseUrl + 'icons/delete.png'
            var htmlContent = Mustache.render(templates.deleteActivityReplyAction,
                { replyId: replyId, deleteImageURL: deleteImageURL,
                  deleteMessage: prefs.getMsg('command.delete') });
            replyActions.prepend(htmlContent);

            replyActions.find('div.jsDelete[data-replyid="' + replyId + '"]').each(function() {
              handleDeleteActivityReply($(this));
            });
          }
        });
        gadgets.window.adjustHeight();
      }
    };
    doAutomationRequest(opCallParameters);
  }

  function confirmDeleteReply() {
    return confirm(prefs.getMsg('label.wall.reply.confirmDelete'));
  }

  function removeActivityReply(activityId, replyId) {
    var opCallParameters = {
      operationId: 'Services.RemoveActivityReply',
      operationParams: {
        activityId: activityId,
        replyId: replyId
      },
      entityType: 'blob',
      operationContext: {},
      operationCallback: function (response, opCallParameters) {
        if (response.rc > 200 && response.rc < 300) {
          $('div[data-replyid="'+ replyId + '"]').remove();
          gadgets.window.adjustHeight();
        }
      }
    };
    doAutomationRequest(opCallParameters);
  }
  /* end activity replies */

  function showMoreActivities() {
    var newOperationParams = jQuery.extend(true, {}, wallOperationParams);
    newOperationParams.offset = offset;
    var NXRequestParams= { operationId : 'Services.GetWallActivityStream',
      operationParams: newOperationParams,
      operationContext: {},
      operationCallback: function(response, params) {
        var newActivities = response.data.activities;
        if (newActivities.length > 0) {
          currentActivities = currentActivities.concat(newActivities);
          offset = response.data.offset;
        } else {
          hasMoreActivities = false;
        }
        displayWallActivities();
      }
    };

    doAutomationRequest(NXRequestParams);
  }

  function showNewActivities() {
    currentActivities = waitingActivities;
    offset = waitingOffset;
    displayWallActivities();
  }

  // gadget initialization
  gadgets.util.registerOnLoadHandler(function() {
    var contentStyleClass = prefs.getString("contentStyleClass");
    if (contentStyleClass) {
      _gel('content').className = contentStyleClass;
    }

    addTabLineHtml();
    addNewMiniMessageHtml();

    registerTabLineHandler();
    registerNewMiniMessageHandler();

    loadWallActivityStream();
    window.setInterval(pollWallActivityStream, 30*1000);
  });

}());
