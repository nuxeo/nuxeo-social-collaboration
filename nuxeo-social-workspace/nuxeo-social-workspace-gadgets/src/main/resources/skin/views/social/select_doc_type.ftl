<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<title></title>
<link href="${skinPath}/css/select_doc_type.css" rel="stylesheet" type="text/css">
</head>
<body>
<form class="selectDocType" action="${This.path}/navigateToCreationForm" method="post" enctype="application/x-www-form-urlencoded" target="_parent">
<h3>Add a Document in ${currentDoc.title} </h3>
<#if (categories?size == 0)>
	Can't create doc in ${currentDoc.title}
<#else>

<h4>Choose Document Type to Create</h4>

<table>
  <tr>
  <#list categories as category>
    <#assign docTypesList = docTypes[category]>
      <td>${category}</td>
      <#list docTypesList as docType>
      	<td>
      	  <a href="${This.path}/createDocumentForm?docRef=${currentDoc.id}&doctype=${docType.id}">
      	    <img src="${contextPath}${docType.icon}" alt="create ${docType.id}"/>  ${docType.label}
      	  </a>
      	</td>
      </#list>
  </tr>
  </#list>
</table>
</#if>

</form>
</body>
</html>