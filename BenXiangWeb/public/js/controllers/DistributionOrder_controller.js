var app = angular.module('MyResellerOrderApp', []);


app.filter('stateDisplay', function () {
    return function (state) {
        if (state === 1){
            return "已支付"
        }
        if (state === 5){
            return "已收货"
        }
        if (state === 6){
            return "已计算分销"
        }
        if (state === 7){
            return "已取消分销"
        }

    }
});
    app.controller('MyResellerOrderController', ['$log', '$scope', '$http', function($log, $scope, $http) {

    $scope.myOrders = [] 
	$scope.images = [] 
	$scope.user = {}
	$scope.resellerAmount = 0
	
    $http.get('/users/current/login').success(function(data, status, headers, config) {
        if (data.flag) {
            $scope.user = data.data;

            $http.get('/resellerorders/users/' + $scope.user.id).success(function(data, status, headers, config) {
                if (data.flag) {
                    $scope.myOrders = data.data;
                    for (var i = 0, len = $scope.myOrders.length; i < len; i++) {
                        for (var j = 0; j < $scope.myOrders[i].orderProducts.length; j++) {
                            $scope.myOrders[i].orderProducts[j].ProductQuantity = $scope.myOrders[i].quantity.split(",")[j]
                        }
                    }
                  /*  for (var i = 0,
                    len = $scope.myOrders.length; i < len; i++) {
                    	if ($scope.myOrders[i].orderProducts.length > 0) {
	                        if ($scope.myOrders[i].orderProducts[0].images) {
	                            $scope.images.push($scope.myOrders[i].orderProducts[0].images.split(",", 1)[0]);
	                        }
                    	}
                    }*/

                    $http.get('/resellerorders/amount/users/' + $scope.user.id).success(function(data, status, headers, config) {
                        if (data.flag) {
                            $scope.resellerAmount = data.data;
                        }
						else{
                            $scope.resellerAmount = 0;
						}
                    });
                }
            });

        } else {
            alert(data.message);
        }
    });
}]);