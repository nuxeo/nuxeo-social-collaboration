// override cell builder
var mkCell = (function() {
  var original_mkCell = mkCell;
  return function(colDef, dashBoardItem) {
    if (colDef.view != 'social_dashboard') {
      return original_mkCell(colDef, dashBoardItem);
    }
    var html = "";
    html += "<td><a target = \"_top\" title=\"";
    html += dashBoardItem.title;
    html += "\" href=\"";
    html += NXGadgetContext.clientSideBaseUrl;
    html += "nxpath/default";
    html += dashBoardItem.path;
    html += "/social@";
    html += colDef.view;
    html += "\" />";
    html += dashBoardItem.title;
    html += "</a></td>";
    return html
  }
})();
