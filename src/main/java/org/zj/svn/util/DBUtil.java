package org.zj.svn.util;

import com.alibaba.druid.pool.DruidDataSource;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: change_log
 * @BelongsPackage: org.zj.svn.util
 * @Author: ZhangJun
 * @CreateTime: 2019/1/15
 * @Description: ${Description}
 */
public class DBUtil {
    private DruidDataSource druidDataSource;
    private Connection connection;
    public DBUtil(String username,String password,String url,String driverName){
        druidDataSource=new DruidDataSource();
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);
        druidDataSource.setUrl(url);
        druidDataSource.setDriverClassName(driverName);
    }
    private Connection getConnection(){
        try {
            if (druidDataSource!=null&&(connection == null || connection.isClosed())) {
                connection=druidDataSource.getConnection();
            }
            return connection;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    private Statement getStatement(){
        try {
            Connection connection = getConnection();
            if (connection == null || connection.isClosed()) {
                return null;
            }
            return connection.createStatement();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据sql来获得ResultSet
     * @param sql
     * @return
     */
    private ResultSet getResultSet(String sql){
        Statement statement = getStatement();
        if(statement==null){
            return null;
        }
        try {
            return statement.executeQuery(sql);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    private <T> List<T> inflateList(ResultSet resultSet, Class c){
        List<T> result=new ArrayList<T>();
        try {
            while (resultSet.next()) {
                result.add((T) convertBean(resultSet,c));
            }
        }catch (Exception e){

        }
        return result;
    }

    /**
     * 执行sql解析成bean
     * @param sql
     * @param c
     * @param <T>
     * @return
     */
    public <T> List<T> getList(String sql,Class c){
        ResultSet resultSet = getResultSet(sql);
        return inflateList(resultSet,c);
    }

    /**
     * 把resultset封装成Bean
     * @param resultSet
     * @param c
     * @param <T>
     * @return
     */
    private <T> T convertBean(ResultSet resultSet, Class c) {
        try {
            Object o = c.newInstance();
            for(Field f:c.getDeclaredFields()){
                f.setAccessible(true);
                f.set(o,getResultSetVal(resultSet,f));
            }
            return (T) o;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获得result中的值
     * @param resultSet
     * @param f
     * @return
     */
    private Object getResultSetVal(ResultSet resultSet, Field f) {
        try {
            if (f.getType() == String.class) {
                return resultSet.getString(getColName(f.getName()));
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return null;
    }


    private static String getColName(String name){
        List<Integer> indexs=new ArrayList<Integer>();
        for(int i=0;i<name.length();i++){
            String substring = name.substring(i, i + 1);
            if(substring.equals(substring.toUpperCase())){
                //如果一致
                name=name.substring(0,i).toUpperCase()+"_"+name.substring(i);
                i++;
                continue;
            }
        }

        return name.toUpperCase();
    }

    public static void main(String[] args) {
        System.out.println(getColName("loanDateTime"));
    }

}
