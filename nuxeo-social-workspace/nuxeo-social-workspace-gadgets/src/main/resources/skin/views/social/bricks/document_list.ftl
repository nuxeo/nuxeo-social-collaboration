<div>
  <#if (docs?size == 0)>
    Aucun document.
  <#else>
  <div id="pageNavigationControls">
    <input type="image" onclick="javascript: documentList('${currentDoc.id}', 0,)" id="navFirstPage" src="${contextPath}/icons/action_page_rewind.gif" />
      <input type="image" onclick="javascript: documentList('${currentDoc.id}', ${prevPage})" id="navPrevPage" src="${contextPath}/icons/action_page_previous.gif"/>
      <span class="currentPageStatus" id="nxDocumentListPage">${page + 1}/${maxPage}</span>
      <input type="image" onclick="javascript: documentList('${currentDoc.id}', ${nextPage})" id="navNextPage" src="${contextPath}/icons/action_page_next.gif"/>
      <input type="image" onclick="javascript: documentList('${currentDoc.id}', ${maxPage - 1} )" id="navLastPage" src="${contextPath}/icons/action_page_fastforward.gif"/>
  </div>

  <table class="dataList">
    <thead>
      <tr>
        <th class="iconColumn"></th>
        <th>Titre</th>
        <th>Dernière modification</th>
        <th>Auteur</th>
        <th>&nbsp;</th>
      </tr>
    </thead>
    <#list docs as doc>
    <tr>
      <td><img src="${contextPath}${doc["common:icon"]}" alt="icon"/></td>
      <#if doc.isFolder>
        <td><a href="javascript: documentList('${doc.id}')" class="navigation">${doc.title}</a></td>
      <#else>
        <td><a href="javascript: goToDocument('${doc.path}', '${fullscreen_views[doc.id]}')" class="navigation">${doc.title}</a></td>
      </#if>
      <td>${doc["dc:modified"]?string("yyyy-MM-dd HH:mm")}</td>
      <td>${doc["dc:creator"]}</td>
      <td>
        <#if removable?seq_contains(doc.id)>
          <a class="button" href="javascript:confirmDeleteDocument('${doc.id}' , '${This.escapeSingleQuote(doc.title)}' )">
            <img src="${contextPath}/icons/action_delete.gif" alt="remove"></img>
            <div class="tooltip">Effacer le document</div>
          </a>
        </#if>
        <#if isPublicSocialWorkspace>
          <#if publishablePublic?seq_contains(doc.id)>
            <a class="button" href="javascript:confirmPublishDocument('${doc.id}', '${This.escapeSingleQuote(doc.title)}', true )">
              <img src="${skinPath}/icons/publish_to_all.png" alt="publish private"></img>
              <div class="tooltip">Rendre ce document public</div>
            </a>
          </#if>
          <#if publishablePrivate?seq_contains(doc.id)>
            <a class="button" href="javascript:confirmPublishDocument('${doc.id}', '${This.escapeSingleQuote(doc.title)}', false )">
              <img src="${skinPath}/icons/publish_to_social_workspace.png" alt="make it public"></img>
              <div class="tooltip">Restreindre l'accés de ce document à l'espace collaboratif</div>
            </a>
          </#if>
        <#else>
          <a class="button disabled" href="#">
            <img src="${skinPath}/icons/publish_to_all_disabled.png" alt="publish public"></img>
            <div class="tooltip">Cet espace collabortif est privé, aucun document ne peut être public</div>
          </a>
        </#if>
        <#if This.hasAttachment(doc)>
          <a class="button" href="${contextPath}/nxfile/default/${doc.id}/file:content/">
            <img src="${contextPath}/icons/download.png" alt="download"></img>
            <div class="tooltip">${Context.getMessage("label.action.download")}</div>
          </a>
        </#if>
      </td>
    </tr>
    </#list>
  </table>
  </#if>
</div>
<#include "@bricks/context">
