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
  templates.activity =
      '<div class="miniMessage" data-activityid="{{id}}" data-likescount="{{likeStatus.likesCount}}" data-userlikestatus="{{likeStatus.userLikeStatus}}">' +
        '<div class="container">' +
          '<div class="messageHeader">' +
            '<span class="timestamp">{{publishedDate}}</span>' +
            '<div class="actions jsActions"></div>' +
          '</div>' +
          '<div class="message">' +
            '<span class="avatar"><img src="{{actorAvatarURL}}" alt="{{displayActor}}" /></span>' +
            '<div class="event">{{{activityMessage}}}</div>' +
          '</div>' +
        '</div>' +
        '<div class="answers jsCommentsContainer">{{{commentsHtml}}}</div>' +
      '</div>';

  templates.miniMessage =
      '<div class="miniMessage" data-activityid="{{id}}" data-likescount="{{likeStatus.likesCount}}" ' +
          'data-userlikestatus="{{likeStatus.userLikeStatus}}" data-allowdeletion="{{allowDeletion}}">' +
        '<div class="container">'+
          '<div class="messageHeader">' +
            '<span class="avatar"><img src="{{actorAvatarURL}}" alt="{{displayActor}}" /></span>' +
            '<span class="username">{{{displayActorLink}}}</span>' +
            '<span class="timestamp">{{{publishedDate}}}</span>' +
            '<div class="actions jsActions"></div>' +
          '</div>' +
          '<div class="message">{{{activityMessage}}}</div>' +
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

  templates.moreActivitiesBar =
      '<div class="moreActivitiesBar jsMoreActivitiesBar">{{moreActivitiesMessage}}</div>';

  templates.noMoreActivitiesBar =
      '<div class="moreActivitiesBar noMore">{{noMoreActivitiesMessage}}</div>';

  templates.moreCommentsBar =
      '<div class="moreActivitiesBar jsMoreCommentsBar">{{moreCommentsMessage}}</div>';

  templates.newActivitiesBar =
      '<div class="newActivitiesBar jsNewActivitiesBar">{{newActivitiesMessage}}</div>';

  templates.newMiniMessageForm =
      '<div style="display: none;">' +
        '<div id="newMiniMessage">' +
          '<form name="newMiniMessageForm" class="newMiniMessageForm">' +
            '<textarea rows="3" name="newMiniMessageText" class="miniMessageText jsMiniMessageText"></textarea>' +
            '<p class="newMiniMessageActions">' +
              '<span class="miniMessageCounter jsMiniMessageCounter"></span>' +
              '<input class="button writeMiniMessageButton jsWriteMiniMessageButton" name="writeMiniMessageButton" type="button" value="{{writeLabel}}" />' +
            '</p>' +
          '</form>' +
        '</div>' +
      '</div>';

  templates.newActivityCommentForm =
      '<div style="display: none;">' +
        '<div id="newActivityComment">' +
          '<form name="newMiniMessageForm" class="newMiniMessageForm">' +
            '<textarea rows="3" name="newMiniMessageText" class="miniMessageText jsActivityCommentText"></textarea>' +
            '<p class="newMiniMessageActions">' +
              '<span class="miniMessageCounter jsActivityCommentCounter"></span>' +
              '<input class="button jsWriteActivityCommentButton" name="writeActivityCommentButton" type="button" value="{{writeLabel}}" />' +
            '</p>' +
          '</form>' +
        '</div>' +
      '</div>';

  templates.deleteActivityAction =
      '<a href="#" class="jsDelete" data-activityid="{{activityId}}">{{deleteMessage}}</a>';

  templates.deleteActivityCommentAction =
      '<a href="#" class="jsDelete" data-commentid="{{commentId}}" >{{deleteMessage}}</a>';

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
  /* end templates */

  var prefs = new gadgets.Prefs();

  var docId = prefs.getString("docId");

  var currentActivities = [];
  var waitingActivities = [];

  var offset = 0;
  var waitingOffset = 0;

  var hasMoreActivities = true;

  var filter = constants.filter.all;

  function displayActivities() {
    var htmlContent = '';

    if (currentActivities.length == 0) {
      htmlContent += '<div class="noStream">' + prefs.getMsg('label.no.activity') + '</div>';
    } else {
      for (var i = 0; i < currentActivities.length; i++) {
        var currentActivity = currentActivities[i];
        if (currentActivity.activityVerb == constants.miniMessageVerb && filter !== constants.filter.events) {
          htmlContent += addActivityHtml(templates.miniMessage, currentActivity);
        } else if (currentActivity.activityVerb !== constants.miniMessageVerb && filter !== constants.filter.discussions) {
          htmlContent += addActivityHtml(templates.activity, currentActivity);
        }
      }
    }
    jQuery('#wall').html(htmlContent);

    addLikeStatus();
    addDeleteLinks();
    addReplyLinks();
    registerMoreCommentsHandler();

    if (hasMoreActivities) {
      addMoreActivitiesBar();
    } else {
      addNoMoreActivitiesText();
    }
    gadgets.window.adjustHeight();
  }

  function addActivityHtml(template, activity) {
    var commentsHtml = '';
    if (activity.comments.length > 0) {
      if (activity.comments.length > 3) {
        var moreCommentsMessage = prefs.getMsg('label.show.all') + ' ' +
          activity.comments.length + ' ' + prefs.getMsg('label.comments');
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
    activity.commentsHtml = commentsHtml;
    return Mustache.render(template, activity);
  }

  function addTabLine() {
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

    jQuery('a[data-filter]').click(function() {
      if (!$(this).is('.selected')) {
        jQuery('[data-filter]').removeClass('selected');
        $(this).addClass('selected');

        filter = $(this).attr('data-filter');
        displayActivities();
      }
    });

    jQuery('.jsPostMessage').click(function () {
      jQuery('<a href="#newMiniMessage"></a>').fancybox({
        'autoScale': true,
        'type': 'inline',
        'transitionIn': 'none',
        'transitionOut': 'none',
        'enableEscapeButton': true,
        'centerOnScroll': true
      }).click();
      jQuery('textarea.jsMiniMessageText').focus();
    });

    // $('#newMiniMessage').on('shown', function() {
    //   alert('shown!');
    // });
    // $('#newMiniMessage').modal();
  }

  function addDeleteLinks() {
    // activities
    jQuery('div[data-activityid][data-allowdeletion="true"]').each(function() {
      $(this).removeAttr('data-allowdeletion');
      var activityId = $(this).attr('data-activityid');

      var actions = $(this).find('div.jsActions');
      var htmlContent = Mustache.render(templates.deleteActivityAction,
          { activityId: activityId, deleteMessage: prefs.getMsg('command.delete') });
      actions.prepend(htmlContent);
    });

    jQuery('a[data-activityid]').click(function() {
      var activityId = jQuery(this).attr("data-activityid");
      if (!confirmDeleteMiniMessage()) {
        return false;
      }
      removeMiniMessage(activityId);
    });

    // activity comments
    jQuery('div[data-commentid][data-allowdeletion="true"]').each(function() {
      $(this).removeAttr('data-allowdeletion');
      var commentId = $(this).attr('data-commentid');

      var actions = $(this).find('div.jsCommentActions');
      var htmlContent = Mustache.render(templates.deleteActivityCommentAction,
          { commentId: commentId, deleteMessage: prefs.getMsg('command.delete') });
      actions.prepend(htmlContent);
    });

    jQuery('a[data-commentid]').click(function() {
      var commentId = jQuery(this).attr('data-commentid');
      if (!confirmDeleteMiniMessage()) {
        return false;
      }

      var activityId = $(this).parents('div[data-activityid]').attr('data-activityid');
      removeActivityComment(activityId, commentId);
    });
  }

  function addLikeStatus() {
    jQuery('div[data-activityid][data-likescount]').each(function() {
      var activityId = $(this).attr('data-activityid');
      var likesCount = $(this).attr('data-likescount');
      var userLikeStatus = $(this).attr('data-userlikestatus');
      var actions = $(this).find('div.jsActions');
      addActivityLikeStatus($(this), actions, activityId, likesCount, userLikeStatus);
    });

    jQuery('div[data-commentid][data-likescount]').each(function() {
      var commentId = $(this).attr('data-commentid');
      var likesCount = $(this).attr('data-likescount');
      var userLikeStatus = $(this).attr('data-userlikestatus');
      var actions = $(this).find('div.jsCommentActions');
      addActivityLikeStatus($(this), actions, commentId, likesCount, userLikeStatus);
    });
  }

  function addReplyLinks() {
    jQuery('div[data-activityid]').each(function() {
      var activityId = $(this).attr('data-activityid');
      var replyImageURL = NXGadgetContext.clientSideBaseUrl + 'icons/reply.png'

      var actions = $(this).find('div.jsActions');
      var htmlContent = Mustache.render(templates.replyAction,
          { activityId: activityId, replyImageURL: replyImageURL,
            replyMessage: prefs.getMsg('command.reply') });
      actions.append(htmlContent);

      $(this).find('div[data-commentid]').each(function() {
        var actions = $(this).find('div.jsCommentActions');
        actions.append(htmlContent);
      });
    });

    jQuery('.jsReply').click(function() {
      jQuery('.jsWriteActivityCommentButton').attr('data-activityid', $(this).attr('data-activityid'));
      jQuery('<a href="#newActivityComment"></a>').fancybox({
        'autoScale': true,
        'type': 'inline',
        'transitionIn': 'none',
        'transitionOut': 'none',
        'enableEscapeButton': true,
        'centerOnScroll': true
      }).click();
      jQuery('textarea.jsActivityCommentText').focus();
    });
  }

  function registerMoreCommentsHandler() {
    jQuery('.jsMoreCommentsBar').click(function() {
      var commentsContainer = $(this).parents('.jsCommentsContainer');
      commentsContainer.find('div[data-commentid]').each(function() {
        $(this).removeClass('displayN');
      });
      commentsContainer.find('.jsMoreCommentsBar').each(function() {
        $(this).remove();
      });
      gadgets.window.adjustHeight();
    });
  }

  function addMoreActivitiesBar() {
    var htmlContent = Mustache.render(templates.moreActivitiesBar,
        { moreActivitiesMessage: prefs.getMsg('label.show.more.activities') });

    jQuery('#wall').append(htmlContent);
    jQuery('.jsMoreActivitiesBar').click(function() {
      showMoreActivities();
    });
  }

  function addNoMoreActivitiesText() {
    var htmlContent = Mustache.render(templates.noMoreActivitiesBar,
        { noMoreActivitiesMessage: prefs.getMsg('label.no.more.activities') });

    jQuery('#wall').append(htmlContent);
  }

  function showMoreActivities() {
    var NXRequestParams= { operationId : 'Services.GetWallActivityStream',
      operationParams: {
        language: prefs.getLang(),
        document: docId,
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
        displayActivities();
      }
    };

    doAutomationRequest(NXRequestParams);
  }

  function loadActivityStream() {
    var NXRequestParams= { operationId : 'Services.GetWallActivityStream',
      operationParams: {
        language: prefs.getLang(),
        document: docId
      },
      operationContext: {},
      operationCallback: function(response, params) {
        currentActivities = response.data.activities;
        offset = response.data.offset;
        displayActivities();
      }
    };

    doAutomationRequest(NXRequestParams);
  }

  function pollActivityStream() {
    var NXRequestParams= { operationId : 'Services.GetWallActivityStream',
      operationParams: {
        language: prefs.getLang(),
        document: docId
      },
      operationContext: {},
      operationCallback: function(response, params) {
        var newActivities = response.data.activities;
        if (newActivities.length > 0 && currentActivities[0].id !== newActivities[0].id) {
          // there is at least one new activity
          waitingActivities = newActivities;
          waitingOffset = response.data.offset;
          addNewActivitiesBar();
          gadgets.window.adjustHeight();
        }
      }
    };

    doAutomationRequest(NXRequestParams);
  }

  function addNewActivitiesBar() {
    if (jQuery('.jsNewActivitiesBar').length > 0) {
      return;
    }

    var htmlContent = Mustache.render(templates.newActivitiesBar,
        { newActivitiesMessage: prefs.getMsg('label.show.new.activities') });

    jQuery('#wall').prepend(htmlContent);
    jQuery('.jsNnewActivitiesBar').click(function() {
      showNewActivities();
    });
  }

  function showNewActivities() {
    currentActivities = waitingActivities;
    offset = waitingOffset;
    displayActivities();
  }

  /* mini message */
  function addNewMiniMessageForm() {
    var htmlContent = Mustache.render(templates.newMiniMessageForm,
        { writeLabel: prefs.getMsg('command.write') });

    $(htmlContent).insertAfter('#wall');

    jQuery('input.jsWriteMiniMessageButton').click(function() {
      createMiniMessage();
    });
    updateMiniMessageCounter();
    jQuery('textarea.jsMiniMessageText').keyup(updateMiniMessageCounter);
    gadgets.window.adjustHeight();
  }

  function updateMiniMessageCounter() {
    var delta = 140 - jQuery('textarea.jsMiniMessageText').val().length;
    var miniMessageCounter = jQuery('.miniMessageCounter');
    miniMessageCounter.text(delta);
    miniMessageCounter.toggleClass('warning', delta < 5);
    if (delta < 0) {
      jQuery('.jsWriteMiniMessageButton').attr('disabled', 'disabled');
    } else {
      jQuery('.jsWriteMiniMessageButton').removeAttr('disabled');
    }
  }

  function createMiniMessage() {
    var miniMessageText = jQuery('textarea.jsMiniMessageText').val();
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
        loadActivityStream();
        $('textarea.jsMiniMessageText').val('');
        updateMiniMessageCounter();
      }
    };
    doAutomationRequest(opCallParameters);
    $.fancybox.close();
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
        loadActivityStream();
      }
    };
    doAutomationRequest(opCallParameters);
  }
  /* end mini message */

  /* like */
  function addActivityLikeStatus(ele, actions, activityId, likesCount, userLikeStatus) {
    ele.find('.jsLike').remove();

    var likeImageURL = userLikeStatus == 1
        ? NXGadgetContext.clientSideBaseUrl + 'icons/vote_up_active.png'
        : NXGadgetContext.clientSideBaseUrl + 'icons/vote_up_unactive.png';

    var htmlContent = Mustache.render(templates.likeAction,
        { likeImageURL: likeImageURL, likesCount: likesCount });

    var deleteLink = $(ele).find('a.jsDelete');
    if (deleteLink.length > 0) {
      $(htmlContent).insertAfter(deleteLink);
    } else {
      actions.prepend(htmlContent);
    }

    var operationId = userLikeStatus == 1
        ? constants.cancelLikeOperationId
        : constants.likeOperationId;
    $(ele).find('.jsLikeIcon').click(function() {
      var NXRequestParams= { operationId : operationId,
        operationParams: {
          activityId: activityId
        },
        operationContext: {},
        operationCallback: function(response, params) {
          var likeStatus = response.data;

          if (jQuery('div[data-activityid="' + activityId + '"]').length > 0) {
            jQuery('div[data-activityid="' + activityId + '"]').each(function() {
              $(this).attr('data-likescount', likeStatus.likesCount);
              $(this).attr('data-userlikestatus', likeStatus.userLikeStatus);
              var actions = $(this).find('div.jsActions');
              addActivityLikeStatus($(this), actions, activityId, likeStatus.likesCount, likeStatus.userLikeStatus);
            });
          } else {
            // comment
            jQuery('div[data-commentid="' + activityId + '"]').each(function() {
              $(this).attr('data-likescount', likeStatus.likesCount);
              $(this).attr('data-userlikestatus', likeStatus.userLikeStatus);
              var actions = $(this).find('div.jsCommentActions');
              addActivityLikeStatus($(this), actions, activityId, likeStatus.likesCount, likeStatus.userLikeStatus);
            });
          }
        }
      };
      doAutomationRequest(NXRequestParams);
    });
  }
  /* end like */

  /* activity comments */
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
          jQuery('div[data-commentid="'+ commentId + '"]').remove();
          gadgets.window.adjustHeight();
        }
      }
    };
    doAutomationRequest(opCallParameters);
  }

  function addNewActivityCommentForm() {
    var htmlContent = Mustache.render(templates.newActivityCommentForm,
        { writeLabel: prefs.getMsg('command.write') });

    $(htmlContent).insertAfter('#wall');

    jQuery('input.jsWriteActivityCommentButton').click(function() {
      createActivityComment();
    });
    updateActivityCommentMessageCounter();
    jQuery('textarea.jsActivityCommentText').keyup(updateActivityCommentMessageCounter);
    gadgets.window.adjustHeight();
  }

  function updateActivityCommentMessageCounter() {
    var delta = 140 - jQuery('textarea.jsActivityCommentText').val().length;
    var miniMessageCounter = jQuery('.jsActivityCommentCounter');
    miniMessageCounter.text(delta);
    miniMessageCounter.toggleClass('warning', delta < 5);
    if (delta < 0) {
      jQuery('.jsWriteActivityCommentButton').attr('disabled', 'disabled');
    } else {
      jQuery('.jsWriteActivityCommentButton').removeAttr('disabled');
    }
  }

  function createActivityComment() {
    var activityCommentText = jQuery('textarea.jsActivityCommentText').val();
    var button = jQuery('.jsWriteActivityCommentButton');
    var activityId = button.attr('data-activityid');
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
        $('textarea.jsActivityCommentText').val('');
        updateActivityCommentMessageCounter();

        var commentsContainer = $('div[data-activityId="' + activityId + '"]')
          .find('.jsCommentsContainer');
        var comment = response.data;
        comment.likeStatus = {};
        comment.likeStatus.likesCount = 0;
        comment.likeStatus.userLikeStatus = 0;
        var commentHtml = Mustache.render(templates.comment, comment);
        commentsContainer.append(commentHtml);

        commentsContainer.find('div[data-commentid="' + comment.id + '"]').each(function() {
          // like status
          var commentId = $(this).attr('data-commentid');
          var likesCount = $(this).attr('data-likescount');
          var userLikeStatus = $(this).attr('data-userlikestatus');
          var commentActions = $(this).find('div.jsCommentActions');
          addActivityLikeStatus($(this), commentActions, commentId, likesCount, userLikeStatus);

          // delete link
          var allowDeletion = $(this).attr('data-allowdeletion');
          if (allowDeletion) {
            $(this).removeAttr('data-allowdeletion');
            var htmlContent = Mustache.render(templates.deleteActivityCommentAction,
                { commentId: commentId, deleteMessage: prefs.getMsg('command.delete') });
            commentActions.prepend(htmlContent);

            commentActions.find('a[data-commentid="' + commentId + '"]').click(function() {
              var commentId = jQuery(this).attr('data-commentid');
              if (!confirmDeleteMiniMessage()) {
                return false;
              }

              var activityId = $(this).parents('div[data-activityid]').attr('data-activityid');
              removeActivityComment(activityId, commentId);
            });
          }

          var activityId = $(this).parents('div[data-activityid]').attr('data-activityid');
          var replyImageURL = NXGadgetContext.clientSideBaseUrl + 'icons/reply.png';
          var htmlContent = Mustache.render(templates.replyAction,
              { activityId: activityId, replyImageURL: replyImageURL,
                replyMessage: prefs.getMsg('command.reply') });
          commentActions.append(htmlContent);

          $(this).find('.jsReply').click(function() {
            jQuery('.jsWriteActivityCommentButton').attr('data-activityid', $(this).attr('data-activityid'));
            jQuery('<a href="#newActivityComment"></a>').fancybox({
              'autoScale': true,
              'type': 'inline',
              'transitionIn': 'none',
              'transitionOut': 'none',
              'enableEscapeButton': true,
              'centerOnScroll': true
            }).click();
            jQuery('textarea.jsActivityCommentText').focus();
          });
        });
      }
    };
    doAutomationRequest(opCallParameters);
    $.fancybox.close();
  }
  /* end activity comments */

  // gadget initialization
  gadgets.util.registerOnLoadHandler(function() {
    var contentStyleClass = prefs.getString("contentStyleClass");
    if (contentStyleClass) {
      _gel('content').className = contentStyleClass;
    }

    addTabLine();
    addNewMiniMessageForm();
    addNewActivityCommentForm();

    loadActivityStream();
    window.setInterval(pollActivityStream, 30*1000);
  });

}());
