var app = angular.module('myOrderApp', []);

app.controller('myOrderController', ['$scope', '$http',

//绑定id页	
    function ($scope, $http) {
        $scope.orders = [];
        $scope.user = {};

        //获取用户id
        $http.get('/users/current/login').success(
            function (data, status, headers, config) {
                if (data.flag) {
                    $scope.user = data.data;
                    if (GetQueryString('yicorder')) {
                        $http.get('/orders/user/' + $scope.user.id + '?status=0').success(
                            function (data, status, headers, config) {
                                if (data.flag) {
                                    $scope.orders = data.data
                                } else {
                                    $scope.orders = []
                                    alert("没有相关的订单");
                                }
                            });
                    } else {
                        refreshData()
                    }
                } else {
                    alert(data.message);
                }
            });

        function refreshData() {
            $http.get('/orders/user/' + $scope.user.id).success(
                function (data, status, headers, config) {
                    if (data.flag) {
                        $scope.orders = data.data
                        document.getElementById("dh3").className = "actie"

                    } else {
                        alert("没有相关的订单");
                    }
                });
        }

        $scope.yicorder = function (pid) {
            $http.get('/orders/user/' + $scope.user.id + '?status=' + pid).success(
                function (data, status, headers, config) {
                    if (data.flag) {
                        $scope.orders = data.data
                    } else {
                        $scope.orders = []
                        alert("没有相关的订单");
                    }
                });
        }

        $scope.Confirm = function (indexNo) {
            /////////确认收货改变订单状态为交易成功/////////
            $scope.orders[indexNo].status = 5
            $http({
                method: 'PUT',
                /*     url: '/orders/' + $scope.myOrders[indexNo].id + '/status/5 ',*/
                url: ' /orders/userupdate/' + $scope.orders[indexNo].id + '/status/5',
                data: $scope.orders[indexNo]
            }).success(function (data, status, headers, config) {
                if (data.flag) {
                    // $scope.productList.splice(indexNo, 1)
                } else {
                    alert(data.message);
                }
            });
        }

        //取消订单
        $scope.cancelorder = function (pid) {
            /*$scope.index = indexNo*/

            $http.get('/orders/' + pid).success(
                function (data, status, headers, config) {
                    if (data.flag) {
                        $scope.order = data.data
                        $scope.order.status = 2
                        $http({
                            method: 'PUT',
                            url: '/orders/' + pid + '/status/2',
                            data: $scope.order
                        }).success(function (data, status, headers, config) {
                            if (data.flag) {
                                refreshData()
                                // $scope.productList.splice(indexNo, 1)
                            } else {
                                alert(data.message);
                            }
                        });
                    } else {
                        alert(data.message);
                    }
                });




        };

        //立即购买
        $scope.Buy = function (orderId) {
            window.location.href = window.location.protocol + '//' + window.location.host + '/wxpay/pay?oid=' + orderId
        }


    }]);


function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}


window.onload = function () {

    $('.myOrder_top_ul li').click(function () {
        $('.myOrder_top_ul li').removeClass('actie');
        $(this).addClass('actie');
    })


    var url = window.location.search;
    if (url == "?yicorder=0") {
        document.getElementById("dh4").className = "actie"
    }
}




