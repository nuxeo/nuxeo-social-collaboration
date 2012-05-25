<tr><td colspan="4" style="border:0; padding-left:15px">
<table style="width: 100%;">
<tr>
  <td style="border-style:none;width:30%">
    ${comment.comment.author}
  </td>
  <td style="border-style:none;width:30%">
    ${comment.comment.text}
  </td>
  <td style="border-style:none;width:40%">
    <a href="#" id="comment_button_${comment.id}">Answer</a>
  </td>
</tr>
<tr class="${comment.id}">
  <td style="border-style:none;"/>
  <td style="border-style:none;"/>
  <td style="border-style:none;">
    <div id="box_${comment.id}" style="display:none">
        <textarea id="commentContent_${comment.id}" ></textarea><br/>
        <input type="button" value="Comment" onclick="Library.addComment('${doc.id}','#commentContent_${comment.id}','${comment.id}')"/>
    </div>
  </td>
</tr>
<script type="text/javascript">
$(document).ready(function () {
    // 'Answer' display handler
    $("#comment_button_"+'${comment.id}').click(function () {
        Library.handleLinkClick("#box_",'${comment.id}');
    });
});
</script>
<#assign commentChildren = This.getCommentChildren(doc,comment)>
<#if commentChildren??>
  <#list commentChildren as comment>
    <#include "@bricks/document_comments">
  </#list>
</#if>
</table>
</td>
</tr>
