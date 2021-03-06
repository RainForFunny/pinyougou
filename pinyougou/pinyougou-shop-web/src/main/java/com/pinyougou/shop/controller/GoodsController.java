package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/goods")
@RestController
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult findPage(@RequestParam(value = "page", defaultValue = "1")Integer page,
                               @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        return goodsService.findPage(page, rows);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Goods goods) {
        try {
            //获取当前登录的用户（商家）设置商家信息
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            goods.getGoods().setSellerId(sellerId);
            //未申请审核
            goods.getGoods().setAuditStatus("0");

            goodsService.addGoods(goods);

            return Result.ok("增加成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("增加失败");
    }

    /**
     * 根据商品spu id查询商品基本、描述、sku列表
     * @param id 商品spu id
     * @return 商品基本、描述、sku列表
     */
    @GetMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findGoodsById(id);
    }

    /**
     * 根据商品spu id保存商品基本、描述、sku列表；
     * @param goods 商品基本、描述、sku列表
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            //获取当前商家信息
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();

            //根据商品id获取商品所属的商家
            TbGoods oldGoods = goodsService.findOne(goods.getGoods().getId());

            //如果当前商家id与要修改的商品的商家的id相同且与oldGoods所属的商家相同
            if (sellerId.equals(goods.getGoods().getSellerId()) && sellerId.equals(oldGoods.getSellerId())) {
                goodsService.updateGoods(goods);
            }else {
                return Result.fail("非法操作");
            }
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.delByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 根据商品SPU id把上架状态设置为1
     * @param ids 商品SPU id
     * @return 操作结果
     */
    @GetMapping("/updateMarketableStatus")
    public Result updateMarketableStatus(Long[] ids) {
        try {
            goodsService.updateMarketableStatus(ids);
            return Result.ok("上架成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("上架失败！");
    }
    /**
     * 分页查询列表
     * @param goods 查询条件
     * @param page 页号
     * @param rows 每页大小
     * @return
     */
    @PostMapping("/search")
    public PageResult search(@RequestBody  TbGoods goods, @RequestParam(value = "page", defaultValue = "1")Integer page,
                               @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(sellerId);
        return goodsService.search(page, rows, goods);
    }

    /**
     * 根据商品spu id数组更新那些商品的状态为1
     * @param ids 商品id数组
     * @param status 商品状态
     * @return 更新结果
     */
    @GetMapping("/updateStatus")
    public Result updateStatus(Long[] ids,String status){
        try {
            goodsService.updateStatus(ids,status);
            return Result.ok("审核通过");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("更新商品状态失败");
    }
}
