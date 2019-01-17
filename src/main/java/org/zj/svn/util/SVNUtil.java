package org.zj.svn.util;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.zj.svn.entity.SqlHistory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @BelongsProject: change_log
 * @BelongsPackage: org.zj.svn
 * @Author: ZhangJun
 * @CreateTime: 2019/1/15
 * @Description: ${Description}
 */
public class SVNUtil {
    private String path;
    private String svnPathNeedReplace;
    private String projectReplace;
    private String classPath;
    private StringBuilder changeSb=new StringBuilder();
    private String shellDirName="脚本";
    private String url;
    private String name;
    private String password;
    private long startVersion=0;
    private long endVersion=-1;
    private DBUtil dbUtil;
    public SVNUtil(String url,String name,String password,long endVersion,String dbUrl,String username,String dbPassword,String driverName,String path,String svnPathNeedReplace,String projectReplace,String classPath){
        this.path=path;
        this.svnPathNeedReplace=svnPathNeedReplace;
        this.projectReplace=projectReplace;
        this.classPath=classPath;
        this.url=url;
        this.name=name;
        this.password=password;
        this.endVersion=endVersion;
        dbUtil=new DBUtil(username,dbPassword,dbUrl,driverName);
    }
    public  void showLogs(){
        try {
            SVNRepository repository;
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
            ISVNAuthenticationManager defaultAuthenticationManager = SVNWCUtil.createDefaultAuthenticationManager(name, password);
            repository.setAuthenticationManager(defaultAuthenticationManager);
            Collection logEntries=null;
            logEntries=repository.log(new String[]{""},null,startVersion,endVersion,true,true);
            for(Iterator entries=logEntries.iterator();entries.hasNext();){
                SVNLogEntry entry = (SVNLogEntry) entries.next();
                System.out.println(entry.getDate());
                if(entry.getChangedPaths().size()>0){
                    for(Iterator changePaths=entry.getChangedPaths().keySet().iterator();changePaths.hasNext();){
                        SVNLogEntryPath svnLogEntryPath = entry.getChangedPaths().get(changePaths.next());

                        System.out.println("改变日志"+svnLogEntryPath.getPath());
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 打包文件
     */
    public void packageUpdate(){
        //我需要获得一个时间
        //获得当年路径下的一个压缩包
        String rarDate = getRarDate(path);
        this.startVersion=getStartVersion(path);
        //这是sql的更新记录
        StringBuilder oracleSqlHistory = getOracleSqlHistory(rarDate);
        //接下来把这些记录写入到文件
        String s=path+"/"+shellDirName+"/"+rarDate+".sql";
        System.out.println(s+"寫入脚本");
        writeText(oracleSqlHistory,path+"/"+shellDirName+"/"+rarDate+".sql");
        //接下来根据开始版本来获得更新记录
        //根据记录来把文件复制到指定位置
        copyChangeFile();
        //把文件更改日志写入到文件
        writeText(changeSb,path+"/更改日志.txt");
    }

    private void copyChangeFile() {
        try {
            SVNRepository repository;
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
            ISVNAuthenticationManager defaultAuthenticationManager = SVNWCUtil.createDefaultAuthenticationManager(name, password);
            repository.setAuthenticationManager(defaultAuthenticationManager);
            Collection logEntries=null;
            logEntries=repository.log(new String[]{""},null,startVersion,endVersion,true,true);
            for(Iterator entries=logEntries.iterator();entries.hasNext();){
                SVNLogEntry entry = (SVNLogEntry) entries.next();
                System.out.println(entry.getDate());
                if(entry.getChangedPaths().size()>0){
                    for(Iterator changePaths=entry.getChangedPaths().keySet().iterator();changePaths.hasNext();){
                        SVNLogEntryPath svnLogEntryPath = entry.getChangedPaths().get(changePaths.next());
                        String changePath = svnLogEntryPath.getPath();
                        System.out.println("svn拿到的   "+changePath);
                        changeSb.append(changePath).append(System.getProperty("line.separator"));
                        String realPath=changePath.replaceAll(svnPathNeedReplace,projectReplace);
                        System.out.println("svn拿到替换后     "+realPath);
                        copyFile(realPath,path);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 把一个文件复制到指定文件夹
     * @param realFilePath
     * @param toPath
     */
    private void copyFile(String realFilePath, String toPath) {
        System.out.println("copyFile   "+realFilePath);
        File f=new File(toPath);
        if(!f.exists()){
            f.mkdirs();
        }

        if(realFilePath.endsWith(".class")){
            doCopyFile(realFilePath,toPath+"/class/");
        }

        if(realFilePath.endsWith(".java")){
            doCopyFile(realFilePath,toPath+"/代码/");
            //这里还要找到他的class 文件的位置，然后也要复制到里面
            findClassAndCopy(realFilePath,toPath+"/class/");
        }
        if(realFilePath.endsWith(".html")){
            doCopyFile(realFilePath,toPath+"/页面/");
        }
    }

    /**
     * 这里还要找到class文件的路径，然后复制到里面
     * @param realFilePath
     * @param toPath
     */
    private void findClassAndCopy(String realFilePath, String toPath) {
        System.out.println("findClassAndCopy      "+realFilePath );

        String dir = realFilePath.replaceAll(projectReplace + "src/main/java", classPath).replaceAll(".java", "");
        String classDir=dir.substring(0,dir.lastIndexOf("/"));

        String nameStrix=realFilePath.substring(0,realFilePath.lastIndexOf("."));
        nameStrix=nameStrix.replaceAll("/","\\\\");
        nameStrix=nameStrix.replaceAll("src\\\\main\\\\java","target\\\\classes");
        File file=new File(classDir);
        if(file==null){
            return;
        }
        if(file.listFiles()==null){
            return;
        }
        for(File f:file.listFiles()){
            System.out.println("   "+f.getAbsolutePath());
            System.out.println("   "+nameStrix);
            System.out.println("----------------");
            if(f.getAbsolutePath().endsWith(".class")&&(f.getAbsolutePath().equals(nameStrix+".class")||f.getAbsolutePath().startsWith(nameStrix+"$"))){
                System.out.println("-0------->"+f.getAbsolutePath()+"      这是class文件路径");
                copyFile(f.getAbsolutePath(),toPath);
            }
        }

    }

    private void doCopyFile(String  realFilePath,String toPath){
        File file = new File(realFilePath);
        if(!file.exists()){
            return;
        }
        FileOutputStream fos = null;
        byte[] buffer = new byte[1024];
        FileInputStream fis = null;
        try {
            File toFile = new File(toPath + file.getName());
            toFile.mkdirs();
            toFile.delete();
            toFile.createNewFile();
            fos=new FileOutputStream(toFile);
            fis= new FileInputStream(file);
            while (fis.read(buffer)!=-1) {
                fos.write(buffer);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                fis.close();
                fos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private Integer getStartVersion(String path) {
        String nameSplite = getNameSplite(path, 2);
        if(nameSplite==null||"".equals(nameSplite)){
            return 0;
        }
        return Integer.valueOf(getNameSplite(path,2));
    }

    private void writeText(StringBuilder sb, String filePath) {
        try {
            File file = new File(filePath);
            file.mkdirs();
            file.delete();
            file.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(sb.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获得当前路径下的一个压缩包的名字来截取日期
     * @param path
     * @return
     */
    public String getRarDate(String path){
        return getNameSplite(path,1).replaceAll("/+",":").replaceAll("&","/");
    }

    private String getNameSplite(String path,int index){
        File file = new File(path);
        if(!file.isDirectory()){
            return null;
        }
        for(File f:file.listFiles()){
            String name = f.getName();
            String[] s = name.split("_");
            if(name.endsWith("rar")){
                //就是这个，我要获得名字，然后 截取出日期
                if(s!=null&&s.length>=index+1){
                    return s[index].replaceAll(".rar","");
                }
            }
        }
        return null;
    }

    /**
     * 获得sql的执行记录
     * @param dateStrFrom 2019-01-09/09:24:47
     */
    public StringBuilder getOracleSqlHistory(String dateStrFrom){
        StringBuilder sb=new StringBuilder();
        String toNow = new SimpleDateFormat("yyyy-MM-dd/hh:mm:ss").format(new Date());
        String sql="select b.SQL_TEXT,b.FIRST_LOAD_TIME,b.SQL_FULLTEXT from v$sqlarea b where b.FIRST_LOAD_TIME between '"+dateStrFrom+"' and '"+toNow+"'";
        if(dbUtil!=null){
            List<SqlHistory> list = dbUtil.getList(sql, SqlHistory.class);
            for(SqlHistory b:list){
                if(b.getSqlText().startsWith("update")||b.getSqlText().startsWith("create")||b.getSqlText().startsWith("insert")||b.getSqlText().equals("delete")){

                    sb.append("--").append(b.getFirstLoadTime()).append(System.getProperty("line.separator"));
                    sb.append(b.getSqlText()+";");
                    sb.append(System.getProperty("line.separator"));
                }
            }
        }
        return sb;
    }
}
