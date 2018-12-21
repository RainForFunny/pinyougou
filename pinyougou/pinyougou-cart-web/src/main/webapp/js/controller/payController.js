app.controller("payController",function ($scope, $location, cartService, payService) {
    //获取用户名称
    $scope.getUsername = function () {
        cartService.getUsername().success(function (response) {
            $scope.username = response.username;
        });
    };

    //获取微信支付二维码
    $scope.createNative = function () {
        //1.获取地址栏里的交易订单号
        $scope.outTradeNo = $location.search()["outTradeNo"];
        //发送请求到后台，获取总金额、二维码链接地址、以及交易结果
        payService.createNative($scope.outTradeNo).success(function (response) {
            //创建支付地址成功
            if ("SUCCESS" == response.result_code) {
                //获取本次支付的总金额
                $scope.totalFee = (response.totalFee/100).toFixed(2);

                //生成二维码图片
                var qr = new QRious({
                    //指定哪个元素
                    element:document.getElementById("qrious"),
                    //大小
                    size:250,
                    //容错级别
                    level:"H",
                    //值
                    value:response.code_url
                });

                queryPayStatus($scope.outTradeNo);
            } else {
                alert("支付失败！");
            }
        });
    };

    queryPayStatus = function (outTradeNo) {
        payService.queryPayStatus(outTradeNo).success(function (response) {
            if (response.success) {
                location.href = "paysuccess.html#?money=" + $scope.totalFee;
            } else {
                if ("支付超时" == response.message) {
                    alert(response.message);
                    //重新生成二维码
                    $scope.createNative();
                } else {
                    //支付失败
                    location.href = "payfail.html";
                }
            }
        })
    };
    
    $scope.loadMoney = function () {
        $scope.money = $location.search()["money"];
    }

});