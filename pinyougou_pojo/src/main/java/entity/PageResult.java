package entity;

import com.pinyougou.pojo.TbBrand;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable{
    //总的记录数:
    private Long total;
    //查询当页的数据:
    private List rows;

    @Override
    public String toString() {
        return "PageResult{" +
                "total=" + total +
                ", rows=" + rows +
                '}';
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }

    public PageResult(Long total, List rows) {

        this.total = total;
        this.rows = rows;
    }
}
