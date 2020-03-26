var app = angular.module('particularsApp', ['ui.bootstrap']);

app.filter('safehtmls', function ($sce) {
    return function (htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});


app.filter('getImageFromSplitStr', function () {
    return function (splitStr, position) {
        return GetListFromStrInSplit(splitStr)[position];
    }
});

app.filter('getImageFromSplitStrtext', function () {
    return function (splitStr1, position1) {
        return GetListFromStrInSplitText(splitStr1)[position1];
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

app.controller('particularsController', ['$scope', '$http',

//绑定id页	

    function ($scope, $http) {
        $scope.alls = []
        $scope.description = []
        $scope.product = {}
        $scope.quantity = 1
        $scope.myInterval = 2000;//轮播时间间隔, 毫秒
        $scope.slides = [];


        //获取用户
        $scope.user = {}
        $http.get('/users/current/login').success(
            function (data, status, headers, config) {
                if (data.flag) {
                    $scope.user = data.data;

                    //判断用户是否有收藏该产品
                    $http.get('/users/' + $scope.user.id + '/favoriteProducts/' + GetQueryString('allid')).success(
                        function (data, status, headers, config) {
                            $scope.favoriteProduct = data.flag
                        });
                } else {
                    alert(data.message);
                }
            });


        $scope.imageList = []
        $http.get('/products/' + GetQueryString('allid')).success(
            function (data, status, headers, config) {
                if (data.flag) {
                    $scope.alls = data.data

                    //获取广告
                    $scope.imageList = $scope.alls.images.split(",")
                    for (i in  $scope.imageList) {
                        $scope.slides.push({"id": i, "images": '/showimg/upload/' + $scope.imageList[i]})
                    }

                    $scope.description = $scope.alls.descriptionImages.split(",")


                } else {
                    alert(data.message);
                }
            });

        $scope.cart = {"items": []}
        //获取购物车
        $http.get('/cart/get').success(function (data, status, headers, config) {
            $scope.cart = JSON.parse(data.data);
        });


        /////////////////////操作类//////////////////////
        //加减数量
        $scope.setAdd = function () {
            $scope.quantity = $scope.quantity + 1
        };
        $scope.setMinus = function () {
            if ($scope.quantity > 1) {
                $scope.quantity = $scope.quantity - 1
            }
        };

        //收藏产品
        $scope.favorite = function () {
            $http({
                method: 'PUT',
                url: '/users/' + $scope.user.id + '/favoriteProduct/' + $scope.alls.id + '/on',
                data: $scope.product
            }).success(
                function (data, status, headers, config) {
                    if (data.flag) {

                        refreshData()
                    } else { /* alert(data.message) */
                    }
                });
            $scope.favoriteProduct = true
        };


        //取消收藏
        $scope.cancelFavorite = function () {
            $http({
                method: 'PUT',
                url: '/users/' + $scope.user.id + '/favoriteProduct/' + $scope.alls.id + '/off',
                data: $scope.product
            }).success(
                function (data, status, headers, config) {
                    if (data.flag) {

                        refreshData()
                    } else { /* alert(data.message) */
                    }
                });
            $scope.favoriteProduct = false
        };
        
        
        
        $http.get('/cart/get').success(function (data, status, headers, config) {
            $scope.cart = JSON.parse(data.data);
       });

        $scope.addCart = function (product, num) {

	                var hasItem = false
	                for (x in $scope.cart.items) {
	                    if (product.id == $scope.cart.items[x].pid) {
	                        $scope.cart.items[x].num += num
	                        hasItem = true
	                        break
	                    }
	                }

	                if (!hasItem) {
	                    $scope.cart.items.push({"pid": product.id, "num": num})
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
                        	
                            $http.get('/cart/get').success(function (data, status, headers, config) {
                                $scope.cart = JSON.parse(data.data);
                           });
                            
                        } else {
                            alert(data.message)
                        }
                    });
        }

    }]);


function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null) return unescape(r[2]);
    return null;
}


$(window).scroll(function () {

    if ($(document).scrollTop() + $(window).height() >= $(document).height()) {
        if (typeof(window.isCancel) == "undefined") {
            document.getElementById("loading").style.display = "block";
            window.isCancel = true;
        }
        setTimeout(func, "2000");
        function func() {
            document.getElementById("par_img").style.display = "block";
            document.getElementById("loading").style.display = "none";
            document.getElementById("par_img").Html = "block";
        }
    }

    //还可以执行其他事务，只要所有自定义事件执行完之后加上下面这句即可


});


function show() {
    document.getElementById("pxc3").style.opacity = "1"
    setTimeout("self.close()", 1000);
}

function close() {
    document.getElementById("pxc3").style.opacity = "0"
}

function show1() {
    document.getElementById("pxc1").style.opacity = "1"
    setTimeout("self.close1()", 1000);
}

function close1() {
    document.getElementById("pxc1").style.opacity = "0"
}


function show2() {
    document.getElementById("pxc2").style.opacity = "1"
    setTimeout("self.close2()", 1000);
}

function close2() {
    document.getElementById("pxc2").style.opacity = "0"
}







