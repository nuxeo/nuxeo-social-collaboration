<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<title></title>
<link href="${skinPath}/css/select_doc_type.css" rel="stylesheet" type="text/css">
</head>
<body>
<form class="selectDocType" action="${This.path}/navigateToCreationForm" method="post" enctype="application/x-www-form-urlencoded" target="_parent">
<h3>Ajouter un Document dans ${currentDoc.title} </h3>
<#if (categories?size == 0)>
  Impossible de créer un document dans ${currentDoc.title}
<#else>

<h4>Choix du type de document à créer</h4>

<table class="wide">
  <tr>
  <#list categories as category>
    <#assign docTypesList = docTypes[category]>
      <td class="labelColumn">${category}</td>
      <#list docTypesList as docType>
        <td>
          <a class="documentType" href="${This.path}/createDocumentForm?docRef=${currentDoc.id}&doctype=${docType.id}">
            <img src="${contextPath}${docType.icon}" alt="create ${docType.id}"/>  ${docType.label}
          </a>
        </td>
      </#list>
  </tr>
  </#list>
</table>
<div class="actions">
<button class="border" name="cancel" value="Cancel" type="button" onclick="parent.jQuery.fancybox.close()">Annuler</button>
<input type="hidden" name="docRef" value="${currentDoc.id}" />
</div>
</#if>

</form>
</body>
</html>