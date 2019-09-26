function getServices() {
    console.log("getting services...");
    $.getJSON({url: window.location.origin + "/service",
        success: function(services) {
            var rows = "";
            $.each(services, function(idx, service) {
                $.each(service['pathResults'], function(pathIdx, pathResult) {
                    var newRow = '<tr><td>' + service['name'] + '</td><td><a href="' + service['openapiUrl'] + '">Link</a></td>';
                    newRow += '<td>' + pathResult['path'] + '</td><td>';
                    $.each(pathResult['operations'], function(operationIdx, operation) {
                        newRow += getButton(operation);
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

//map operations to colors
function getButton(operation) {
    var className = "btn-secondary"; //default color for the more exotic operations

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