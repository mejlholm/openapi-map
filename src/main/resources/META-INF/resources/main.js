function getServices() {
    console.log("getting services...");
    $.getJSON({url: window.location.origin + "/service",
        success: function(result) {
            var rows = "";
            $.each(result, function(idx) {
                $.each(result[idx]['pathResults'], function(pathIdx) {
                    var newRow = '<tr><td>' + result[idx]['name'] + '</td><td><a href="' + result[idx]['openapiUrl'] + '">Link</a></td>';
                    newRow += '<td>' + result[idx]['pathResults'][pathIdx]['path'] + '</td><td>' + result[idx]['pathResults'][pathIdx]['operations'] + '</td></tr>';
                    rows += newRow;
                })
            });
            $("#services").html(rows);
        },
        error:  function(result) {
            $("#services").html("<tr><td colspan=\"2\">Oh no - error getting services!</td></tr>");
        }
    });
    reload();
}

function reload() {
   setTimeout(getServices, 30000);
}

$(document).ready(function(){
    getServices();
});