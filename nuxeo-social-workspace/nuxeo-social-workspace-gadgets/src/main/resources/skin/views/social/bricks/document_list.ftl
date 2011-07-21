<div>
	<#if (docs?size == 0)>
		No documents.
	<#else>
	<div id="pageNavigationControls">
    	<input type="image" onclick="javascript: documentList('${currentDoc.id}', 0, 			 )" 	id="navFirstPage" src="${contextPath}/icons/action_page_rewind.gif" />
        <input type="image" onclick="javascript: documentList('${currentDoc.id}', ${prevPage}   )" 	id="navPrevPage" src="${contextPath}/icons/action_page_previous.gif"/>
        <span class="currentPageStatus" id="nxDocumentListPage">${page + 1}/${maxPage}</span>
        <input type="image" onclick="javascript: documentList('${currentDoc.id}', ${nextPage}  )" 	id="navNextPage" src="${contextPath}/icons/action_page_next.gif"/>
        <input type="image" onclick="javascript: documentList('${currentDoc.id}', ${maxPage - 1} )" 	id="navLastPage" src="${contextPath}/icons/action_page_fastforward.gif"/>
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
				<td><a href="javascript: documentList('${doc.id}')" class="navigation">${doc.title}</a></td>
			<#else>
				<td><a href="javascript: goToDocument('${doc.path}')" class="navigation">${doc.title}</a></td>
			</#if>
			<td>${doc["dc:modified"]?string("yyyy-MM-dd HH:mm")}</td>
			<td>${doc["dc:creator"]}</td>
			<td>
				<a href="javascript: confirmDeleteDocument('${doc.id}' , '${doc.title}' )"><img src="${contextPath}/icons/action_delete.gif" alt="remove"></a>

				<#if publishablePublic?seq_contains(doc.id)>
					<a href="javascript: confirmPublishDocument('${doc.id}', '${doc.title}', true )"><img src="${skinPath}/icons/publish_to_all.png" alt="publish private"></a>
				</#if>
				<#if publishablePrivate?seq_contains(doc.id)>
					<a href="javascript: confirmPublishDocument('${doc.id}', '${doc.title}', false )"><img src="${skinPath}/icons/publish_to_social_workspace.png" alt="publish public"></a>
				</#if>

			</td>
		</tr>
		</#list>
	</table>
	</#if>
</div>
<#include "@bricks/context">
