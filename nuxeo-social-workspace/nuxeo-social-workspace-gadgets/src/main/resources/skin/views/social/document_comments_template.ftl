<tr>
  <td colspan="4">
    <#assign comments = This.getComments(doc)>
    <a href="#" id="${doc.id}" class="comment_display_button">Show ${comments?size} threads</a>
    <div id="${doc.id}_display" style="display:none">
      <table class="dataList">
        <#list comments as comment>
          <#include "@bricks/document_comments">
        </#list>
      </table>
    </div>
  </td>
</tr>
<script type="text/javascript">
    $(document).ready(function()
      {
        // Activate comment box toggles
        // Answer
        $(".comment_button").click(function(){
          var element = $(this);
          var id = element.attr("id");
          $("#box_"+id).slideToggle(300);
          $("#box_"+id).style.display = true;
          return false;
        });
        // Display
        $(".comment_display_button").click(function(){
          var element = $(this);
          var id = element.attr("id");
          $("#"+id+"_display").slideToggle(300);
          $("#"+id+"_display").style.display = true;
          return false;
      });});
</script>