var prefs = new gadgets.Prefs();

var currentActivities = [];
var waitingActivities = [];

var activityStreamType = prefs.getString("activityStreamType");

function displayActivities() {
  var htmlContent = '';

  for (var i = 0; i < currentActivities.length; i++) {
    var cssClass = 'activity';
    htmlContent += '<div class="' + cssClass + '">';
    htmlContent += '<div>';
    htmlContent += '<span>' + currentActivities[i].activityMessage + '</span>';
    htmlContent += '</div>';
    htmlContent += '<div>';
    htmlContent += '<span>' + currentActivities[i].publishedDate + '</span>';
    htmlContent += '</div>';
    htmlContent += '</div>';
  }

  _gel('activitiesContainer').innerHTML = htmlContent;
  gadgets.window.adjustHeight();
}

function loadActivityStream() {
  var NXRequestParams= { operationId : 'Services.GetActivityStreamForActor',
    operationParams: {
      language: prefs.getLang(),
      activityStreamType: activityStreamType
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
  var NXRequestParams= { operationId : 'Services.GetActivityStreamForActor',
    operationParams: {
      language: prefs.getLang(),
      activityStreamType: activityStreamType
    },
    operationContext: {},
    operationCallback: function(response, params) {
      var newActivities = response.data;
      if (currentActivities[0].id !== newActivities[0].id) {
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
  var bar = document.createElement('div');
  bar.id = 'newActivitiesBar';
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
