angular.module('openapiMapApp', [])
  .controller('OpenAPIMapController', function($scope, $http) {

    $http.get('/services/namespace')
        .then(function(response){
            $scope.namespace = response.data['namespace'];
        }, function(response){
            $scope.namespace = "ERROR";
        });

    $http.get('/services')
        .then(function(response){
            console.log(response.data);
            $scope.ingressedServices = response.data;
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