package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.jms.*;
import java.util.List;

@RequestMapping("/goods")
@RestController
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ActiveMQQueue solrItemQueue;

    @Autowired
    private ActiveMQQueue solrItemDeleteQueue;
    @Autowired
    private ActiveMQTopic itemTopic;
    @Autowired
    private ActiveMQTopic itemDeleteTopic;


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

    /**
     * 根据商品SPU id把删除状态设置为1
     * @param ids
     * @return
     */
    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.deleteGoodsByIds(ids);
//            itemSearchService.deleteItemByGoodsIdList(Arrays.asList(ids));
            //发送MQ消息，更新搜索系统
            sendMQMsg(solrItemDeleteQueue,ids);

            sendMQMsg(itemDeleteTopic,ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 发送消息到ActiveMQ
     * @param destination 模式
     * @param ids 发送的商品spu id数组
     * @throws JMSException
     */
    private void sendMQMsg(Destination destination, Long[] ids) {
        jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage objectMessage = session.createObjectMessage();
                objectMessage.setObject(ids);
                return objectMessage;
            }
        });
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
            if ("2".equals(status)) {
                //审核通过更新搜索系统数据
                //根据商品spu id数组查询这些spu对应的已启用的sku列表
                List<TbItem> itemList = goodsService.findItemListByIdsAndStatus(ids,"1");

                //更新搜索系统数据
//                itemSearchService.importItemList(itemList);
                jmsTemplate.send(solrItemQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage = session.createTextMessage();
                        textMessage.setText(JSON.toJSONString(itemList));
                        return textMessage;
                    }
                });

                //更新详情系统的静态页面
                sendMQMsg(itemTopic,ids);
            }
            return Result.ok("审核通过");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("更新商品状态失败");
    }


}
