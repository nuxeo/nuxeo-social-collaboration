<div class="breadcrumb">
	${Context.getMessage("label.path")}:
	<a href="javascript: documentList('${socialWorkspace.id}')"> &gt; </a>
	<#list ancestors as doc>
		<a href="javascript: documentList('${doc.id}')">${doc.title}</a> &gt;
	</#list>
</div>