package org.zj.svn;

import org.zj.svn.util.SVNUtil;

/**
 * @BelongsProject: change_log
 * @BelongsPackage: org.zj.svn
 * @Author: ZhangJun
 * @CreateTime: 2019/1/15
 * @Description: ${Description}
 */
public class Content {
    public static void main(String[] args) {
        //spring.datasource.url=jdbc:oracle:thin:@//101.201.57.169:2521/NEWUAT
        //#spring.datasource.url=jdbc:oracle:thin:@//101.201.57.188:2521/NEWUAT
        //#spring.datasource.username=zjrlworkflow20180922
        //#spring.datasource.password=zjrl123
        //spring.datasource.username=zjrlworkflow20181127
        //spring.datasource.password=zjrltest
        //#spring.datasource.username=zjrlworkflow
        //#spring.datasource.username=autofin
        //#spring.datasource.password=asdasd
        //spring.datasource.driver-class-name=oracle.jdbc.driver.OracleDriver

        new SVNUtil("svn://101.201.57.169:4690/zjrlsource/SourceCode/zjrlframwork/coreframwork","zhangxr",
                "zhangxr123@a",-1,"jdbc:oracle:thin:@//101.201.57.169:2521/NEWUAT",
                "zjrlworkflow20181127","zjrltest","oracle.jdbc.driver.OracleDriver",
                "d:/testsvn","/SourceCode/zjrlframwork/coreframwork/","D:/c/","D:/c/target/classes")
                .packageUpdate();

    }
}
