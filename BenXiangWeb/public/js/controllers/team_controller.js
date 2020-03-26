var app = angular.module('teamApp', []);

app.filter('resellerDisplay', function () {
    return function (user) {
        if (user.isReseller)
            return '已是分销商'
        else
            return '将TA设为分销商'
    }
});

app.controller('teamController', ['$log', '$scope', '$http', function ($log, $scope, $http) {

    $scope.downlineUsers = [];
    $scope.user = {};
    $scope.resellerAmount = 0;
    $scope.downlineUserOne = 0
    $scope.downlineUserTwo = 0
    $scope.downlineUserThree = 0


    $http.get('/users/current/login').success(function (data, status, headers, config) {

        if (data.flag) {
            $scope.user = data.data;

            $http.get('/downlines/users/' + $scope.user.id + '?size=500').success(function (data, status, headers, config) {
                if (data.flag) {
                    $scope.downlineUsers = data.data;
                    $scope.downlineUserOne = data.message;
                    $scope.downlineUserTwo = data.message1;
                    /*    $scope.downlineUserThree = data.message2;*/

                    $http.get('/resellerorders/amount/users/' + $scope.user.id).success(function (data, status, headers, config) {
                        if (data.flag) {
                            $scope.resellerAmount = data.data;
                        } else {
                            $scope.resellerAmount = 0;
                        }
                    });
                }
            });
        } else {
            alert(data.message);
        }
    });

    $scope.setReseller = function (userObj) {
        if (userObj.isReseller)
            return;

        // 利用对话框返回的值 （true 或者 false）
        if (confirm("你确定将 [" + userObj.nickname + "] 设为分销商吗?")) {
            $http({
                method: 'PUT',
                url: '/users/set/reseller/' + userObj.id,
                data: {}
            }).success(function (data, status, headers, config) {
                if (data.flag) {
                    userObj.isReseller = true
                    alert('设置成功')
                } else {
                    alert(data.message)
                }
            });
        }
    }
}]);