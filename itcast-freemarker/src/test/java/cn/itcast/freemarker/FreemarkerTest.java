package cn.itcast.freemarker;


import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.Test;

import java.io.FileWriter;
import java.util.*;

public class FreemarkerTest {

    @Test
    public void test() throws Exception {
        //1. 创建Configuration对象指定Freemarker版本；
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        //2. 设置模版路径；
        configuration.setClassForTemplateLoading(FreemarkerTest.class,"/ftl");
        //3. 指定生成文件的编码为utf-8；
        configuration.setDefaultEncoding("UTF-8");
        //4. 获取模版
        Template template = configuration.getTemplate("test.ftl");
        //5. 获取数据
        Map<String,Object> dataModel = new HashMap<>();
        dataModel.put("name","xy");
        dataModel.put("message","nice");

        //创建一个集合对象
        List<Map<String,Object>> goodsList = new ArrayList<>();
        Map<String,Object> map1 = new HashMap<>();
        map1.put("name","orange");
        map1.put("price",4.5);
        Map<String,Object> map2 = new HashMap<>();
        map2.put("name","apple");
        map2.put("price",6);
        goodsList.add(map1);
        goodsList.add(map2);

        dataModel.put("today",new Date());
        dataModel.put("number",123456789L);

        dataModel.put("goodsList",goodsList);
        //6. 创建一个文件编写对象Writer
        FileWriter fileWriter = new FileWriter("D:\\itcast\\generatedCode\\test.html");
        //7. 使用模版和数据输出到指定路径
        template.process(dataModel,fileWriter);
        //8. 关闭资源
        fileWriter.close();
    }
}
