var prefs = new gadgets.Prefs();

var currentActivities = [];
var waitingActivities = [];

var socialWorkspacePath = getTargetContextPath();

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
  gadgets.window.adjustHeight();
}

function loadActivityStream() {
  var NXRequestParams= { operationId : 'Services.GetSocialWorkspaceActivityStream',
    operationParams: {
      language: prefs.getLang(),
      contextPath: socialWorkspacePath
    },
    operationContext: {},
    operationCallback: function(response, params) {
      currentActivities = response.data;
      displayActivities();
    }
  };

  doAutomationRequest(NXRequestParams);
}

function pollActivityStream() {
  var NXRequestParams= { operationId : 'Services.GetSocialWorkspaceActivityStream',
    operationParams: {
      language: prefs.getLang(),
      contextPath: socialWorkspacePath
    },
    operationContext: {},
    operationCallback: function(response, params) {
      var newActivities = response.data;
      if (newActivities.length > 0 && currentActivities[0].id !== newActivities[0].id) {
        // there is at least one new activity
        waitingActivities = newActivities;
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
  displayActivities();
}

gadgets.util.registerOnLoadHandler(function() {
  loadActivityStream();
  window.setInterval(pollActivityStream, 30*1000);
});
