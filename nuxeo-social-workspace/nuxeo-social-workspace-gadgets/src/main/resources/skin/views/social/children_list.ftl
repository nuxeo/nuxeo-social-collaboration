<#if (renderFullHtml)>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" class="no-js">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

  <title>Library Gadget</title>

  <link rel="stylesheet" type="text/css" href="${contextPath}/nxthemes-lib/gadget-common.css,jquery.fancybox.style.css,gadget-library.css?path=${contextPath}&basepath=${contextPath}"/>

  <script src="${contextPath}/nxthemes-lib/jquery.js,jquery.fancybox.js,gadget-library.js"></script>

  <script type="text/javascript">
    jQuery(document).ready(function() {
      jQuery(".addPopup").each(function() {
        Library.addPopupBoxTo($(this));
      });
    });
  </script>
</head>
</#if>

<div id="content">
<div class="header">
  <#include "@bricks/toolbar">
  <#include "@bricks/search_box">
  <div class="clear"></div>
</div>
<div>
    <#include "@bricks/breadcrumb">
</div>
<#include "@bricks/document_list">
</div>

<#if (renderFullHtml)>
</html>
</#if>
