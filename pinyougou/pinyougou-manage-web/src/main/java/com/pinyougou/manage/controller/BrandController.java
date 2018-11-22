package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@Controller
@RequestMapping("/brand")
@RestController//组合了@Controller和@ResponseBody，对类中的所有方法有效
public class BrandController {
    //引入远程的服务对象  com.alibaba.dubbo.config.annotation.Reference
    @Reference
    private BrandService brandService;

    /**
     * 查询所有品牌列表
     * @return：品牌列表json格式字符串
     */
    /*@RequestMapping(value = "/findAll",method = RequestMethod.GET)
    @ResponseBody*/
    @GetMapping("/findAll")
    public List<TbBrand> findAll(){
        return brandService.queryAll();
    }
}
