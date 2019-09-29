angular.module('openapiMapApp', [])
  .controller('OpenAPIMapController', function($scope, $http) {
    $http.get('/services/namespace')
        .then(function(response){
            $scope.namespace = response.data['namespace'];
        }, function(response){
            $scope.namespace = "ERROR";
        });

    $http.get('/services/ingressed')
        .then(function(response){
            console.log(response.data);
            $scope.ingressedServices = response.data;
        });

    $http.get('/services/nonIngressed')
        .then(function(response){
            $scope.nonIngressedServices = response.data;
        });

    $scope.renderOperation = function(operation) {
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
        return className;
    }

});

/*
function getIngressedServices() {
    console.log("getting ingressed services...");
    $.getJSON({url: window.location.origin + "/services/ingressed",
        success: function(services) {
            $("#ingressedServices").html(renderRows(services));
        },
        error:  function(result) {
            $("#ingressedServices").html("<tr><td colspan=\"4\">Oh no - error getting services!</td></tr>");
        }
    });
    reloadIngressed();
}

function getNonIngressedServices() {
    console.log("getting nonIngressed services...");
    $.getJSON({url: window.location.origin + "/services/nonIngressed",
        success: function(services) {
            console.log(services);
            $("#nonIngressedServices").html(renderRows(services));
        },
        error:  function(result) {
            $("#nonIngressedServices").html("<tr><td colspan=\"4\">Oh no - error getting services!</td></tr>");
        }
    });
    reloadNonIngressed();
}

function renderRows(services) {

    var rows = "";
    var previousName = "";
    $.each(services, function(idx, service) {

        $.each(service['pathResults'], function(pathIdx, pathResult) {
            var newRow = '<tr class="text-left">';

            if (service['name'] === previousName) {
                newRow += '<td colspan="2"/>';
            } else {
                newRow += '<td class="lead">' + service['name'] + '</td>';

                newRow += '<td>'
                if (service['openapiUrl'] != null) {
                   newRow += '<a href="' + service['openapiUrl'] + '">OpenAPI</a>';
                }

                if (service['openapiUiUrl'] != null) {
                    newRow += ' | <a href="' + service['openapiUiUrl'] + '">UI</a>';
                }

                newRow += '</td>';
            }

            newRow += '<td class="lead">' + pathResult['path'] + '</td>';
            newRow += '<td>';

            $.each(pathResult['operations'], function(operationIdx, operation) {
                newRow += getButton(operation);
            });

            newRow += '</td>';
            newRow += '</tr>';
            rows += newRow;

            previousName = service['name'];
        });

    });

    return rows;
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

function reloadIngressed() {
   setTimeout(getIngressedServices, 30000);
}

function reloadNonIngressed() {
   setTimeout(getNonIngressedServices, 30000);
}

$(document).ready(function(){
    getNamespace();
    getIngressedServices();
    getNonIngressedServices();
});

*/