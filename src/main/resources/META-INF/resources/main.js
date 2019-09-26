function getServices() {
    console.log("getting services...");
    $.getJSON({url: window.location.origin + "/service",
        success: function(result) {
            var rows = "";
            $.each(result, function(idx) {
                $.each(result[idx]['pathResults'], function(pathIdx) {
                    var newRow = '<tr><td>' + result[idx]['name'] + '</td><td><a href="' + result[idx]['openapiUrl'] + '">Link</a></td>';
                    newRow += '<td>' + result[idx]['pathResults'][pathIdx]['path'] + '</td><td>';
                    $.each(result[idx]['pathResults'][pathIdx]['operations'], function(operationIdx) {
                        newRow += getButton(result[idx]['pathResults'][pathIdx]['operations'][operationIdx]);
                    })
                    rows += '</td></tr>' + newRow;
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

function getButton(operation) {
    var className = "btn-secondary";
    if (operation === 'GET'){
        className = "btn-primary";
    } else if (operation === 'DELETE') {
        className = 'btn-danger';
    } else if (operation === 'POST') {
        className = 'btn-success';
    } else if (operation === 'PUT') {
        className = 'btn-warning';
    }

    return '<button type="button" class="btn ' + className + ' mr-1" aria-disabled="true" disabled>' + operation + '</button>';
}

function reload() {
   setTimeout(getServices, 30000);
}

$(document).ready(function(){
    getServices();
});