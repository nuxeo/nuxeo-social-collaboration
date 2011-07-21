<div class="links">
	<a href="${This.path}/selectDocTypeToCreate?docRef=${currentDoc.id}" class="addPopup"><img src="${skinPath}/icons/add_document.gif" alt="create document"/></a>
	<a href="${This.path}/createDocumentForm?docRef=${currentDoc.id}&doctype=Folder" class="addPopup"><img src="${skinPath}/icons/add_folder.gif" alt="create folder"/></a>
	<a href="javascript: documentList('${currentDoc.id}', ${page})"><img src="${skinPath}/icons/refresh.png" alt="refresh"/></a>

	<#if parent?? >
		<a href="javascript: documentList('${parent.id}')"><img src="${skinPath}/icons/folder_up.png" alt="goToParent"/></a>
	</#if>
	<#if (ancestors?size > 1) >
		<a href="javascript: documentList('${socialWorkspace.id}')"><img src="${skinPath}/icons/root.png" alt="goToRoot"/></a>
	</#if>
</div>