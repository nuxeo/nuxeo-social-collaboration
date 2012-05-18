<#list This.getComments(doc) as comment>
  <#include "@bricks/document_comments">
</#list>
<script type="text/javascript">
    $(document).ready(function()
      {
        //Activate comment box toggle effect
        $(".comment_button").click(function(){
        var element = $(this);
        var id = element.attr("id");
        $("#slide"+id).slideToggle(300);
        $("#slide"+id).style.display = true;
        return false;
      });});
</script>