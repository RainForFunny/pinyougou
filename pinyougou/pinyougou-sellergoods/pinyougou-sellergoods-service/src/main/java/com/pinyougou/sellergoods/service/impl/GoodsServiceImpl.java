package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;
import javafx.collections.MapChangeListener;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
@Transactional
@Service(interfaceClass = GoodsService.class)
public class GoodsServiceImpl extends BaseServiceImpl<TbGoods> implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsDescMapper goodsDescMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemCatMapper itemCatMapper;

    @Autowired
    private SellerMapper sellerMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public PageResult search(Integer page, Integer rows, TbGoods goods) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        //删除状态为1的商品可以设置不可见
        criteria.andNotEqualTo("isDelete","1");
        if(!StringUtils.isEmpty(goods.getSellerId())){
            criteria.andLike("sellerId", goods.getSellerId());
        }
        if(!StringUtils.isEmpty(goods.getAuditStatus())){
            criteria.andLike("auditStatus",  goods.getAuditStatus());
        }
        if(!StringUtils.isEmpty(goods.getGoodsName())){
            criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
        }

        List<TbGoods> list = goodsMapper.selectByExample(example);
        PageInfo<TbGoods> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public void addGoods(Goods goods) {
        //新增商品基本信息
        add(goods.getGoods());

        //新增商品描述
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insertSelective(goods.getGoodsDesc());

        saveItemList(goods);
    }

    @Override
    public Goods findGoodsById(Long id) {
        Goods goods = new Goods();
        //查询商品SPU
        goods.setGoods(findOne(id));

        //查询商品描述
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(tbGoodsDesc);

        //查询商品sku列表
        TbItem item = new TbItem();
        item.setGoodsId(id);
        List<TbItem> itemList = itemMapper.select(item);
        goods.setItemList(itemList);

        return goods;

    }

    @Override
    public void updateGoods(Goods goods) {
        //更新商品spu列表
        goodsMapper.updateByPrimaryKey(goods.getGoods());

        //更新商品描述
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

        //删除sku列表
        //根据spu id删除sku列表 delete form tb_item where goods_id =?
        TbItem item = new TbItem();
        item.setGoodsId(goods.getGoods().getId());
        itemMapper.delete(item);

        //更新sku列表
        saveItemList(goods);

    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        //update tb_goods set  audit_status = '1' where id = ?
        TbGoods goods = new TbGoods();
        goods.setAuditStatus(status);

        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        //参数一：更新对象；参数二：更新条件
        goodsMapper.updateByExampleSelective(goods,example);

        //审核通过之后要将item的状态设为1
        if ("2".equals(status)) {
            TbItem item = new TbItem();
            item.setStatus("1");
            Example itemExample = new Example(TbItem.class);
            itemExample.createCriteria().andIn("id", Arrays.asList(ids));
            itemMapper.updateByExampleSelective(item,itemExample);
        }
    }

    public void deleteGoodsByIds(Long[] ids) {
        //根据商品spu id更新商品的删除状态（is_delete）为已删除（值为1）
        TbGoods goods = new TbGoods();
        goods.setIsDelete("1");

        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        goodsMapper.updateByExampleSelective(goods,example);
    }

    @Override
    public void updateMarketableStatus(Long[] ids) {
        //根据商品spu id更新商品的上架状态(is_marketable)为已上架（值为1）
        TbGoods goods = new TbGoods();
        goods.setIsMarketable("1");

        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id",Arrays.asList(ids));
        goodsMapper.updateByExampleSelective(goods,example);
    }

    /**
     * 保存动态sku列表
     * @param goods 商品信息（基本信息，商品描述，sku列表）
     */
    private void saveItemList(Goods goods) {

        if ("1".equals(goods.getGoods().getIsEnableSpec())){
            //[{spec:{},price:0, num:9999, status:"0",isDefault:"0"}]
            if (goods.getItemList() != null && goods.getItemList().size() > 0) {
                for (TbItem item : goods.getItemList()) {
                    //获取标题=spu名称+所有规格的选项值
                    String title = goods.getGoods().getGoodsName();
                    //获取规格；{"网络":"移动3G","机身内存":"16G"}
                    Map<String,Object> map = JSON.parseObject(item.getSpec(), Map.class);
                    Set<Map.Entry<String, Object>> entries = map.entrySet();

                    for (Map.Entry<String, Object> entry : entries) {
                        title += " " + entry.getValue().toString();
                    }
                    item.setTitle(title);

                    setItemValue(item,goods);

                    //保存sku
                    itemMapper.insertSelective(item);
                }
            }
        } else {
            //未启用规格，从spu中取，spu中没有，则自己设置
            TbItem item = new TbItem();
            // 如果spu中没有的数据，如：spec（｛｝），num（9999），status(0未启用)，isDefault(1默认)
            item.setSpec("{}");
            item.setPrice(goods.getGoods().getPrice());
            item.setNum(9999);
            item.setStatus("0");
            item.setIsDefault("1");
            item.setTitle(goods.getGoods().getGoodsName());

            //设置其他数据
            setItemValue(item,goods);
            itemMapper.insertSelective(item);
        }
    }

    private void setItemValue(TbItem item, Goods goods) {
        //商品分类 来自 商品spu的第3级商品分类id
        item.setCategoryid(goods.getGoods().getCategory3Id());
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(item.getCategoryid());
        item.setCategory(itemCat.getName());
        //图片：可以从spu的图片地址列表获取第一张图片
        if (!StringUtils.isEmpty(goods.getGoodsDesc().getItemImages())) {
            List<Map> imageList = JSONArray.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
            if (imageList.get(0).get("url") != null) {
                item.setImage(imageList.get(0).get("url").toString());
            }
        }

        item.setGoodsId(goods.getGoods().getId());

        //品牌
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());

        item.setCreateTime(new Date());
        item.setUpdateTime(item.getCreateTime());

        //卖家
        item.setSellerId(goods.getGoods().getSellerId());
        TbSeller seller = sellerMapper.selectByPrimaryKey(item.getSellerId());
        item.setSellerId(seller.getName());
    }

}
