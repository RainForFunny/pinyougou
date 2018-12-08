package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;

import java.util.List;

public interface GoodsService extends BaseService<TbGoods> {

    PageResult search(Integer page, Integer rows, TbGoods goods);

    void addGoods(Goods goods);

    /**
     * 根据商品spu id查询商品基本、描述、sku列表
     * @param id 商品spu id
     * @return 商品基本、描述、sku列表
     */
    Goods findGoodsById(Long id);

    /**
     * 根据商品spu id保存商品基本、描述、sku列表；
     * @param goods 商品基本、描述、sku列表
     */
    void updateGoods(Goods goods);

    /**
     * 根据商品spu id数组更新那些商品的状态为1
     * @param ids 商品id数组
     * @param status 商品状态
     */
    void updateStatus(Long[] ids, String status);

    void deleteGoodsByIds(Long[] ids);

    /**
     * 根据商品SPU id把山那个价状态设置为1
     * @param ids 商品SPU id
     */
    void updateMarketableStatus(Long[] ids);

    /**
     * 根据商品spu id数组查询这些spu对应的已启用的sku列表
     * @param ids 商品spu id数组
     * @param status sku的状态
     * @return sku列表
     */
    List<TbItem> findItemListByIdsAndStatus(Long[] ids, String status);
}