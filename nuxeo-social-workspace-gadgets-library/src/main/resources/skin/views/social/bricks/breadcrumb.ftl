<div class="breadcrumb">
	${Context.getMessage("label.path")}
	<#list ancestors as doc>
	  <a href="javascript: Library.documentList('${socialWorkspace.id}')"> &gt; </a>
		<a href="javascript: Library.documentList('${doc.id}')">${doc.title}</a>
	</#list>
</div>
