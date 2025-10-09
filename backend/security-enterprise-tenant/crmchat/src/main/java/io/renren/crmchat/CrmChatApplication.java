package io.renren.crmchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * CRM Chat Customer Service System
 * PHP API Compatible Implementation
 *
 * @author CRMChat Team
 */
@SpringBootApplication
@ComponentScan(basePackages = {"io.renren.crmchat", "io.renren.common"})
public class CrmChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrmChatApplication.class, args);
    }
}
