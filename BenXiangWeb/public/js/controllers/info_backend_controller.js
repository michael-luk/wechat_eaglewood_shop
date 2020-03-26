/**
 * Created by yanglu on 15/11/16.
 */

var app = angular.module('InfoBackendApp', ['ngGrid', 'angularFileUpload', 'fundoo.services','simditor']);
var cellEditableTemplate = "<input ng-class=\"'colt' + col.index\" ng-input=\"COL_FIELD\" ng-model=\"COL_FIELD\" ng-blur=\"updateEntity(col, row)\"/>";
var uploadTemplateInfo = '<div> <input type="file" name="files[]" accept="image/*" ng-file-select="uploadImage($files, \'images\', row.entity)"/> <div ng-repeat="imageName in row.entity.imageList"> <a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-if="isShowImg(imageName)" ng-href="/showImage/{{imageName}}"><img ng-src="/showimg/thumb/{{imageName}}" style="width:50px;height:50px;float:left"></a><input type="button" ng-if="isShowImg(imageName)" ng-click="deleteImage(row.entity, \'images\', imageName)" value="删除" style="float:left" /></div></div>';
var uploadSmallImage = '<div> <input type="file" name="files[]" accept="image/*" ng-file-select="uploadImageShow($files, \'smallImages\', row.entity)"/> <div ng-repeat="imageName in row.entity.smallImagesList"> <a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-if="isShowImg(imageName)" ng-href="/showImage/{{imageName}}"><img ng-src="/showimg/thumb/{{imageName}}" style="width:50px;height:50px;float:left"></a><input type="button" ng-if="isShowImg(imageName)" ng-click="deleteImageShow(row.entity, \'smallImages\', imageName)" value="删除" style="float:left" /></div></div>';
//var uploadSmallImage = '<div> <input type="file" name="files[]" accept="image/*" ng-file-select="uploadImage($files, col.field, row.entity)"/>   <a class="fancybox" target="_blank" data-fancybox-group="gallery" fancybox ng-if="isShowImg(COL_FIELD)" ng-href="/showImage/{{COL_FIELD}}"><img ng-src="/showimg/thumb/{{COL_FIELD}}" style="width:100px;height:100px" ></a></div>';

app.filter('safehtml', function($sce) {
    return function(htmlString) {
        return $sce.trustAsHtml(htmlString);
    }
});

