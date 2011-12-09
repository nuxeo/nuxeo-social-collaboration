<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<title></title>
<link href="${skinPath}/css/create_document.css" rel="stylesheet" type="text/css">
  <style type="text/css">
    .required {background:transparent url(${contextPath}/icons/required.gif) top right no-repeat; padding:0 1em 0 0; }
    .warning {background-color: #FFF7B0;}
  </style>
  <script src="${contextPath}/nxthemes-lib/jquery.js"></script>
  <script type="text/javascript">
    $(function() {
      $("#create_document").submit(function() {
        var isFormValid = true;
        $('.required').parents("tr").find(":last").each(function() {
          if (!$(this).val()) {
            $(this).addClass("warning");
            isFormValid = false;
          } else {
            $(this).removeClass("warning");
          }
        });
        return isFormValid;
      });
    });
  </script>
</head>
<body>
<form id="create_document" class="createDocument" action="${This.path}/createDocument" method="post" enctype="multipart/form-data" target="hiddenIFrame">
<h3>
${Context.getMessage("label.create.a.document")}
 ${This.getTranslatedLabel(docType.label)}
 ${Context.getMessage("label.in")}
 "${currentDoc.title}"
</h3>
<div class="center">
<table class="create">
<tr>
<td><span class="required">${Context.getMessage("label.dublincore.title")}</span></td>
<td><input class="border input" type="text" name="dc:title" /></td>
</tr>
<tr>
<td><span<#if coreType.hasSchema("social_document")> class="required"</#if>>${Context.getMessage("label.dublincore.description")}</span></td>
<td><textarea class="border input" name="dc:description" rows="2"></textarea></td>
</tr>
<#if coreType.hasSchema("note")>
<tr>
<td><span>${Context.getMessage("label.content")}</span></td>
<td><textarea class="border input" name="note:note" rows="5"></textarea></td>
</tr>
</#if>
<#if coreType.hasSchema("file")>
<tr>
<td><span>${Context.getMessage("label.content")}</span></td>
<td><input class="border input" name="file:content" type="file"></td>
</tr>
</#if>
</table>
<div class="actions">
<input class="border" type="submit" name="createDocument" value="${Context.getMessage("label.action.create")}"/>
<button class="border" name="cancel" value="Cancel" type="button" onclick="parent.jQuery.fancybox.close()">${Context.getMessage("label.action.cancel")}</button>
<input type="hidden" name="docRef" value="${currentDoc.id}" />
<input type="hidden" name="doctype" value="${docType.id}" />
</div>
</div>
</form>
<iframe onload="parent.iframeLoaded(this)" style="visibility:hidden;display:none" name="hiddenIFrame"/>
</body>
</html>
