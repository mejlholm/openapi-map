function getServices() {
    console.log("getting services...");
    $.getJSON({url: window.location.origin + "/services",
        success: function(services) {

            var rows = "";
            $.each(services, function(idx, service) {

                var previousName = "";
                $.each(service['pathResults'], function(pathIdx, pathResult) {

                    if (service['name'] === previousName) {
                        var newRow = '<tr><td colspan="3"/>';
                    } else {
                        var newRow = '<tr><td class="text-left lead">' + service['name'] + '</td>';
                        newRow += '<td class="text-left"><a href="' + service['openapiUrl'] + '">Link</a></td>';
                        newRow += '<td class="text-left"><a href="' + service['openapiUiUrl'] + '">Link</a></td>';
                    }
                    newRow += '<td class="text-left lead">' + pathResult['path'] + '</td><td class="text-left">';

                    $.each(pathResult['operations'], function(operationIdx, operation) {
                        newRow += getButton(operation);
                    })

                    rows += '</td></tr>' + newRow;
                    previousName = service['name'];
                })
            });

            $("#services").html(rows);

        },
        error:  function(result) {
            $("#services").html("<tr><td colspan=\"5\">Oh no - error getting services!</td></tr>");
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

    $.getJSON({url: window.location.origin + "/services/namespace",
        success: function(data) {
            $("#namespace").html('Namespace: ' + data['namespace']);
        },
        error:  function(result) {
            $("#namespace").html('ERROR');
        }
    });
});