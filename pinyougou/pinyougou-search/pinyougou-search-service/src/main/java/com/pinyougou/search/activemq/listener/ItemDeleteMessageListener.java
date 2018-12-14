package com.pinyougou.search.activemq.listener;

import com.pinyougou.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.util.Arrays;

public class ItemDeleteMessageListener extends AbstractAdaptableMessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        //接收消息，转换为商品spu id数组
        ObjectMessage objectMessage = (ObjectMessage) message;
        Long[] goodIds = (Long[]) objectMessage.getObject();

        //更新到solr中
        itemSearchService.deleteItemByGoodsIdList(Arrays.asList(goodIds));
    }
}
