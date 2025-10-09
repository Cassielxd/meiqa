package io.renren.jimureport;

import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.springframework.stereotype.Service;

@Service
public class JimuReportTokenService implements JmReportTokenServiceI {

    @Override
    public String getUsername(String s) {
        return "admin";
    }

    @Override
    public String[] getRoles(String s) {
        return new String[]{"admin", "lowdeveloper", "dbadeveloper"};
    }

    @Override
    public Boolean verifyToken(String s) {
        return true;
    }

    @Override
    public String[] getPermissions(String token) {
        //drag:datasource:testConnection   仪表盘数据库连接测试
        //onl:drag:clear:recovery          清空回收站
        //drag:analysis:sql                SQL解析
        //drag:design:getTotalData         仪表盘对Online表单展示数据
        return new String[]{"drag:datasource:testConnection", "onl:drag:clear:recovery", "drag:analysis:sql", "drag:design:getTotalData"};
    }
}