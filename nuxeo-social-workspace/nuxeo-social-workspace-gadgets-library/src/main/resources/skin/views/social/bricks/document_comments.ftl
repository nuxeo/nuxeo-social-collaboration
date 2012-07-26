<tr>
  <td colspan="4" class="messageRow">
    <table class="messageBlock">
      <tr>
        <td class="messageBody">
          <div class="messageUserInfos">
            <span class="avatar"><img src="${This.getAvatarURL(comment.comment.author)}"/></span>
            <span class="username">${comment.comment.author}</span>
            <span class="timestamp">${comment.comment.creationDate.getInstance().getTime()?string("dd/MM/yyyy")}</span>
          </div>
          <div class="messageContent">
            <span class="message comment">${comment.comment.text}</span>
          </div>
        </td>
        <td class="messageReply">
          <a href="#" class="actionItem" id="comment_button_${comment.id}"><img src="${contextPath}/icons/reply.png" />Reply</a>
        </td>
        </tr>
        <tr class="${comment.id}">
        <td colspan="3">
          <div id="box_${comment.id}" class="replyMessageForm" style="display:none">
              <textarea id="commentContent_${comment.id}" ></textarea>
              <input class="button" type="button" value="Comment" onclick="Library.addComment('${doc.id}','#commentContent_${comment.id}','${comment.id}')"/>
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
    </table>
  </td>
</tr>
<#assign commentChildren = This.getCommentChildren(doc,comment)>
<#if commentChildren??>
  <#list commentChildren as comment>
    <#include "@bricks/document_comments">
  </#list>
</#if>
