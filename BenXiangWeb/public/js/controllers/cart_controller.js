var app = angular.module('CartApp', []);

app.filter('getFirstImage', function () {
    return function (imageStr) {
        if(imageStr){
            return imageStr.split(",")[0]
        }
        else{
            return ""
        }
    }
});

app.filter('cartCalculate', function () {
    return function (cartObj) {
        var cartItemsCount = 0
        for (x in cartObj.items) {
            cartItemsCount += cartObj.items[x].num
        }
        return cartItemsCount;
    }
});
app.filter('selectProductAmout', function () {
    return function (cartObj) {
        return getCartProductAmount(cartObj)
    }
});

function getCartProductAmount(cartObj){
    var cartProductAmout = 0
    for(x in cartObj.items){
        if(cartObj.items[x] && cartObj.items[x].select && cartObj.items[x].product)
            cartProductAmout += cartObj.items[x].num*cartObj.items[x].product.price
    }
    return cartProductAmout;
}

app.controller('CartController', [
    '$scope',
    '$http',
    function($scope, $http) {
        $scope.cart = {}
        $scope.selectAllFlag = true

        
		
		//获取用户id
		$http.get('/users/current/login').success(
				function(data, status, headers, config) {
					if (data.flag) {
						$scope.user = data.data;
						refreshData()
					} else {
					alert(data.message);
				}
			});
        
        
        
       
        //获取购物车
        function refreshData() {
        	$http.get('/cart/get').success(function (data, status, headers, config) {
                $scope.cart = JSON.parse(data.data);
                for (var i = 0; i < $scope.cart.items.length; i++) {
                    $http
                        .get('/products/' + $scope.cart.items[i].pid)
                        .success(
                        function (data, status, headers,
                                  config) {
                            if (data.flag) {
                                for (x in $scope.cart.items) {
                                    if ($scope.cart.items[x].pid == data.data.id) {
                                        $scope.cart.items[x].product = data.data
                                        $scope.cart.items[x].select = true
                                    }
                                }
                                $scope.selectAllFlag = checkSelectAll()
                            } else {
                                alert(data.message);
                            }
                        });
                }
        	 });
        }

        //添加产品数量
        $scope.setAdd = function(IndexNo) {
            $scope.cart.items[IndexNo].num ++
            updateCart2Server()
        };

        $scope.setMinus = function(IndexNo) {
            if ($scope.cart.items[IndexNo].num > 1) {
                $scope.cart.items[IndexNo].num = $scope.cart.items[IndexNo].num  - 1

                updateCart2Server()
            }
        };

        $scope.selectAll = function () {
            $scope.selectAllFlag = !$scope.selectAllFlag
            for(x in $scope.cart.items){
                $scope.cart.items[x].select = $scope.selectAllFlag
            }
            updateCart2Server()
        }

        $scope.selectLeft = function(item) {
            item.select = !item.select
            $scope.selectAllFlag = checkSelectAll()
            updateCart2Server()
        };


        $scope.leaveProducts = []
        //删除购物车产品
        $scope.deleteProcuct = function(indexNo){
            $scope.cart.items.splice(indexNo, 1)
            updateCart2Server()
        }





        $scope.Buy = function(){
            if(isEmptyCart()){
                alert('请至少选择一项产品')
                return
            }

            $http({
                method: 'PUT',
                url: '/cart/set',
                data: $scope.cart
            })
                .success(
                    function (data, status, headers,
                              config) {
                        if (data.flag) {
                            window.location.href = window.location.protocol + '//' + window.location.host + '/w/order?productAmount=' + getCartProductAmount($scope.cart)
                        } else {
                            alert(data.message)
                        }
                    });
        }

        function updateCart2Server(){
            $http({
                method: 'PUT',
                url: '/cart/set',
                data: $scope.cart
            })
                .success(
                    function (data, status, headers,
                              config) {
                        if (data.flag) {
                        	 refreshData()
                        } else {
                            alert(data.message)
                        }
                    });
        }

        function checkSelectAll(){
            var selectAll = true
            for(x in $scope.cart.items){
                if(!$scope.cart.items[x].select){
                    selectAll = false
                    break
                }
            }
            return selectAll
        }

        function isEmptyCart(){
            var empty = true
            for(x in $scope.cart.items){
                if($scope.cart.items[x].select){
                    empty = false
                    break
                }
            }
            return empty
        }
    } ]);

