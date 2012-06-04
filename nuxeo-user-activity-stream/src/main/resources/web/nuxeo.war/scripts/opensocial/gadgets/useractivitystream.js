(function() {

  /* constants */
  var constants = {
    noActivityTypeIcon: "icons/activity_empty.png"
  };
  /* end constants */

  /* templates */
  var templates = {};
  templates.activity =
    '<div class="miniMessage jsMainActivity" data-activityid="{{id}}" data-likescount="{{likeStatus.likesCount}}" data-userlikestatus="{{likeStatus.userLikeStatus}}">' +
      '<div class="container">' +
        '<div class="message">' +
          '<span class="activityType"><img src="{{icon}}"></span>' +
          '<span class="avatar"><img src="{{actorAvatarURL}}" alt="{{displayActor}}" /></span>' +
          '<div class="event">{{{activityMessage}}}</div>' +
        '</div>' +
        '<div class="actions jsActions">' +
          '<span class="timestamp">{{publishedDate}}</span>' +
        '</div>' +
      '</div>' +
    '</div>';

  templates.newActivitiesBar =
    '<div class="newActivitiesBar jsNewActivitiesBar">{{newActivitiesMessage}}</div>';

  templates.moreActivitiesBar =
    '<div class="moreActivitiesBar jsMoreActivitiesBar">{{moreActivitiesMessage}}</div>';

  templates.noMoreActivitiesBar =
      '<div class="moreActivitiesBar noMore">{{noMoreActivitiesMessage}}</div>';
  /* end templates */

  var prefs = new gadgets.Prefs();

  var activityStreamType = prefs.getString("activityStreamType");
  var actor = prefs.getString("actor");

  var currentActivities = [];
  var waitingActivities = [];

  var offset = 0;
  var waitingOffset = 0;

  var hasMoreActivities = true;

  function displayActivities() {
    var htmlContent = '';

    if (currentActivities.length == 0) {
      htmlContent += '<div class="noStream">' + prefs.getMsg('label.no.activity') + '</div>';
    } else {
      for (var i = 0; i < currentActivities.length; i++) {
        var currentActivity = currentActivities[i];
        htmlContent += buildActivityHtml(templates.activity, currentActivity);
      }
    }

    $('#container').html(htmlContent);
    if (hasMoreActivities) {
      addMoreActivitiesBarHtml();
      registerMoreActivityBarHandler();
    } else {
      addNoMoreActivitiesTextHtml();
    }
    gadgets.window.adjustHeight();
  }

  /* HTML building functions */
  function buildActivityHtml(template, activity) {
    if (activity.icon.indexOf(NXGadgetContext.clientSideBaseUrl) < 0) {
      var icon = activity.icon;
      if (icon != null && icon.length > 0) {
        if (icon[0] == '/') {
          icon = icon.substring(1);
        }
      } else {
        icon = constants.noActivityTypeIcon;
      }
      activity.icon = NXGadgetContext.clientSideBaseUrl + icon;
    }
    return Mustache.render(template, activity);
  }

  function addMoreActivitiesBarHtml() {
    var htmlContent = Mustache.render(templates.moreActivitiesBar,
        { moreActivitiesMessage: prefs.getMsg('label.show.more.activities') });
    $('#container').append(htmlContent);
  }

  function addNoMoreActivitiesTextHtml() {
    var htmlContent = Mustache.render(templates.noMoreActivitiesBar,
        { noMoreActivitiesMessage: prefs.getMsg('label.no.more.activities') });
    $('#container').append(htmlContent);
  }

  function addNewActivitiesBarHtml() {
    if ($('.jsNewActivitiesBar').length > 0) {
      return;
    }

    var htmlContent = Mustache.render(templates.newActivitiesBar,
        { newActivitiesMessage: prefs.getMsg('label.show.new.activities') });
    $('#container').prepend(htmlContent);
  }
  /* end HTML building functions */

  /* handler functions */
  function registerMoreActivityBarHandler() {
    $('.jsMoreActivitiesBar').click(function() {
      showMoreActivities();
    });
  }

  function showMoreActivities() {
    var NXRequestParams= { operationId : 'Services.GetActivityStream',
      operationParams: {
        language: prefs.getLang(),
        actor: actor,
        activityStreamType: activityStreamType,
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

  function registerNewActivitiesBarHandler() {
    $('.jsNewActivitiesBar').click(function() {
      showNewActivities();
    });
  }

  function showNewActivities() {
    currentActivities = waitingActivities;
    offset = waitingOffset;
    displayActivities();
  }
  /* end handler functions */

  gadgets.util.registerOnLoadHandler(function() {
    var contentStyleClass = prefs.getString("contentStyleClass");
    if (contentStyleClass) {
      _gel('content').className = contentStyleClass;
    }

    loadActivityStream();
    window.setInterval(pollActivityStream, 30*1000);
  });

  function loadActivityStream() {
    var NXRequestParams= { operationId : 'Services.GetActivityStream',
      operationParams: {
        language: prefs.getLang(),
        actor: actor,
        activityStreamType: activityStreamType
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
    var NXRequestParams= { operationId : 'Services.GetActivityStream',
      operationParams: {
        language: prefs.getLang(),
        actor: actor,
        activityStreamType: activityStreamType
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

}());
