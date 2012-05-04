var ContextManagement = {};

var contextListLoaded = false;

ContextManagement.getTargetRepository = function () {
    return gadgets.util.unescapeString(prefs.getString("nuxeoTargetRepository"));
}

ContextManagement.getTargetContextPath = function () {
    var targetContextPath = gadgets.util.unescapeString(prefs.getString("nuxeoTargetContextPath"));
    if (targetContextPath == null || targetContextPath == '') {
        targetContextPath = "/"; //in Nuxeo pref should be set at creation time
    }
    return targetContextPath;
}

ContextManagement.getTargetContextObject = function () {
    var targetContextObject = gadgets.util.unescapeString(prefs.getString("nuxeoTargetContextObject"));
    if (targetContextObject == null || targetContextObject == '') {
        targetContextObject = "Domain"; //in Nuxeo pref should be set at creation time
    }
    return targetContextObject;
}

ContextManagement.saveContext = function () {
    var contextPath = _gel("contextPathChooser").value;
    prefs.set("nuxeoTargetContextPath", contextPath);
    _gel("contextChooser").style.display = "none";
}

ContextManagement.displayContextChooser = function () {

    var query = "select * from ";
    query += ContextManagement.getTargetContextObject();
    query += " where ecm:currentLifeCycleState != 'deleted'";

    var ContextRequestParams = {
        operationId: 'Document.Query',
        operationParams: {
            query: query
        },
        operationContext: {},
        operationDocumentProperties: "dublincore",
        entityType: 'documents',
        usePagination: false,
        displayMethod: ContextManagement.availableContextsReceived
    };

    if (contextListLoaded) {
        ContextManagement.showContextPathSelector();
    } else {
        doAutomationRequest(ContextRequestParams);
    }
}

ContextManagement.availableContextsReceived = function (entries, nxParams) {

    var elSel = _gel("contextPathChooser");

    var selectedValue = ContextManagement.getTargetContextPath();

    for (var i = 0; i < entries.length; i++) {

        var elOptNew = document.createElement('option');
        elOptNew.text = entries[i].title;
        elOptNew.value = entries[i].path;
        if (elOptNew.value == selectedValue) {
            elOptNew.selected = true;
        }
        try {
            elSel.add(elOptNew, null); // standards compliant; doesn't work in IE
        } catch (ex) {
            elSel.add(elOptNew); // IE only
        }
    }
    contextListLoaded = true;
    ContextManagement.showContextPathSelector();
}

ContextManagement.showContextPathSelector = function () {
    _gel("contextChooser").style.display = "block";
}

ContextManagement.initContextPathSettingsButton = function () {
    if (gadgets.nuxeo) {
        var permission = gadgets.nuxeo.isEditable();
        if (permission) {
            _gel("contextButton").style.display = "block";
        }
    } else {
        _gel("contextButton").style.display = "block";
    }
}