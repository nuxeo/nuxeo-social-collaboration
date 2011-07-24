var prefs = new gadgets.Prefs();

// configure Automation REST call
var NXRequestParams={ operationId : 'Social.Provider',            // id of operation or chain to execute
  operationParams : { query : "Select * from News WHERE ecm:currentLifeCycleState <> 'deleted'" +
      "AND ecm:isProxy = 1",
       pageSize : 5,
       socialWorkspacePath : getTargetContextPath(),
       sortInfo : "dc:modified 1"
  },  // parameters for the chain or operation
  operationContext : {},                                                // context
  operationDocumentProperties : "common,dublincore,note",               // schema that must be fetched from resulting documents
  entityType : 'documents',                                             // result type : only document is supported for now
  usePagination : true,                                                 // manage pagination or not
  displayMethod : displayArticles,                                  // js method used to display the result
  displayColumns : [ { type: 'builtin', field: 'icon'},                 // minimalist layout listing
                   { type: 'builtin', field: 'titleWithLink', label: '__MSG_label.dublincore.title__'},
                   { type: 'date', field: 'dc:modified', label: '__MSG_label.dublincore.modified__'},
                   { type: 'text', field: 'dc:creator', label: '__MSG_label.dublincore.creator__'},
                   { type: 'text', field: 'note:note', label: '__MSG_label.article.abstract__'}
                   ],
                   noEntryLabel: prefs.getMsg('label.gadget.no.document')
};

function displayArticles(entries, nxParams) {

  var html = "";
  if(entries && entries.length <= 0){

    nxParams.noEntryLabel = nxParams.noEntryLabel || 'Nothing to show.';
      html = '<p>' + nxParams.noEntryLabel + '</p> </br>';
      _gel('pageNavigationControls').style.display = 'none';
    }else{
      html=displayListOfArticles(entries,nxParams);
    }
  _gel("nxDocumentListData").innerHTML = html;

  _gel("nxDocumentList").style.display = 'block';

  gadgets.window.adjustHeight();
}

function displayListOfArticles(entries,nxParams){
  var html ="";
  for (var i = 0; i < entries.length; i++) {
      var entry = entries[i];
      var date = new Date(entry.properties["dc:modified"]);
      html += '<div class="article">';
      html += '<p>' + entry.properties["dc:creator"] + ', ' + date.toLocaleDateString() + ' - ';
      html += '<a target ="_top" class="article_title" title="';
      html += entry.title;
      html += "\" href=\"";
      html += NXGadgetContext.clientSideBaseUrl;
      html += "nxpath/default";
      html += entry.path;
      html += "@view_social_wokspace_news_item";
      html += "\" >";
      html += entry.title;
      html += '</a></p>';
      var text = entry.properties["note:note"];
      text = text.substring(0, text.length > 300 ? 300 : text.length);
      html += '<p>' + text + '</p>';
      html += '</div>';
    }

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
