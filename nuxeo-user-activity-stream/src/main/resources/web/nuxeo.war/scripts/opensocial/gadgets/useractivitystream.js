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
      htmlContent += '<div class="activity">';
      htmlContent += '<div class="timestamp">';
      htmlContent += currentActivities[i].publishedDate;
      htmlContent += '</div>';
      htmlContent += '<div class="activityMessage">';
      htmlContent += currentActivities[i].activityMessage;
      htmlContent += '</div>';
      htmlContent += '</div>';
    }
  }

  _gel('activitiesContainer').innerHTML = htmlContent;
  if (hasMoreActivities) {
    addMoreActivitiesBar();
  } else {
    addNoMoreActivitiesText();
  }
  gadgets.window.adjustHeight();
}

function addMoreActivitiesBar() {
  var bar = document.createElement('div');
  bar.id = 'moreActivitiesBar';
  bar.className = 'moreActivitiesBar';
  bar.innerHTML = prefs.getMsg('label.show.more.activities');
  bar.onclick = showMoreActivities;
  var container = _gel('activitiesContainer');
  container.insertBefore(bar, null);
}

function addNoMoreActivitiesText() {
  var bar = document.createElement('div');
  bar.id = 'moreActivitiesBar';
  bar.className = 'moreActivitiesBar noMore';
  bar.innerHTML = prefs.getMsg('label.no.more.activities');
  var container = _gel('activitiesContainer');
  container.insertBefore(bar, null);
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
        addNewActivitiesBar();
        gadgets.window.adjustHeight();
      }
    }
  };

  doAutomationRequest(NXRequestParams);
}

function addNewActivitiesBar() {
  if (document.getElementById('newActivitiesBar') !== null) {
    return;
  }

  var bar = document.createElement('div');
  bar.id = 'newActivitiesBar';
  bar.className = 'newActivitiesBar';
  bar.innerHTML = prefs.getMsg('label.show.new.activities');
  bar.onclick = showNewActivities;
  var container = _gel('activitiesContainer');
  container.insertBefore(bar, container.firstChild);
}

function showNewActivities() {
  currentActivities = waitingActivities;
  offset = waitingOffset;
  displayActivities();
}

gadgets.util.registerOnLoadHandler(function() {
  var contentStyleClass = prefs.getString("contentStyleClass");
  if (contentStyleClass) {
    _gel('content').className = contentStyleClass;
  }

  loadActivityStream();
  window.setInterval(pollActivityStream, 30*1000);
});
