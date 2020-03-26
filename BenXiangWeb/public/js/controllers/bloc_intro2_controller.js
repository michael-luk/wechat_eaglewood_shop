var app = angular.module('intro2App', []);

app.filter('safehtmls', function($sce) {
    return function(htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});


app.filter('getImageFromSplitStr', function() {
    return function(splitStr, position) {
        return GetListFromStrInSplit(splitStr)[position];
    }
});

app.filter('getImageFromSplitStrtext', function() {
    return function(splitStr1, position1) {
        return GetListFromStrInSplitText(splitStr1)[position1];
    }
});

app.controller('intro2Controller', [
    '$scope',
    '$http',

    //绑定id页
    function($scope, $http) {
        $http.get('/infos/' + GetQueryString('id')).success(
            function(data, status, headers, config) {
                if (data.flag) {
                    $scope.intro2 = data.data
                    $scope.pageInfo=data.page
                } else {
                    alert(data.message);
                }
            });



    } ]);


function GetQueryString(name) {
    var url = decodeURI(window.location.search);
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = url.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}

angular.element(document).ready(function() {
    angular.bootstrap(document.getElementById("A2"), [ "news2App" ]);
});