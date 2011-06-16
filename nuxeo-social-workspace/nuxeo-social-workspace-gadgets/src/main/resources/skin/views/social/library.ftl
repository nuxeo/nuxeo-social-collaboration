<h3>${Context.getMessage("label.community")}: ${socialWorkspace.title}</h3>

<div class="toolBox">
  <div class="path">
		${Context.getMessage("label.path")}:
		<a href="javascript: loadContent('${socialWorkspace.id}')"> &gt; </a>
		<#list ancestors as doc>
		<a href="javascript: loadContent('${doc.id}')">${doc.title}</a> &gt;
		</#list>
	</div>
	<div class="links">
		<a href="javascript: loadContent('${currentDoc.id}', ${page})"><img src="${skinPath}/icons/refresh.png" alt="refresh"/></a>
		<#if parent?? >
			<a href="javascript: loadContent('${parent.id}')"><img src="${skinPath}/icons/folder_up.png" alt="goToParent"/></a>
		</#if>
		<#if (ancestors?size > 1) >
		<a href="javascript: loadContent('${socialWorkspace.id}')"><img src="${skinPath}/icons/root.png" alt="goToRoot"/></a>
		</#if>
  </div>
  <div class="clear"></div>
</div>


<div>
	<#if (children?size == 0)>
		No documents.
	<#else>
	<div id="pageNavigationControls">
    	<input type="image" onclick="javascript: loadContent('${currentDoc.id}', 0)" 				id="navFirstPage" src="${contextPath}/icons/action_page_rewind.gif" />
        <input type="image" onclick="javascript: loadContent('${currentDoc.id}', ${prevPage} )" 	id="navPrevPage" src="${contextPath}/icons/action_page_previous.gif"/>
        <span class="currentPageStatus" id="nxDocumentListPage">${page + 1}/${maxPage}</span>
        <input type="image" onclick="javascript: loadContent('${currentDoc.id}', ${nextPage} )" 	id="navNextPage" src="${contextPath}/icons/action_page_next.gif"/>
        <input type="image" onclick="javascript: loadContent('${currentDoc.id}', ${maxPage - 1} )" 	id="navLastPage" src="${contextPath}/icons/action_page_fastforward.gif"/>
    </div>


	<table class="dataList">
	  	<thead>
	  		<tr>
	  			<th class="iconColumn"></th>
	  			<th>${Context.getMessage("label.dublincore.title")}</th>
	  			<th>${Context.getMessage("label.dublincore.modified")}</th>
	  			<th>${Context.getMessage("label.dublincore.creator")}</th>
	  		</tr>
	  	</thead>
	    <#list children as doc>
		<tr>
			<td><img src="${contextPath}${doc["common:icon"]}" alt="icon"/></td>
			<#if doc.isFolder>
				<td><a href="javascript: loadContent('${doc.id}')" class="navigation">${doc.title}</a></td>
			<#else>
				<td>${doc.title}</td>
			</#if>
			<td>${doc["dc:modified"]?string("yyyy-MM-dd HH:mm")}</td>
			<td>${doc["dc:creator"]}</td>
		</tr>
		</#list>
	</table>
	</#if>
</div>

</div>