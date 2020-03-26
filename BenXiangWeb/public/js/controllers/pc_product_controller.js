
var app = angular.module('PcProductApp', []);


app.filter('safehtmls', function($sce) {
    return function(htmlString) {
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

app.filter('getImageFromSplitStrtext', function() {
    return function(splitStr1, position1) {
        return GetListFromStrInSplitText(splitStr1)[position1];
    }
});


app.controller('PcProductController', ['$scope', '$http',

    function($scope, $http) {

        $scope.catalog = {};
                    //获取产品
                    $http.get('/products?size=50').success(

                        function(data, status, headers, config) {
                            if (data.flag) {
                                $scope.catalogProducts = data.data
                            } else {
                                alert(data.message);
                            }
                        });


    }]);

function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}

angular.element(document).ready(function() {
    angular.bootstrap(document.getElementById("A2"), [ "PcProductApp" ]);
});




























