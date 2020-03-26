var app = angular.module('UserApp', []);


app.controller('UserController', [
    '$scope',
    '$http',
    function ($scope, $http) {
        $scope.user = {}
        $http.get('/users/current/login').success(
            function (data, status, headers, config) {
                if (data.flag) {
                    $scope.user = data.data;
                } else {
                    alert(data.message);
                }
            });

        $scope.CustomerService = function () {
            alert('本平台当前暂不支持在线自主退款功能，如需退款请联系客服 : 4008-223-208');
        }

    }]);
