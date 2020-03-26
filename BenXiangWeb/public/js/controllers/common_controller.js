var app = angular.module('commonApp', []);

app.filter('cartCalculate', function () {
    return function (cartObj) {
        var cartItemsCount = 0
        for (x in cartObj.items) {
            cartItemsCount += cartObj.items[x].num
        }
        return cartItemsCount;
    }
});


app.controller('commonController', ['$scope', '$http', '$log', function ($scope, $http, $log) {

	   
	//获取购物车
	  
	        $http.get('/cart/get').success(function (data, status, headers, config) {
	            $scope.cart = JSON.parse(data.data);
	        });
}]);


angular.element(document).ready(function() {
    angular.bootstrap(document.getElementById("A2"), [ "commonApp" ]);
});