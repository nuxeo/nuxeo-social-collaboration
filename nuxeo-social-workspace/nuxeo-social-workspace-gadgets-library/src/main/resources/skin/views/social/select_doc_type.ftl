<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<title></title>
<link href="${skinPath}/css/select_doc_type.css" rel="stylesheet" type="text/css">
</head>
<body>
<form class="selectDocType" action="${This.path}/navigateToCreationForm" method="post" enctype="application/x-www-form-urlencoded" target="_parent">
<h3>
${Context.getMessage("label.create.a.document")}
 ${Context.getMessage("label.in")}
 "${currentDoc.title}"
</h3>

<#if (categories?size == 0)>
  ${Context.getMessage("label.document.creation.not.possible")} ${currentDoc.title}
<#else>

<h4>${Context.getMessage("label.select.document.type")}</h4>

<table class="wide">
  <tr>
  <#list categories as category>
    <#assign docTypesList = docTypes[category]>
      <td class="labelColumn">${This.getTranslatedLabel(category)}</td>
      <#list docTypesList as docType>
        <td>
          <a class="documentType" href="${This.path}/createDocumentForm?docRef=${currentDoc.id}&doctype=${docType.id}&lang=${lang}">
            <img src="${contextPath}${docType.icon}" alt="create ${docType.id}"/>${This.getTranslatedLabel(docType.label)}
          </a>
        </td>
      </#list>
  </tr>
  </#list>
</table>
<div class="actions">
<button class="border" name="cancel" value="Cancel" type="button" onclick="parent.jQuery.fancybox.close()">${Context.getMessage("label.action.cancel")}</button>
<input type="hidden" name="docRef" value="${currentDoc.id}" />
</div>
</#if>

</form>
</body>
</html>