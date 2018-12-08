<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Freemarker测试</title>
</head>
<body>
<#--注释，不会被输出-->

<h1>${name}----${message}</h1>
<br>
<hr>
<br>
assign指令<br>
<#--assign指令使用-->
<#assign linkman="黑马"/>
${linkman}<br>

<#assign info={"mobile":"133333","address":"吉山村"}/>
mobile = ${info.mobile};address= ${info.address}
<br>
<hr>
<br>
include引入其他模板<br>
<#include "head.ftl"/>

<br>
<hr>
<br>
if条件语句<br>
<#assign bool=true>
<#if bool>
    bool的值为true
<#else>
    bool的值为false
</#if>

<br>
<hr>
<br>
list循环控制语句<br>
<#list goodsList as goods>
    ${goods_index}--${goods.name}--${goods.price}<br>
</#list>
总共有${goodsList?size}条记录
<br>
<hr>
<br>

eval内建函数，可以将Json字符串转换为对象<br>
<#assign jsonStr= '{"id":123,"name":"jack"}'/>
<#assign jsonObj=jsonStr?eval/>
${jsonObj.id}---${jsonObj.name}
<br>
<hr>
<br>

日期格式化<br>
.now 表示当前日期时间：${.now}<br>
today的日期时间：${today?datetime}<br>
today的日期：${today?date}<br>
today的时间：${today?time}<br>
today的格式化显示：${today?string("yyyy年MM月dd日 HH:mm:ss")}<br>

<br>
<hr>
<br>
number数值默认显示=${number}；可以使用?c方式进行格式化为字符串显示而不会出现千分位上使用“,”隔开。现在为：${number?c}

<br>
<hr>
<br>
空值处理<br>
<br>
值为空可以使用!表示什么都不显示${emp!}；如果值为空以后显示具体的值则可以以这种方式==>  !"要显示的值"-->${emp!"emp的值为空"}

<br>
<br>
???，前面两个??表示一个变量是否存在；如果存在则返回true，否则返回false。后面一个?表示函数的调用。<br>
<#assign bool2=false/>
${bool2???string}

<br>
<#if stre??>
    stre存在
<#else>
    stre不存在
</#if>
</body>
</html>