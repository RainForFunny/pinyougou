app.controller("orderInfoController",function ($scope,addressService,cartService) {

    //加载地址列表
    $scope.findAddressList = function () {
        addressService.findAddressList().success(function (response) {
            $scope.addressList = response;
            //获取默认的地址
            for (var i = 0; i < response.length; i++) {
                var address = response[i];
                if ("1" == address.isDefault) {
                    $scope.address = address;
                    break;
                }
            }
        });
    };

    //加载购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;

            //计算总数量和总价 totalValue = totalNum + totalMoney 两部分
            $scope.totalValue = cartService.subTotalValue(response);
        });
    };

    //获取用户信息
    $scope.getUsername = function () {
        cartService.getUsername().success(function (response) {
            $scope.username = response.username;
        })
    };

    //是否选中地址
    $scope.isSelectedAddress = function (address) {
        return $scope.address == address;
    };

    //当前选择的地址
    $scope.selectedAddress = function (address) {
        $scope.address = address;
    };

    //初始化支付方式，1为微信支付，2为货到付款
    $scope.order = {"paymentType":"1"};

    $scope.selectPaymentType = function (type) {
        $scope.order.paymentType = type;
    };

    $scope.submitOrder = function () {
        //设置收件人信息
        $scope.order.receiver = $scope.address.contact;
        $scope.order.receiverMobile = $scope.address.mobile;
        $scope.order.receiverAreaName = $scope.address.address;

        cartService.submitOrder($scope.order).success(function (response) {
            if (response.success) {
                if ("1" == $scope.order.paymentType) {
                    //如果是微信支付则跳转到微信支付成功页面
                    location.href = "pay.html#?outTradeNo=" + response.message;
                } else {
                    //如果是货到付款，则直接跳转到付款成功页面
                    location.href = "paysuccess.html";
                }
            } else {
                alert("提交订单失败!");
            }
        })
    }
});