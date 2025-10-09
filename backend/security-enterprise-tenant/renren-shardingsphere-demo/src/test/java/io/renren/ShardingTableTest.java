package io.renren;

import io.renren.dao.UserLogDao;
import io.renren.entity.UserLogEntity;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

/**
 * 测试 ShardingSphere 分表
 *
 * @author Mark sunlightcs@gmail.com
 */
@SpringBootTest
public class ShardingTableTest {
    @Resource
    private UserLogDao userLogDao;

    @Test
    public void shardingTest() {
        UserLogEntity log = new UserLogEntity();
        //log.setId(1L);
        log.setName("test");
        log.setContent("测试");
        log.setCreateTime(LocalDateTime.now());

        userLogDao.insert(log);
    }
}
