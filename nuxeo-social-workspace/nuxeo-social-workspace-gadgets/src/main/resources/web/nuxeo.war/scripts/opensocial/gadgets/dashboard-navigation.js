// override cell builder
var mkCell = (function() {
  var original_mkCell = mkCell;
  return function(colDef, dashBoardItem) {
    if (colDef.view != 'dashboard') {
      return original_mkCell(colDef, dashBoardItem);
    }
    var html = "";
    html += "<td><a target = \"_top\" title=\"";
    html += dashBoardItem.title;
    html += "\" href=\"";
    html += NXGadgetContext.clientSideBaseUrl;
    html += "collaboration/default";
    html += encode(dashBoardItem.path);
    html += "/social@";
    html += colDef.view;
    html += "?tabIds=" + encodeURIComponent("MAIN_TABS:collab");
    if (top && top.currentConversationId) {
      html += "&conversationId=" + top.currentConversationId;
    }
    html += "\" />";
    html += dashBoardItem.title;
    html += "</a></td>";
    return html
  }
})();
