var app = angular.module('PayApp', ['angularFileUpload']);

app.filter('safehtml', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.controller('PayController', [
    '$log',
    '$scope',
    '$upload',
    '$http',
    '$filter',
    function ($log, $scope, $upload, $http, $filter) {
        $scope.newOrder = {
            'orderNo': '',
            'refBuyerId': 0,           // 用户id
            'buyer': {},
            'refResellerId': 0,       // 分销用户id
            'reseller': {},           // 分销用户
            'quantity': GetQueryString('num'),
            'shipFee': 0,
            'price': GetQueryString('price') * 1,
            'amount': 0,
            'shipName': '',
            'shipPhone': '',
            'shipPostCode': '',
            'shipProvice': '',
            'shipLocation': '',
            'productAmount': GetQueryString('productAmount') * 1,
            'themeName': GetQueryString('kouwei'),
            'orderProducts': [],
            'liuYan': '',
            'jifen': 0,
            'promotionAmount': 0,
            'jifenAmount': 0,
        }

        if (GetQueryString('wineWeight')) {
            $scope.newOrder.wineWeight = GetQueryString('wineWeight')
        }

        $scope.LocationIndex = GetQueryString('LocationId')
        $scope.needInvoice = false
        $scope.myLocations = []
        $scope.defaultLocation = null
        $scope.shipInfoList = []
        $scope.shipInfo = {}
        $scope.user = {}
        $scope.orderProduct = {}
        $scope.pid = GetQueryString('pid')

        //取购物车商品
        $scope.cart = {}

        $scope.dt1 = new Date();
        $scope.dt1.getHours();
        $scope.dt2 = $filter("date")($scope.dt1, "HH");


        $http.get('/cart/get').success(function (data, status, headers, config) {

            // 直接下单, 不需要同步session购物车
            if (GetQueryString('num') != null && GetQueryString('num') != '') {
                $scope.cart.items = [{"pid": GetQueryString('pid'), "num": GetQueryString('num'), "select": true}]
            } else {
                // 服务端session同步下来, 购物车购物时候使用
                $scope.cart = JSON.parse(data.data);
            }

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
                                    }
                                }
                            } else {
                                alert(data.message);
                            }
                        });
            }
        });

        // 取促销
        $http.get('/promotions').success(function (data, status, headers, config) {
            if (data.flag) {
                $scope.promotions = data.data;

                // 计算促销, 调整订单价格
                $scope.newOrder.amount = $scope.newOrder.productAmount + calculatePromotion($scope.promotions)

                // 取用户
                $http.get('/users/current/login')
                    .success(
                        function (data, status, headers,
                                  config) {
                            if (data.flag) {
                                $scope.user = data.data;

                                // 取送货地址
                                $http.get('/users/' + $scope.user.id + '/shipinfos').success(
                                    function (data, status, headers, config) {
                                        if (data.flag) {
                                            $scope.myLocations = data.data;

                                            if ($scope.LocationIndex) {
                                                $scope.defaultLocation = $scope.myLocations[$scope.LocationIndex]
                                            }
                                            else {
                                                $scope.defaultLocation = $scope.myLocations[0]

                                                for (var i = 0; i < $scope.myLocations.length; i++) {
                                                    if ($scope.myLocations[i].isDefault) {
                                                        $scope.defaultLocation = $scope.myLocations[i]
                                                        break
                                                    }
                                                }
                                            }

                                            $scope.newOrder.shipName = $scope.defaultLocation.name
                                            $scope.newOrder.shipPhone = $scope.defaultLocation.phone
                                            $scope.newOrder.shipPostCode = $scope.defaultLocation.postCode
                                            $scope.newOrder.shipProvice = $scope.defaultLocation.provice
                                            $scope.newOrder.shipLocation = $scope.defaultLocation.location

                                            // 取运费
                                            refreshDataShipPrice()
                                        } else {
                                            $scope.myLocations = null
                                            /* alert(data.message); */
                                        }
                                    });

                            } else {
                                alert(data.message);
                            }
                        });
            } else {
//  			  alert(data.message); 
            }
        });

        function calculatePromotion(promList) {
            var discount = 0
            for (x in promList) {
                if (promList[x].available) {//TODO: 以后要计算复杂促销
                    discount -= promList[x].discount
                }
            }
            return discount
        }

        function refreshDataShipPrice() {
            $http
                .get('/shipareaprices/' + $scope.defaultLocation.provice)
                .success(
                    function (data, status, headers,
                              config) {
                        if (data.flag) {

                            $scope.shipInfo = data.data;
                            $scope.newOrder.shipFee = $scope.shipInfo.shipPrice

                            // 计算运费, 调整订单总价
                            $scope.newOrder.amount += $scope.newOrder.shipFee

//                                     $http.get('/users/' + GetQueryString('userId')).success(
//                                             function (data, status, headers, config) {
//                                                 if (data.flag) {
//                                                     $scope.user = data.data;                                                      
//                                                     $scope.newOrder.amount = $scope.newOrder.productAmount + $scope.newOrder.shipFee
//                                                     $scope.newOrder.buyer = $scope.user
//                                                 } else {
//                                                     /* alert(data.message); */
//                                                 }
//                                             });     
                        }
                    });
        }

        function getOrderQuantity(cartObj) {
            var quantityStr = ""
            for (x in cartObj.items) {
                if (cartObj.items[x].select) {
                    quantityStr += cartObj.items[x].num + ","
                }
            }
            return quantityStr.substring(0, quantityStr.length - 1)
        }

        function getOrderProducts(cartObj) {
            var products = []
            for (x in cartObj.items) {
                if (cartObj.items[x].select) {
                    products.push(cartObj.items[x].product)
                }
            }
            return products
        }

        function verifyOrderAmount() {
            // 计算商品总额
            var productAmount = 0
            for (x in $scope.cart.items) {
                if ($scope.cart.items[x].select) {
                    productAmount += $scope.cart.items[x].product.price * $scope.cart.items[x].num
                }
            }

            $scope.newOrder.amount = productAmount + calculatePromotion($scope.promotions) + $scope.newOrder.shipFee
            $scope.newOrder.promotionAmount = calculatePromotion($scope.promotions)
        }

        //清除购物车中已购买的商品
        function deleteCartProcuct(orderId) {
            $http({
                method: 'PUT',
                url: '/cart/set',
                data: $scope.cart
            })
                .success(
                    function (data, status, headers, config) {
                        if (data.flag) {

                            //下单成功跳转页面
                            window.location.href = window.location.protocol + '//' + window.location.host + '/wxpay/pay?oid=' + orderId
                        } else {
                            alert(data.message)
                        }
                    });
        }

        $scope.addOrder = function () {
            // 未登录不允许提交订单
            if (!$scope.user) {
                alert('用户未登录')
                return
            }
            else {
                $scope.newOrder.buyer = $scope.user
                $scope.newOrder.refBuyerId = $scope.user.id
            }

            /*
             * if ($scope.needInvoice) { $log.log($scope.invoiceType) if
             * ($scope.invoiceType === '个人') { $scope.newOrder.invoiceTitle =
             * '个人' } else { if (!$scope.newOrder.invoiceTitle) {
             * alert('请输入发票的单位抬头'); } $log.log($scope.newOrder.invoiceTitle) } }
             */

            $scope.newOrder.quantity = getOrderQuantity($scope.cart)
            $scope.newOrder.orderProducts = getOrderProducts($scope.cart)

            if ($scope.newOrder.orderProducts == null) {
                alert('产品未获取,请重试')
                return
            }

            if ($scope.newOrder.productAmount < 20) {
                alert('菜品满￥20起送')
                return
            }
            if ($scope.dt2 > 3 && $scope.dt2 < 11) {
                alert('送餐时间为早上11:00 - 凌晨 3:00')
                return
            }

//            if (!$scope.newOrder.price || $scope.newOrder.price <= 0) {
//                alert('订单总额有误,请重新下单')
//                return
//            }

            // 用户若有上线, 设置这个订单为分销订单
            if ($scope.user.refUplineUserId) {
                if ($scope.user.refUplineUserId > 0) {
                    $scope.newOrder.refResellerId = $scope.user.refUplineUserId
                }
            }

            // 重算订单额
            verifyOrderAmount()
            $log.log($scope.newOrder)
            $http({
                method: 'POST',
                url: '/orders',
                data: $scope.newOrder
            }).success(function (data, status, headers, config) {

                if (data.flag) {
                    //清除购物车中已购买的商品
                    if (!GetQueryString('num')) {
                        for (var i = 0; i < $scope.cart.items.length; i++) {
                            if ($scope.cart.items[i].select == true) {
                                $scope.cart.items.splice(i, 1)
                                i--
                            }
                        }
                        if (!$scope.cart.items) $scope.cart.items = []
                        deleteCartProcuct(data.data.id)
                    }else{
                        window.location.href = window.location.protocol + '//' + window.location.host + '/wxpay/pay?oid=' + data.data.id
                    }
                } else {
                    alert(data.message)
                }
            });
        };

        $scope.jinggao = function (obj) {
            if (obj === null) {
                alert('请填写地址')
            }
        }


        $scope.Use = false
        $scope.UseImage = '19.png'
        $scope.UseCoupons = function () {
            if ($scope.Use === false) {
                $scope.UseImage = '20.png'
                $scope.Use = true


                if ($scope.defaultLocation === null) {


                    $http.get('/users/' + $scope.user.id).success(
                        function (data, status, headers, config) {
                            if (data.flag) {
                                $scope.user = data.data;
                                $scope.newOrder.jifen = $scope.user.jifen
                                $scope.newOrder.jifenAmount = $scope.newOrder.jifen / 100
                                $scope.newOrder.amount = $scope.newOrder.productAmount - $scope.user.jifen / 100
                                $scope.newOrder.buyer = $scope.user
                            } else {
                                /* alert(data.message); */
                            }
                        });

                }
                $http
                    .get('/shipareaprices/' + $scope.defaultLocation.provice)
                    .success(
                        function (data, status, headers,
                                  config) {
                            if (data.flag) {

                                $scope.shipInfo = data.data;
                                $scope.newOrder.shipFee = $scope.shipInfo.shipPrice

                                $http.get('/users/' + $scope.user.id).success(
                                    function (data, status, headers, config) {
                                        if (data.flag) {
                                            $scope.user = data.data;
                                            $scope.newOrder.jifen = $scope.user.jifen
                                            $scope.newOrder.jifenAmount = $scope.newOrder.jifen / 100
                                            $scope.newOrder.amount = $scope.newOrder.productAmount - $scope.user.jifen / 100 + $scope.newOrder.shipFee
                                            $scope.newOrder.buyer = $scope.user
                                        } else {
                                            /* alert(data.message); */
                                        }
                                    });
                            }
                        });


            }
            else {
                if ($scope.Use === true) {
                    $scope.UseImage = '19.png'
                    $scope.Use = false
                    $scope.newOrder.jifen = 0
                    if ($scope.defaultLocation === null) {
                        $scope.newOrder.amount = $scope.newOrder.productAmount

                    }

                    refreshDataShipPrice()


                }
            }
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
