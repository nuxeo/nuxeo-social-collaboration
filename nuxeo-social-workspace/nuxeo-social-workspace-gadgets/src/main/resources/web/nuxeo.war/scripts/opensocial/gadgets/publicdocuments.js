var prefs = new gadgets.Prefs();

//configuration leaved here to allow label traduction by shindig
// configure Automation REST call
var NXRequestParams = { operationId:'Social.Provider', // id of operation or chain to execute
  operationParams:{
    providerName: 'public_documents',
    pageSize:5,
    contextPath:getTargetContextPath(),
    onlyPublicDocuments:"true", //restrict the research to public-section
    sortInfo:"dc:modified 1"
  }, // parameters for the chain or operation
  operationContext:{}, // context
  operationDocumentProperties:"common,dublincore,note,file", // schema that must be fetched from resulting documents
  entityType:'documents', // result type : only document is supported for now
  usePagination:true, // manage pagination or not
  displayMethod:displayResultsForPublicDocument, // js method used to display the result
  noEntryLabel: prefs.getMsg('label.gadget.no.document')
};

function displayResultsForPublicDocument(entries, nxParams) {

  var html = "";
  if(entries && entries.length <= 0){
      nxParams.noEntryLabel = nxParams.noEntryLabel || 'Nothing to show.';
      html = '<p>' + nxParams.noEntryLabel + '</p> </br>';
      _gel('pageNavigationControls').style.display = 'none';
    }else{
      html=displayDocuments(entries,nxParams);
    }
  _gel("nxDocumentListData").innerHTML = html;

  _gel("nxDocumentList").style.display = 'block';

  gadgets.window.adjustHeight();
}

function displayDocuments(entries,nxParams){
  var html ="<table class=\"dataList\"> ";
  for (var i = 0; i < entries.length; i++) {
      var entry = entries[i];
      html += '<tr >';
      html += '<td>';
      html += '<a target ="_top" class="article_title" title="';
      html += entry.title;
      html += "\" href=\"";
      html += NXGadgetContext.clientSideBaseUrl;
      html += "collaboration/default";
      html += encode(entry.path);
      html += "@view_documents";
      html += '\" >';
      html += entry.title;
      html += '</a>';
      html += '</td>';
      html += '<td>';
      if (entry.properties["file:content"]){
          html += '<a target ="_top" href=\"';
          html += NXGadgetContext.clientSideBaseUrl;
          html += "nxfile/default/";
          html += entry.uid;
          html += "/blobholder:0/";
          html += entry.properties["file:content"]["name"];
          html += '\" >';
          html += "<img src=\"/nuxeo/icons/download.png\" alt=\"Download\" title=\"Download\">";
          html += '</a>';
      }
      html += '</td>';
      html += '</tr>';
    }
    html += "</table>";

    _gel('navFirstPage').onclick = function(e) {
        firstPage(nxParams)
    };
    _gel('navPrevPage').onclick = function(e) {
        prevPage(nxParams)
    };
    _gel('navNextPage').onclick = function(e) {
        nextPage(nxParams)
    };
    _gel('navLastPage').onclick = function(e) {
        lastPage(nxParams)
    };
    if (nxParams.usePagination) {
        _gel('nxDocumentListPage').innerHTML = (currentPage + 1) + "/" + maxPage;
    } else {
        console.log("hide nav controls");
        _gel('pageNavigationControls').style.display = 'none';
    }

  return html;
}

// execute automation request onload
gadgets.util.registerOnLoadHandler(function() {doAutomationRequest(NXRequestParams);} );
