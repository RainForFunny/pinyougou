package com.pinyougou.vo;

import java.io.Serializable;
import java.util.List;
//分页对象
public class PageResult implements Serializable {
    //总记录数
    private long total ;

    //列表
    //占位符？，如果赋值以后是不可以修改里面的值的
    private List<?> rows;

    public PageResult(long total, List<?> rows) {
        this.total = total;
        this.rows = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<?> getRows() {
        return rows;
    }

    public void setRows(List<?> rows) {
        this.rows = rows;
    }
}
