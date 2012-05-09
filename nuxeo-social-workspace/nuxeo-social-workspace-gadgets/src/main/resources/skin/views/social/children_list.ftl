<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" class="no-js">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<title>Library Gadget</title>

<link rel="stylesheet" type="text/css" href="${skinPath}/css/library.css"/>

<script type="text/javascript" src="${skinPath}/script/library.js"></script>
<script type="text/javascript" src="${skinPath}/script/context-management.js"></script>
<script type="text/javascript" src="${skinPath}/script/jquery/jquery-1.5.2.min.js"></script>
<script type="text/javascript" src="${skinPath}/script/jquery/jquery.fancybox.pack.js"></script>

</head>
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
</html>