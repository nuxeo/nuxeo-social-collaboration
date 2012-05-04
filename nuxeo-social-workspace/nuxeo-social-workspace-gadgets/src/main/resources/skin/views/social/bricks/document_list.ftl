<div>
  <#if (docs?size == 0)>
  	${Context.getMessage("label.empty.list")}
  <#else>
  <div id="pageNavigationControls">
    <input type="image" onclick="javascript: Library.documentList('${currentDoc.id}', 0,)" id="navFirstPage" src="${contextPath}/icons/action_page_rewind.gif" />
      <input type="image" onclick="javascript: Library.documentList('${currentDoc.id}', ${prevPage})" id="navPrevPage" src="${contextPath}/icons/action_page_previous.gif"/>
      <span class="currentPageStatus" id="nxDocumentListPage">${page + 1}/${maxPage}</span>
      <input type="image" onclick="javascript: Library.documentList('${currentDoc.id}', ${nextPage})" id="navNextPage" src="${contextPath}/icons/action_page_next.gif"/>
      <input type="image" onclick="javascript: Library.documentList('${currentDoc.id}', ${maxPage - 1} )" id="navLastPage" src="${contextPath}/icons/action_page_fastforward.gif"/>
  </div>

  <table class="dataList">
    <thead>
      <tr>
        <th class="iconColumn"></th>
        <th>${Context.getMessage("label.dublincore.title")}</th>
        <th>${Context.getMessage("label.dublincore.modified")}</th>
        <th>${Context.getMessage("label.dublincore.creator")}</th>
        <th>&nbsp;</th>
      </tr>
    </thead>
    <#list docs as doc>
    <tr>
      <td><img src="${contextPath}${doc["common:icon"]}" alt="icon"/></td>
      <#if doc.isFolder>
        <td><a href="javascript: Library.documentList('${doc.id}')" class="navigation">${doc.title?xml}</a></td>
      <#else>
        <td><a href="javascript: Library.goToDocument('${This.escapePath(doc.path)}', '${collaboration_views[doc.id]}')" class="navigation">${doc.title?xml}</a></td>
      </#if>
      <td>${doc["dc:modified"]?string("yyyy-MM-dd HH:mm")}</td>
      <td>${doc["dc:creator"]}</td>
      <td>
        <#if removable?seq_contains(doc.id)>
          <a class="button" href="javascript:Library.confirmDeleteDocument('${doc.id}' , '${This.escape(doc.title)}' )">
            <img src="${contextPath}/icons/action_delete.gif" alt="remove"></img>
            <div class="tooltip">${Context.getMessage("tooltip.remove.document")}</div>
          </a>
        </#if>
        <#if isPublicSocialWorkspace>
          <#if publishablePublic?seq_contains(doc.id)>
            <a class="button" href="javascript:Library.confirmPublishDocument('${doc.id}', '${This.escape(doc.title)}', true )">
              <img src="${skinPath}/icons/publish_to_all.png" alt="publish private"></img>
              <div class="tooltip">${Context.getMessage("tooltip.publish.document")}</div>
            </a>
          </#if>
          <#if publishablePrivate?seq_contains(doc.id)>
            <a class="button" href="javascript:Library.confirmPublishDocument('${doc.id}', '${This.escape(doc.title)}', false )">
              <img src="${skinPath}/icons/publish_to_social_workspace.png" alt="make it public"></img>
              <div class="tooltip">${Context.getMessage("tooltip.restrict.document")}</div>
            </a>
          </#if>
        <#else>
          <a class="button disabled" href="#">
            <img src="${skinPath}/icons/publish_to_all_disabled.png" alt="publish public"></img>
            <div class="tooltip">${Context.getMessage("tooltip.can.not.publish.document")}</div>
          </a>
        </#if>
        <#assign attachment = This.getAttachment(doc) />
        <#if attachment != null>
          <a class="button" href="${contextPath}/nxfile/default/${doc.id}/blobholder:0/${attachment.filename}">
            <img src="${contextPath}/icons/download.png" alt="download"></img>
            <div class="tooltip">${Context.getMessage("label.action.download")}</div>
          </a>
        </#if>
        <a class="button" href="${contextPath}/nxpath/default${doc.path}@view_documents" target="_blank">
          <img src="${contextPath}/icons/external.gif" alt="openInDM"></img>
          <div class="tooltip">${Context.getMessage("label.action.openInDM")}</div>
        </a>
      </td>
    </tr>
    </#list>
  </table>
  </#if>
</div>
<#include "@bricks/context">
