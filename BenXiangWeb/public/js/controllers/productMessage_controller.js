/**
 * Created by yanglu on 15/11/16.
 */

var app = angular.module('ProductMessageApp', ['ui.bootstrap']);

app.filter('getFirstImageFromSplitStr', function() {
    return function(splitStr, position) {
        return GetListFromStrInSplit(splitStr)[position];
    }
});

app.controller('ProductMessageController', ['$scope', '$http', function ($scope, $http) {

    $scope.newProduct = {}
    $scope.product = {}
    $scope.slides =[]
    $scope.myInterval = 2000;//轮播时间间隔, 毫秒
    $http.get('/products/' + GetQueryString('id')).success(function (data, status, headers, config) {
        if (data.flag) {
            $scope.product = data.data;
            if($scope.product.descriptionImages != null){
            $scope.description = $scope.product.descriptionImages.split(",")
            }
            $scope.imageList = $scope.product.images.split(",");
            for (i in  $scope.imageList) {
                $scope.slides.push({"id": i, "images": '/showimg/upload/' + $scope.imageList[i]})
            }

        }
        else {
            alert(data.message);
        }
    });

    $http.get('/products?size=10&isHotSale=true&sort=desc').success(function (data, status, headers, config) {
        if (data.flag) {
            $scope.ProductListHotSale = data.data;
        } else {
//										alert(data.message);
        }
    });
}]);
angular.element(document).ready(function() {
    angular.bootstrap(document.getElementById("A2"), [ "ProductMessageApp" ]);
});


function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}