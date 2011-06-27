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
				<td>${doc.title}</td>
			</#if>
			<td>${doc["dc:modified"]?string("yyyy-MM-dd HH:mm")}</td>
			<td>${doc["dc:creator"]}</td>
			<td>
				<a href="javascript: deleteDocument('${doc.id}')"><img src="${contextPath}/icons/action_delete.gif" alt="remove"></a>
				<a href="javascript: publishDocument('${doc.id}')"><img src="${contextPath}/icons/blog_folder.png" alt="publish"></a>
			</td>
		</tr>
		</#list>
	</table>
	</#if>
</div>
<#include "@bricks/context">
