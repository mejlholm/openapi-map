angular.module('openapiMapApp', [])
  .controller('OpenAPIMapController', function($scope, $http) {

    $http.get('/services/namespace')
        .then(function(response){
            $scope.namespace = response.data['namespace'];
        }, function(){
            $scope.namespace = "ERROR";
        });

    $http.get('/services')
        .then(function(response){
            $scope.services = response.data;
        });

    $scope.renderMethod = function(method) {
        let className = "btn-secondary"; //default color for the more exotic methods

        if (method === 'GET'){
            className = "btn-primary";
        } else if (method === 'DELETE') {
            className = 'btn-danger';
        } else if (method === 'POST') {
            className = 'btn-success';
        } else if (method === 'PUT') {
            className = 'btn-warning';
        }
        return className;
    }

});