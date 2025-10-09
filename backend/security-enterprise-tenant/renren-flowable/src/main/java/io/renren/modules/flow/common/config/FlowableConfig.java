package io.renren.modules.flow.common.config;

import io.renren.modules.flow.common.factory.RenActivityBehaviorFactory;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;

/**
 * 流程配置
 *
 * @author Mark sunlightcs@gmail.com
 */
@Configuration(proxyBeanMethods = false)
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    @Override
    public void configure(SpringProcessEngineConfiguration configuration) {
        configuration.setActivityFontName("宋体");
        configuration.setLabelFontName("宋体");
        configuration.setAnnotationFontName("宋体");
        configuration.setActivityBehaviorFactory(new RenActivityBehaviorFactory());
    }
}
