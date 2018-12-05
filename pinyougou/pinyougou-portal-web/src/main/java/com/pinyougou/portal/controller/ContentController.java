package com.pinyougou.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.pojo.TbContent;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/content")
@RestController
public class ContentController {

    @Reference
    private ContentService contentService;

    @RequestMapping("/findContentListByCategoryId")
    public List<TbContent> findContentListByCategoryId(Long categoryId) {
        return contentService.findContentListByCategoryId(categoryId);
    }

}
