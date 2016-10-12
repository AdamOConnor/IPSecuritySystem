var $image = $("img").first();
var $downloadingImage = $("<img>");
$downloadingImage.load(function(){
  $image.attr("src", $(this).attr("src"));	
});
$downloadingImage.attr("src", "http://an.image/to/aynchrounously/download.jpg");