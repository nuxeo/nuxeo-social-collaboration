  <tr>
  <td colspan="5" class="actions">
    <#assign comments = This.getComments(doc)>
    <a class="actionItem" href="#" id="comment_display_button_${doc.id}">Show ${comments?size} threads</a>
    <a class="actionItem" href="#" id="root_comment_button_${doc.id}"><img src="${contextPath}/icons/comment.png" />Comment</a>
    <a class="actionItem" href="#" id="like_${doc.id}" onclick="Library.docLike('${doc.id}');" class="like_link">
    <#if This.hasUserLiked(doc)>
      <img src="${contextPath}/icons/unlike.png" />
      ${This.getLikesCount(doc)}
    <#else>
      <img src="${contextPath}/icons/like.png" />
      ${This.getLikesCount(doc)}
    </#if>
    </a>
    <div class="newMessageForm" id="box_comment_${doc.id}" style="display:none">
        <textarea id="rootCommentContent_${doc.id}"></textarea>
        <input class="button" type="button" value="Comment" onclick="Library.addComment('${doc.id}','#rootCommentContent_${doc.id}')"/>
    </div>
    <div id="display_${doc.id}" style="display:none">
      <table id="comments_list_${doc.id}" class="documentThread">
        <#list comments as comment>
          <#include "@bricks/document_comments">
        </#list>
      </table>
    </div>
  </td>
</tr>
<script type="text/javascript">
$(document).ready(function () {
    // Init comment display link
    if(${comments?size}==0){
        $("#comment_display_button_"+'${doc.id}').hide();
    }
    // Activate comment box toggles
    // All comments display handler
    $("#comment_display_button_"+'${doc.id}').click(function () {
        Library.commentsDisplayHandler($(this),'${doc.id}');
        Library.handleLinkClick("#display_",'${doc.id}');
    });
    // 'Comment' display handler
    $("#root_comment_button_"+'${doc.id}').click(function () {
        Library.handleLinkClick("#box_comment_",'${doc.id}');
    });
});
</script>