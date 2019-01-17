package org.zj.svn.entity;

/**
 * @BelongsProject: change_log
 * @BelongsPackage: org.zj.svn.entity
 * @Author: ZhangJun
 * @CreateTime: 2019/1/15
 * @Description: ${Description}
 */

//select b.SQL_TEXT,b.FIRST_LOAD_TIME,b.SQL_FULLTEXT from v$sqlarea b
public class SqlHistory {
    private String sqlText;//执行的sql
    private String firstLoadTime;//执行的时间

    public String getSqlText() {
        return sqlText;
    }

    public void setSqlText(String sqlText) {
        this.sqlText = sqlText;
    }

    public String getFirstLoadTime() {
        return firstLoadTime;
    }

    public void setFirstLoadTime(String firstLoadTime) {
        this.firstLoadTime = firstLoadTime;
    }

    @Override
    public String toString() {
        return "SqlHistory{" +
                "sqlText='" + sqlText + '\'' +
                ", firstLoadTime='" + firstLoadTime + '\'' +
                '}';
    }
}
