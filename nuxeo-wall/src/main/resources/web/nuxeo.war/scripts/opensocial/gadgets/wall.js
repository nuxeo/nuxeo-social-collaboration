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
        '<div class="answers jsCommentsContainer">{{{commentsHtml}}}</div>' +
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
        '<div class="answers jsCommentsContainer">{{{commentsHtml}}}</div>' +
      '</div>';

  templates.comment =
      '<div class="miniMessage {{commentClass}}" data-commentid="{{id}}" data-likescount="{{likeStatus.likesCount}}" ' +
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
          '<div class="actions jsCommentActions"></div>' +
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

  templates.newActivityComment =
      '<div class="displayN jsNewActivityComment messageBlock" data-activityid="{{activityId}}">' +
        '<form>' +
          '<textarea placeholder="{{placeholderMessage}}" rows="1" class="jsActivityCommentText"></textarea>' +
          '<p class="newMiniMessageActions">' +
            '<span class="miniMessageCounter jsActivityCommentCounter"></span>' +
            '<input class="button disabled jsWriteActivityCommentButton" name="writeActivityCommentButton" type="button" value="{{writeLabel}}" />' +
          '</p>' +
        '</form>' +
      '</div>';

  templates.deleteActivityAction =
      '<div class="actionItem">' +
        '<img src="{{deleteImageURL}}" />' +
        '<a href="#" class="jsDelete" data-activityid="{{activityId}}">{{deleteMessage}}</a>' +
      '</div>';

  templates.deleteActivityCommentAction =
      '<div class="actionItem">' +
        '<img src="{{deleteImageURL}}" />' +
        '<a href="#" class="jsDelete" data-commentid="{{commentId}}" >{{deleteMessage}}</a>' +
      '</div>';

  templates.replyAction =
      '<div class="actionItem">' +
        '<img src="{{replyImageURL}}" />' +
        '<a href="#" class="jsReply" data-activityid="{{activityId}}" >{{replyMessage}}</a>' +
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

  templates.moreCommentsBar =
      '<div class="moreActivitiesBar jsMoreCommentsBar">{{moreCommentsMessage}}</div>';

  templates.newActivitiesBar =
      '<div class="newActivitiesBar jsNewActivitiesBar">{{newActivitiesMessage}}</div>';
  /* end templates */

  var prefs = new gadgets.Prefs();

  var docId = prefs.getString("docId");
  var activityStreamName = prefs.getString("activityStreamName");

  var currentActivities = [];
  var waitingActivities = [];

  var offset = 0;
  var waitingOffset = 0;

  var hasMoreActivities = true;

  var filter = constants.filter.all;

  function loadWallActivityStream() {
    var NXRequestParams = {
      operationId: 'Services.GetWallActivityStream',
      operationParams: {
        language: prefs.getLang(),
        document: docId,
        activityStreamName: activityStreamName
      },
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
      operationParams: {
        language: prefs.getLang(),
        document: docId,
        activityStreamName: activityStreamName
      },
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
    registerNewActivityCommentHandler();
    registerMoreCommentsHandler();

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
    var commentsHtml = '';
    if (activity.comments.length > 0) {
      if (activity.comments.length > 3) {
        var moreCommentsMessage = prefs.getMsg('label.view.all') + ' ' +
          activity.comments.length + ' ' + prefs.getMsg('label.activity.comments');
          commentsHtml += Mustache.render(templates.moreCommentsBar, {
          moreCommentsMessage: moreCommentsMessage
        });
      }
      for (var i = 0; i < activity.comments.length; i++) {
        var comment = activity.comments[i];
        comment.commentClass = i < activity.comments.length - 3 ? 'displayN' : '';
        commentsHtml += Mustache.render(templates.comment, comment);
      }
    }

    commentsHtml += Mustache.render(templates.newActivityComment, {
      activityId: activity.id,
      placeholderMessage: prefs.getMsg('label.placeholder.new.activity.comment'),
      writeLabel: prefs.getMsg('command.reply') });

    activity.commentsHtml = commentsHtml;
    return Mustache.render(template, activity);
  }

  function addTabLineHtml() {
    var htmlContent = Mustache.render(templates.tabLine,
        { allFilter: constants.filter.all,
          allMessage: prefs.getMsg('label.activities.filter.all'),
          discussionsFilter: constants.filter.discussions,
          discussionsMessage: prefs.getMsg('label.activities.filter.discussions'),
          eventsFilter: constants.filter.events,
          eventsMessage: prefs.getMsg('label.activities.filter.events'),
          postMessage: prefs.getMsg('label.post.message')
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

    // activity comments
    $('div[data-commentid][data-allowdeletion="true"]').each(function() {
      $(this).removeAttr('data-allowdeletion');
      var commentId = $(this).attr('data-commentid');
      var deleteImageURL = NXGadgetContext.clientSideBaseUrl + 'icons/delete.png'

      var actions = $(this).find('div.jsCommentActions');
      var htmlContent = Mustache.render(templates.deleteActivityCommentAction,
          { commentId: commentId, deleteImageURL: deleteImageURL,
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
      addActivityLikeStatusHtml($(this), actions, activityId, likesCount, userLikeStatus);
    });

    $('div[data-commentid][data-likescount]').each(function() {
      var commentId = $(this).attr('data-commentid');
      var likesCount = $(this).attr('data-likescount');
      var userLikeStatus = $(this).attr('data-userlikestatus');
      var actions = $(this).find('div.jsCommentActions');
      addActivityLikeStatusHtml($(this), actions, commentId, likesCount, userLikeStatus);
    });
  }

  function addActivityLikeStatusHtml(activityOrComment, actions, activityId, likesCount, userLikeStatus) {
    activityOrComment.find('.jsLike').remove();

    var likeImageURL = userLikeStatus == 1
        ? NXGadgetContext.clientSideBaseUrl + 'icons/vote_up_active.png'
        : NXGadgetContext.clientSideBaseUrl + 'icons/vote_up_unactive.png';

    var htmlContent = Mustache.render(templates.likeAction,
        { likeImageURL: likeImageURL, likesCount: likesCount });

    var deleteLink = $(activityOrComment).find('a.jsDelete');
    if (deleteLink.length > 0) {
      $(htmlContent).insertAfter(deleteLink);
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
      var likeIcon = $(this).find('.jsLikeIcon');
      registerLikeStatusHandlerFor(activityId, likeIcon);
    });
    // comments
    $('div[data-commentid]').each(function() {
      var commentId = $(this).attr('data-commentid');
      var likeIcon = $(this).find('.jsLikeIcon');
      registerLikeStatusHandlerFor(commentId, likeIcon);
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
              addActivityLikeStatusHtml($(this), actions, activityId,
                likeStatus.likesCount, likeStatus.userLikeStatus);
              registerLikeStatusHandlerFor(activityId, $(this).find('.jsLikeIcon'));
            });
          } else {
            // comment
            $('div[data-commentid="' + activityId + '"]').each(function() {
              $(this).attr('data-likescount', likeStatus.likesCount);
              $(this).attr('data-userlikestatus', likeStatus.userLikeStatus);
              var actions = $(this).find('div.jsCommentActions');
              addActivityLikeStatusHtml($(this), actions, activityId,
                likeStatus.likesCount, likeStatus.userLikeStatus);
              registerLikeStatusHandlerFor(activityId, $(this).find('.jsLikeIcon'));
            });
          }
        }
      };
      doAutomationRequest(NXRequestParams);
    });
  }

  function registerDeleteLinksHandler() {
    $('a.jsDelete[data-activityid]').click(function() {
      if (!confirmDeleteMiniMessage()) {
        return false;
      }

      var activityId = $(this).attr("data-activityid");
      removeMiniMessage(activityId);
    });

    $('a.jsDelete[data-commentid]').each(function() {
      handleDeleteActivityComment($(this));
    });
  }

  function handleDeleteActivityComment(deleteLink) {
    deleteLink.click(function() {
      if (!confirmDeleteMiniMessage()) {
        return false;
      }

      var commentId = $(deleteLink).attr('data-commentid');
      var activityId = $(deleteLink).parents('div[data-activityid]').attr('data-activityid');
      removeActivityComment(activityId, commentId);
    });
  }

  function registerReplyLinksHandler() {
    $('.jsReply').click(function() {
      var activityId = $(this).attr('data-activityid');
      var newActivityComment = $('.jsNewActivityComment[data-activityid="' + activityId + '"]');
      updateActivityCommentMessageCounter(newActivityComment);
      newActivityComment.show();
      newActivityComment.find('textarea.jsActivityCommentText').focus();
      gadgets.window.adjustHeight();
    });
  }

  function registerNewActivityCommentHandler() {
    $('.jsActivityCommentText').keyup(function () {
      var writeButton = $(this).siblings('.newMiniMessageActions').find('.jsWriteActivityCommentButton');
      if ($(this).val().length == 0) {
        writeButton.addClass('disabled');
      } else {
        writeButton.removeClass('disabled');
      }

      var newActivityComment = $(this).parents('.jsNewActivityComment');
      updateActivityCommentMessageCounter(newActivityComment);
    });

    $('.jsNewActivityComment').each(function() {
      var newActivityComment = $(this);
      var activityId = $(this).attr('data-activityid');
      var writeButton = $(this).find('.jsWriteActivityCommentButton');
      writeButton.attr('data-activityid', activityId);
      writeButton.click(function() {
        if ($(this).siblings('.jsActivityCommentText').val().length > 0) {
          createActivityComment(newActivityComment);
        }
      });
    });
  }

  function registerMoreCommentsHandler() {
    $('.jsMoreCommentsBar').click(function() {
      var commentsContainer = $(this).parents('.jsCommentsContainer');
      commentsContainer.find('div[data-commentid]').each(function() {
        $(this).show();
      });
      commentsContainer.find('.jsMoreCommentsBar').each(function() {
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
    var opCallParameters = {
      operationId: 'Services.AddMiniMessage',
      operationParams: {
        message: miniMessageText,
        language: prefs.getLang(),
        document: docId
      },
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

  function confirmDeleteMiniMessage() {
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
        loadWallActivityStream();
      }
    };
    doAutomationRequest(opCallParameters);
  }
  /* end mini message */

  /* activity comments */
  function updateActivityCommentMessageCounter(newActivityComment) {
    var delta = 140 - newActivityComment.find('textarea.jsActivityCommentText').val().length;
    var miniMessageCounter = newActivityComment.find('.jsActivityCommentCounter');
    miniMessageCounter.text(delta);
    miniMessageCounter.toggleClass('warning', delta < 5);
    if (delta < 0) {
      newActivityComment.find('.jsWriteActivityCommentButton').attr('disabled', 'disabled');
    } else {
      newActivityComment.find('.jsWriteActivityCommentButton').removeAttr('disabled');
    }
  }

  function createActivityComment(newActivityComment) {
    var activityCommentText = newActivityComment.find('textarea.jsActivityCommentText').val();
    var activityId = newActivityComment.attr('data-activityid');
    var opCallParameters = {
      operationId: 'Services.AddActivityComment',
      operationParams: {
        message: activityCommentText,
        language: prefs.getLang(),
        activityId: activityId
      },
      entityType: 'blob',
      operationContext: {},
      operationCallback: function (response, opCallParameters) {
        newActivityComment.find('.jsActivityCommentText').val('');
        updateActivityCommentMessageCounter(newActivityComment);
        newActivityComment.hide();

        var commentsContainer = $('div[data-activityId="' + activityId + '"]')
          .find('.jsCommentsContainer');
        var comment = response.data;
        comment.likeStatus = {};
        comment.likeStatus.likesCount = 0;
        comment.likeStatus.userLikeStatus = 0;
        var commentHtml = Mustache.render(templates.comment, comment);
        $(commentHtml).insertBefore(commentsContainer.find('.jsNewActivityComment'));

        commentsContainer.find('div[data-commentid="' + comment.id + '"]').each(function() {
          // like status
          var commentId = $(this).attr('data-commentid');
          var likesCount = $(this).attr('data-likescount');
          var userLikeStatus = $(this).attr('data-userlikestatus');
          var commentActions = $(this).find('div.jsCommentActions');
          addActivityLikeStatusHtml($(this), commentActions, commentId, likesCount, userLikeStatus);

          // delete link
          var allowDeletion = $(this).attr('data-allowdeletion');
          if (allowDeletion) {
            $(this).removeAttr('data-allowdeletion');
            var htmlContent = Mustache.render(templates.deleteActivityCommentAction,
                { commentId: commentId, deleteMessage: prefs.getMsg('command.delete') });
            commentActions.prepend(htmlContent);

            commentActions.find('a[data-commentid="' + commentId + '"]').each(function() {
              handleDeleteActivityComment($(this));
            });
          }
        });
        gadgets.window.adjustHeight();
      }
    };
    doAutomationRequest(opCallParameters);
  }

  function removeActivityComment(activityId, commentId) {
    var opCallParameters = {
      operationId: 'Services.RemoveActivityComment',
      operationParams: {
        activityId: activityId,
        commentId: commentId
      },
      entityType: 'blob',
      operationContext: {},
      operationCallback: function (response, opCallParameters) {
        if (response.rc > 200 && response.rc < 300) {
          $('div[data-commentid="'+ commentId + '"]').remove();
          gadgets.window.adjustHeight();
        }
      }
    };
    doAutomationRequest(opCallParameters);
  }
  /* end activity comments */

  function showMoreActivities() {
    var NXRequestParams= { operationId : 'Services.GetWallActivityStream',
      operationParams: {
        language: prefs.getLang(),
        document: docId,
        activityStreamName: activityStreamName,
        offset: offset
      },
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
