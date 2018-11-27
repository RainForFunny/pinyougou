package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

//@Controller
@RequestMapping("/brand")
@RestController//组合了@Controller和@ResponseBody，对类中的所有方法有效
public class BrandController {
    //引入远程的服务对象  com.alibaba.dubbo.config.annotation.Reference
    @Reference
    private BrandService brandService;

    /**
     * 查询品牌列表
     * @return 品牌列表 数据结构为：[{"id":1,"text":"联想"},{"id":2,"text":"华为"}]
     */
    @GetMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }

    /**
     * 条件分页查询
     * @param brand 查询条件对象
     * @param page  当前页号
     * @param rows  总记录数
     * @return
     */
    @PostMapping("/search")
    public PageResult search(@RequestBody TbBrand brand,
                             @RequestParam(value = "page",defaultValue = "1") Integer page,
                             @RequestParam(value = "rows",defaultValue = "10") Integer rows){
        return brandService.search(brand,page,rows);
    }

    /**
     * 根据复选框中已选择的品牌id数组删除品牌数据
     * @param ids 已选择的品牌id数组
     * @return 结果是否成功
     */
    @GetMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.delByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    @PostMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            brandService.update(brand);
            return Result.ok("新增成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("新增失败");
    }

    @GetMapping("/findOne")
    public TbBrand findOne(Long id){
        return brandService.findOne(id);
    }

    /**
     * 将用户输入的品牌名称、首字母保存的数据库中
     * @param brand 品牌
     * @return 返回结果是否成功
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.add(brand);
            return Result.ok("新增成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("新增失败");
    }

    /**
     * 分页查询
     * @param page 页号
     * @param rows 页大小
     * @return 分页对象
     */
    @GetMapping("/findPage")
    public PageResult findPage(@RequestParam(value = "page",defaultValue = "1") Integer page,
                               @RequestParam(value = "rows",defaultValue = "10") Integer rows){
        return brandService.findPage(page,rows);
    }

    /**
     * 查询所有品牌列表
     * @return：品牌列表json格式字符串
     */
    /*@RequestMapping(value = "/findAll",method = RequestMethod.GET)
    @ResponseBody*/
    @GetMapping("/findAll")
    public List<TbBrand> findAll(){
//        return brandService.queryAll();
        return brandService.findAll();
    }


    /**
     * 根据当前页和页面大小查询品牌列表
     * @param page 当前页
     * @param rows 页面大小
     * @return     品牌列表
     */
    @GetMapping("/testPage")
    public List<TbBrand> testPage(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer rows){
//        return brandService.testPage(page,rows);
        return (List<TbBrand>) brandService.findPage(page,rows).getRows();
    }
}
