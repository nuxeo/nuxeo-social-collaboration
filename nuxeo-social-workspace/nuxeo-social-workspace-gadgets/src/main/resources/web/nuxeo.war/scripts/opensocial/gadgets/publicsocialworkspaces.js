var prefs = new gadgets.Prefs();

function getPublicSociaWorkspaces() {
  var pattern = jQuery('#queryText').val();

  var NXRequestParams = { operationId: 'SocialWorkspace.GetPublicSocialWorkspaces',
    operationParams: { pattern: pattern },
    operationContext: {},
    operationDocumentProperties: "common,dublincore",
    entityType: 'documents',
    usePagination: true,
    displayMethod: displayDocumentList,
    displayColumns: [
      {type: 'builtin', field: 'icon'},
      {type: 'builtin', field: 'titleWithLink', label: prefs.getMsg('label.dublincore.title'), view: 'social_dashboard', codec: 'collaboration'},
      {type: 'date', field: 'dc:modified', label: prefs.getMsg('label.dublincore.modified') },
      {type: 'text', field: 'dc:creator', label: prefs.getMsg('label.dublincore.creator') }
    ],
    noEntryLabel: prefs.getMsg('label.info.no.social.workspace.found') };

  doAutomationRequest(NXRequestParams);
}

function displaySearchForm() {
  var searchFormHtml = '<div class="wideDefinition">';
  searchFormHtml += '<input class="searchBox" name="queryText" id="queryText" type="text"></input>';
  searchFormHtml += '<a href="javascript:getPublicSociaWorkspaces()">' + prefs.getMsg('command.search') + '</a>';
  searchFormHtml += '<div class="clear"></div>';
  searchFormHtml += '</div>';

  jQuery('#search').html(searchFormHtml);
  jQuery('#queryText').keydown(function (event) {
    if (event.keyCode == '13') {
      getPublicSociaWorkspaces();
    }
  });
}

gadgets.util.registerOnLoadHandler(function () {
  displaySearchForm();
  getPublicSociaWorkspaces();
});
