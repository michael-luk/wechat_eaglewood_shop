var app = angular.module('OrderApp', []);

app.filter('safehtml', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

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

app.controller('OrderController', [
    '$log',
    '$scope',
    '$http',
    function ($log, $scope, $http) {
        $scope.newOrder = {
            'orderNo': '',
            'refBuyerId': 0,           // 用户id
            'buyer': {},
            'refResellerId': 0,       // 分销用户id
            'reseller': {},           // 分销用户
            'quantity': GetQueryString('num'),
            'shipFee': 0,
            'price': GetQueryString('price')*1,
            'amount': 0,
            'shipName': '',
            'shipPhone': '',
            'shipPostCode': '',
            'shipProvice': '',
            'shipLocation': '',
            'productAmount': GetQueryString('productAmount')*1,
            'orderProducts': [],
            'jifen':0,
            'promotionAmount': 0,
            'jifenAmount': 0,
        }

        $scope.Shipinfo = {
            'name' : '',
            'phone' : '',
            'provice' : '',
            'location' : '',
            'postCode' : '',
            'isDefault' : true,
        }

        if (GetQueryString('wineWeight')) {
            $scope.newOrder.wineWeight = GetQueryString('wineWeight')
        }

        $scope.LocationIndex = GetQueryString('LocationIndex')
        $scope.needInvoice = false
        $scope.myLocations = []
        $scope.defaultLocation = null
        $scope.shipInfoList = []
        $scope.shipInfo = {}
        $scope.user = {}
        $scope.enjoyTheCode = {}
        $scope.orderProduct = {}
        $scope.pid = GetQueryString('pid')
        $scope.jifen = 0
        $scope.wxpay = true
        $scope.alipay = false


        //取购物车商品
        $scope.cart = {}

        $http.get('/cart/get').success(function (data, status, headers, config) {

            // 直接下单, 不需要同步session购物车
            if( GetQueryString('num') != null){
                $scope.cart.items = [{"pid":GetQueryString('pid'), "num":GetQueryString('num'), "select": true}]
            }else{
                // 服务端session同步下来, 购物车购物时候使用
                $scope.cart = JSON.parse(data.data);
            }

            for(var i = 0; i < $scope.cart.items.length; i++){
                $http
                    .get('/products/' +$scope.cart.items[i].pid )
                    .success(
                        function(data, status, headers,
                                 config) {
                            if (data.flag) {

                                for(x in $scope.cart.items){
                                    if($scope.cart.items[x].pid == data.data.id){
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
        $http.get('/promotions').success( function (data, status, headers, config) {
            if (data.flag) {
                $scope.promotions = data.data;

                // 计算促销, 调整订单价格
                $scope.newOrder.amount = $scope.newOrder.productAmount + calculatePromotion($scope.promotions)

            } else {
                $scope.newOrder.amount = $scope.newOrder.productAmount
//  			  alert(data.message);
            }
            // 取用户

            $http.get('/users/current/login').success(
                    function(data, status, headers, config) {
                        if (data.flag) {
                            $scope.user = data.data;
                            $scope.jifen = $scope.user.jifen

                            // 取送货地址

                            $http.get('/users/'+$scope.user.id+'/shipinfos').success(
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
                                        $scope.myLocations = []


                                    }
                                });
                            //取尊享码
                            $http
                                .get('/enjoyTheCode?refUserId='+$scope.user.id)
                                .success(
                                    function (data, status, headers,
                                              config) {
                                        if (data.flag) {
                                            $scope.enjoyTheCode = data.data[0];
                                            if ($scope.enjoyTheCode.number > 0) {
                                                $scope.Code = $scope.enjoyTheCode.number
                                            }else{
                                                $scope.Code = ''
                                            }
                                        }else{
                                            $scope.enjoyTheCode = []
                                        }
                                    });


                        } else {
                            $scope.user = null
                        }
                    });
        });


        // 添加地址
        $scope.addLocation = function() {
            $scope.defaultLocation =  $scope.Shipinfo
            if($scope.user){
            $http({
                method : 'POST',
                url :'/users/'+$scope.user.id+'/shipinfos',
                data : $scope.Shipinfo
            }).success(function(data, status, headers, config) {
                if (data.flag) {
                    window.location.href = window.location.protocol + '//' + window.location.host + '/w/order' + window.location.search
                }
            });
            }else{
                    alert("用户未登录")
                    window.location.href = window.location.protocol + '//' + window.location.host + '/w/order' + window.location.search
            }
        };


        //更改默认地址状态
        $scope.ond = function () {
            $scope.Shipinfo.isDefault = true
        }
        $scope.offd = function () {
            $scope.Shipinfo.isDefault = false
        }

        function calculatePromotion(promList){
            var discount = 0
            for(x in promList){
                if(promList[x].available){//TODO: 以后要计算复杂促销
                    discount -= promList[x].discount
                }
            }
            return discount
        }

       /* function refreshDataShipPrice() {
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

                        }
                    });
        }*/

        function refreshDataShipPrice() {
            $http
                .get('/shipareaprices')
                .success(
                    function (data, status, headers,
                              config) {
                        if (data.flag) {

                            $scope.shipInfo = data.data[0];
                            if($scope.newOrder.productAmount < $scope.shipInfo.area * 1 && $scope.Since == false ){
                            $scope.newOrder.shipFee = $scope.shipInfo.shipPrice
                            }
                            // 计算运费, 调整订单总价
                            $scope.newOrder.amount += $scope.newOrder.shipFee
                            $scope.showAmount = $scope.newOrder.amount

                        }
                    });
            }



        function getOrderQuantity(cartObj){
            var quantityStr = ""
            for(x in cartObj.items){
                if(cartObj.items[x].select){
                    quantityStr += cartObj.items[x].num + ","
                }
            }
            return quantityStr.substring(0, quantityStr.length -1)
        }

        function getOrderProducts(cartObj){
            var products = []
            for(x in cartObj.items){
                if(cartObj.items[x].select){
                    products.push(cartObj.items[x].product)
                }
            }
            return products
        }

        function verifyOrderAmount(){
            // 计算商品总额
            var productAmount = 0
            for(x in $scope.cart.items){
                if($scope.cart.items[x].select){
                    productAmount += $scope.cart.items[x].product.price * $scope.cart.items[x].num
                }
            }
            $scope.newOrder.amount = $scope.newOrder.productAmount
          /*  $scope.newOrder.promotionAmount = calculatePromotion($scope.promotions)*/

                if($scope.UseCodeStatus == true){
                    $scope.newOrder.amount = $scope.newOrder.productAmount * $scope.enjoyTheCode.discount
                    if( $scope.Use == true ){
                        $scope.newOrder.jifen = $scope.Coupons
                        $scope.newOrder.jifenAmount = $scope.Coupons/100
                        $scope.newOrder.amount = $scope.newOrder.amount -  $scope.newOrder.jifenAmount
                    }
            }else{
                    if( $scope.Use == true ){
                        $scope.newOrder.jifen = $scope.Coupons
                        $scope.newOrder.jifenAmount = $scope.Coupons/100
                        $scope.newOrder.amount = $scope.newOrder.productAmount -  $scope.newOrder.jifenAmount
                    }

                }
            if( $scope.Since == true ){
                $scope.newOrder.shipFee = 0
            }

            $scope.newOrder.amount = $scope.newOrder.amount + calculatePromotion($scope.promotions) + $scope.newOrder.shipFee

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
                            if($scope.wxpay == true){
                            window.location.href = window.location.protocol + '//' + window.location.host + '/wxpay/pay?oid=' + orderId
                            }else{
                                window.location.href = window.location.protocol + '//' + window.location.host + '/alipay/prepare?oid=' + orderId
                            }
                        } else {
                            alert(data.message)
                        }
                    });
        }

        $scope.addOrder = function () {

            // 未登录不允许提交订单
            //是否使用积分
            if( $scope.Use == true ){
                $scope.newOrder.jifen = $scope.Coupons
                $scope.newOrder.jifenAmount = $scope.Coupons/100
             /*   $scope.newOrder.amount = $scope.newOrder.amount -  $scope.newOrder.jifenAmount*/
            }

            if (!$scope.user) {
                alert('用户未登录')
                return
            }
            if (!$scope.user) {
                alert('用户未登录')
                return
            }

            else{
                $scope.newOrder.buyer = $scope.user
                $scope.newOrder.refBuyerId = $scope.user.id
            }

            $scope.newOrder.quantity = getOrderQuantity($scope.cart)
            $scope.newOrder.orderProducts = getOrderProducts($scope.cart)

            if ($scope.newOrder.orderProducts == null) {
                alert('产品未获取,请重试')
                return
            }

            if ($scope.user.refUplineUserId) {
                if ($scope.user.refUplineUserId > 0) {
                    $scope.newOrder.refResellerId = $scope.user.refUplineUserId
                }
            }

            // 重算订单额
           verifyOrderAmount()
//            $log.log($scope.newOrder)
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
                    }else {
                        if($scope.wxpay == true){
                        window.location.href = window.location.protocol + '//' + window.location.host + '/wxpay/pay?oid=' + data.data.id
                        }else{
                            window.location.href = window.location.protocol + '//' + window.location.host + '/alipay/prepare?oid=' + data.data.id
                        }
                    }
                } else {
                    alert(data.message)
                }
            });
        };

     /*   $scope.jinggao = function (obj) {
            if (obj === null) {
                alert('请填写地址')
            }
        }*/
        $scope.addOrderFalse = function () {
            if(!$scope.defaultLocation){
                alert('请添加地址')
                return
            }
            if (!$scope.user) {
                alert('用户未登录')
                window.location.href = window.location.protocol + '//' + window.location.host + '/w/order' + window.location.search
            }else{
                alert('请正确填写积分')
            window.location.href = window.location.protocol + '//' + window.location.host + '/w/order' + window.location.search
            }
        }

     //是否使用积分抵扣
        $scope.Code = null
        $scope.Use = false
        $scope.Coupons =0
        $scope.UseCoupons = function () {
            $scope.Use = !$scope.Use
        } 
        
        //是否使用尊享码
        $scope.UseCodeStatus = false
        $scope.UseCode = function (code) {
            if(!code){
                alert('请填写尊享码')
            }else{
            if($scope.UseCodeStatus){
                alert('已使用尊享码')
            }else{
            //该用户有尊享码
             if(code ==  $scope.enjoyTheCode.number){
                 if($scope.enjoyTheCode.state){
                     $scope.UseCodeStatus = true
                     $scope.newOrder.promotionAmount = $scope.newOrder.productAmount - ( $scope.newOrder.productAmount * $scope.enjoyTheCode.discount)
                 $scope.newOrder.amount =  $scope.newOrder.amount - $scope.newOrder.promotionAmount
                 $scope.showAmount =  $scope.newOrder.amount

                 }else {
                     $scope.Code = null
                     alert('该尊享码未启用')
                 }
             }else{
                 //该用户没有尊享码 or 输入的尊享码不是自己的
                 $http
                     .get('/enjoyTheCode?number=' + code)
                     .success(
                         function (data, status, headers,
                                   config) {
                             if (data.flag) {

                                 $scope.enjoyTheCode = data.data[0];
                                 if($scope.enjoyTheCode.state && $scope.enjoyTheCode.codeType){
                                     $scope.UseCodeStatus = true
                                     $scope.newOrder.promotionAmount = $scope.newOrder.productAmount - ($scope.newOrder.productAmount * $scope.enjoyTheCode.discount)
                                     $scope.newOrder.amount =  $scope.newOrder.amount -  $scope.newOrder.promotionAmount
                                     $scope.showAmount =  $scope.newOrder.amount
                                 }else{
                                     $scope.Code = null
                                     alert('该尊享码未开放')
                                 }
                             }else
                             {
                                 $scope.Code = null
                                 alert('没有该尊享码')
                             }
                         });
             }
            }
            }
        }
//自提货
        $scope.showshipFee = 0
        $scope.Since = false
        $scope.UserSince= function () {
            $scope.showshipFee = $scope.newOrder.shipFee
            $scope.Since = true
        }
//送货
        $scope.Delivery= function () {
            $scope.Since = false
            $scope.showshipFee = 0
        }
//送货
        $scope.weixpay= function () {
            $scope.wxpay = true
            $scope.alipay = false
        }
        $scope.alip= function () {
            $scope.wxpay = false
            $scope.alipay = true
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



function show() {
    document.getElementById("pay").style.display = "block"
}

function close20() {
    document.getElementById("pay").style.display = "none"
}