app.controller('InfoBackendController', ['$scope', '$http', '$upload', 'createDialog', '$log', function ($scope, $http, $upload, createDialogService, $log) {
	
    $scope.currentObj = {}
    $scope.list = []
    $scope.page = 1;
    $scope.pageInfo = {}
    $scope.classify=[
        {"id": "首页广告", "name":"首页广告"},
        {"id": "PC首页广告", "name":"PC首页广告"},
        {"id": "广告", "name":"广告"},
        {"id": "新闻", "name":"新闻"},
        {"id": "集团首页广告", "name":"集团首页广告"},
        {"id": "集团那洲基地", "name":"集团那洲基地"},
        {"id": "集团连州基地", "name":"集团连州基地"},
        {"id": "集团招商加盟", "name":"集团招商加盟"},
        {"id": "集团公司新闻", "name":"集团公司新闻"},
        {"id": "集团媒体报道", "name":"集团媒体报道"},
        {"id": "集团行业资讯", "name":"集团行业资讯"},
        {"id": "集团公司概况", "name":"集团公司概况"},
        {"id": "集团生产规划", "name":"集团生产规划"},
        {"id": "集团发展历程", "name":"集团发展历程"},
        {"id": "集团资质荣誉", "name":"集团资质荣誉"},
        {"id": "集团董事长致辞", "name":"集团董事长致辞"},
        {"id": "集团加入本香", "name":"集团加入本香"},
        {"id": "集团联系我们", "name":"集团联系我们"},
        {"id": "集团员工风采", "name":"集团员工风采"},
        {"id": "集团旅游养生", "name":"集团旅游养生"},
        {"id": "集团友情链接", "name":"集团友情链接"},

    ]

    $scope.selectClassifyId = null

    $scope.$watch('selectClassifyId', function(){
        if($scope.list.length > 0) refreshDate();
    }, false);

    $scope.$watch('page', function(){
        refreshDate();
    }, false);
    
    $scope.goHomePage = function() {
    	$scope.page = 1;
    }
    
    $scope.goPrevPage = function() {
    	$scope.page = $scope.pageInfo.current -1;
    }

    $scope.goNextPage = function() {
        $scope.page = $scope.pageInfo.current +1;
    }
    
    $scope.goLastPage = function() {
    	$scope.page = $scope.pageInfo.total;
    }

    $scope.goJumpPage = function() {
        if($scope.jumpPage > $scope.pageInfo.total){
            $scope.jumpPage = $scope.pageInfo.current
            bootbox.alert('总页数最多为' +$scope.pageInfo.total+ '页');
        }else{
            $scope.page = $scope.jumpPage;
        }
    }

    function refreshDate(){
        var url = '/infos?page=' + $scope.page
        if($scope.selectClassifyId != null) url += '&classify=' + $scope.selectClassifyId
        $log.log('get infos from api: ' + url)


        $http.get(url).success(function (data, status, headers, config) {
        	$log.log(data)
            if (data.flag) {
            	for(x in data.data){
            		if(data.data[x].images){
            			data.data[x].imageList = data.data[x].images.split(',');
            		}
        			else{
        				data.data[x].imageList = ''
        			}
            		if(data.data[x].smallImages){
            			data.data[x].smallImagesList = data.data[x].smallImages.split(',');
            		}
        			else{
        				data.data[x].smallImagesList = ''
        			}

            	}
                $scope.list = data.data;
                $scope.pageInfo = data.page;
            }
            else {
            	bootbox.alert(data.message)
            }
        });
    }
    
    $scope.gridOptions = { data: 'list',
        rowHeight: 100,
        // showSelectionCheckbox:true,
        // enableCellSelection: false,
        enableRowSelection: true,
        selectedItems: [],
        multiSelect:false,
        // enableCellEdit: false,
        plugins:[new ngGridFlexibleHeightPlugin()],
        columnDefs: [
            {field: 'id', displayName: 'Id', width: '5%'},
            {field: 'classify', displayName: '信息分类', width: '8%', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'name', displayName: '名称', width: '10%', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'phone', displayName: '联系电话', width: '10%', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'url', displayName: 'url', width: '10%', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'imageList', displayName: '图片', cellTemplate: uploadTemplateInfo, width: '20%'},
            {field: 'smallImagesList', displayName: '小图片', cellTemplate: uploadSmallImage, width: '20%'},
            {field: 'description1', displayName: '描述1', width: '8%', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
            {field: 'description2', displayName: '描述2', width: '8%', enableCellEdit: true, editableCellTemplate: cellEditableTemplate},
        ] };

	$scope.uploadImage = function($files, imageField, parentObj) {
        for (var i = 0; i < $files.length; i++) {
            var file = $files[i];

            $log.log('start upload image file on id: '
                + parentObj.id + ', file: ' + file
                + ', property: ' + imageField)

            $scope.upload = $upload.upload({
                    url : '/upload/image',
                    data : {
                        cid : parentObj.id,
                        className : 'InfoModel',
                        property : imageField
                    },
                    file : file
                })
                .progress(
                    function(evt) {
                        $log.log('upload percent: '
                            + parseInt(100.0 * evt.loaded
                                / evt.total));
                    })
                .success(function(data, status, headers, config) {
                    $log.log(data);
                    if (data.flag) {
                        if (imageField == 'images') {
                            /*	parentObj.images = data.data;
                             parentObj.imageList = parentObj[imageField].split(',');
                             */
                            if(parentObj[imageField])
                                parentObj[imageField] += ',' + data.data;
                            else
                                parentObj.images = data.data;
                            parentObj.imageList = parentObj[imageField].split(',');

                        } else if (imageField == 'smallImages') {
                            parentObj.smallImages = data.data;
                        } else {
                            bootbox.alert('字段不存在: ' + property)
                        }
                    } else {
                        bootbox.alert(data.message)
                    }
                });
            // .error(...)
            // .then(success, error, progress);
        }
    };


    $scope.uploadImageShow = function($files, imageField, parentObj) {
        for (var i = 0; i < $files.length; i++) {
            var file = $files[i];

            $log.log('start upload image file on id: '
                + parentObj.id + ', file: ' + file
                + ', property: ' + imageField)




            $scope.upload = $upload.upload({
                    url : '/upload/image',
                    data : {
                        cid : parentObj.id,
                        className : 'InfoModel',
                        property : imageField
                    },
                    file : file
                })
                .progress(
                    function(evt) {
                        $log.log('upload percent: '
                            + parseInt(100.0 * evt.loaded
                                / evt.total));
                    })
                .success(function(data, status, headers, config) {
                    $log.log(data);
                    if (data.flag) {
                   if (imageField == 'smallImages') {
                            if(parentObj[imageField])
                                parentObj[imageField] += ',' + data.data;
                            else
                                parentObj.smallImages = data.data;
                            parentObj.smallImagesList = parentObj[imageField].split(',');
                            /*parentObj.smallImages = data.data;*/
                        } else {
                            bootbox.alert('字段不存在: ' + property)
                        }
                    } else {
                        bootbox.alert(data.message)
                    }
                });
            // .error(...)
            // .then(success, error, progress);
        }
    };



    // 删除图片
	$scope.deleteImage = function(obj, property, imageName) {
        $scope.currentObj = obj;
        var index = obj.imageList.indexOf(imageName)
        obj.imageList.splice(index, 1)//在数组中删掉这个图片文件名
        obj[property] = obj.imageList.join(",")//数组转为字符串, 以逗号分隔
        $log.log('更新后的images字符串: ' + obj[property])
        
        $scope.saveContent();
	};
	$scope.deleteImageShow = function(obj, property, imageName) {
        $scope.currentObj = obj;
        var index = obj.smallImagesList.indexOf(imageName)
        obj.smallImagesList.splice(index, 1)//在数组中删掉这个图片文件名
        obj[property] = obj.smallImagesList.join(",")//数组转为字符串, 以逗号分隔
        $log.log('更新后的images字符串: ' + obj[property])

        $scope.saveContent();
	};

	$scope.isShowImg = function(url) {
		return (url) && (url.length > 0);
	};
	
    // 当前行更新字段
    $scope.updateEntity = function(column, row) {
        $scope.currentObj = row.entity;
        $scope.saveContent();
    };

    // 新建或更新对象
    $scope.saveContent = function() {
        var content = $scope.currentObj;
        var isNew = !content.id
        var url = '/infos'
        if(isNew){
        	var http_method = "POST";
            content.classify = $scope.gridClassify.selectedItems[0].id
        }else{
        	var http_method = "PUT";
        	url += '/' + content.id
            var pos = $scope.list.indexOf(content);
        }
        $http({method: http_method, url: url, data:content}).success(function(data, status, headers, config) {
                if(data.flag){
                    if(isNew){
                        $scope.list.push(data.data);
                        bootbox.alert('新建[' + data.data.name + ']成功');
                    }else{
                        $scope.list[pos] = data.data;
                    }
                }else{
                    bootbox.alert(data.message);
                }
            });
    };

    $scope.deleteContent = function(){
        var items = $scope.gridOptions.selectedItems;
        if(items.length == 0){
            bootbox.alert("请至少选择一个对象.");
        }else{
            var content = items[0];
            if(content.id){
                bootbox.confirm("您确定要删除这个对象吗?", function(result) {
                    if(result) {
                        $http.delete('/infos/' + content.id).success(function(data, status, headers, config) {
                            if (data.flag) {
                            	var index = $scope.list.indexOf(content);
                                $scope.gridOptions.selectItem(index, false);
                                $scope.list.splice(index, 1);
                                bootbox.alert("删除成功");
                            }
                            else {
                                bootbox.alert(data.message);
                            }
                        });
                    }
                });
            }
        }
    };

    $scope.formSave = function(formOk){
    	if(!formOk){
            bootbox.alert('验证有误, 请重试');
            return
    	}
        $scope.saveContent();
        $scope.$modalClose();
    };
    
    $scope.addContent = function(){
        $scope.currentObj = {};
        fillGridWithClassifys();
        createDialogService("/assets/js/controllers/info_editortemplate.html",{
            id: 'editor',
            title: '新增信息',
            scope: $scope,
            footerTemplate: '<div></div>'
        });
    };


    $scope.updateContent = function(){
        var items = $scope.gridOptions.selectedItems;
        if(items.length == 0){
            bootbox.alert("请至少选择一个对象.");
        }else{
            var content = items[0];
            if(content.id){
                $scope.currentObj =  items[0];
            }
            fillGridWithClassifys();

            createDialogService("/assets/js/controllers/info_editortemplate.html",{
                id: 'editor',
                title: '更新信息',
                scope: $scope,
                footerTemplate: '<div></div>'
            });
        }
    };
    function fillGridWithClassifys(){
        for(x in $scope.classify){
            if($scope.currentObj.classify===$scope.classify[x].id){
                $scope.gridClassify.selectedItems = []
                $scope.gridClassify.selectedItems.push($scope.classify[x])
                break
            }
        }

    }

    $scope.gridClassify = { data: 'classify',
        rowHeight: 30,
        showSelectionCheckbox:true,
        // enableCellSelection: false,
        // enableRowSelection: true,
        selectWithCheckboxOnly: true,
        enableRowSelection: true,
        multiSelect:false,
        selectedItems: [],
        // enableCellEdit: false,
        plugins:[new ngGridFlexibleHeightPlugin()],
        redioCellTemplate: '<div class="ngSelectionCell"><input tabindex="-1" class="ngSelectionCheckbox" type="redio" ng-model="row.entity.refClassify" /></div>',
        columnDefs: [
            {field: 'name', displayName: '所属分类', width: '200'},
        ] };

}]);




