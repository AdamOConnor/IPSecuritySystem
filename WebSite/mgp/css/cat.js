$(document).ready(function() {
function getRandomSize(min, max) {
  return Math.round(Math.random() * (max - min) + min);
}
  for (var i = 0; i < 25; i++) {
    var width = getRandomSize(200, 400);
    var height =  getRandomSize(200, 400);
    $('#photos').append('<img src="http://www.lorempixel.com/'+width+'/'+height+'/cats" alt="pretty kitty">');
  }
});

<?php
$dirname = "";
$images = glob($dirname."*.jpg");
foreach($images as $image) {
echo '<img src="'.$image.'" /><br />';
}
?>