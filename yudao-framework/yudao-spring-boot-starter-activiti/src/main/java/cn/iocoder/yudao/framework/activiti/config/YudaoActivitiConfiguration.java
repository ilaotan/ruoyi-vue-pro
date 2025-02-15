package cn.iocoder.yudao.framework.activiti.config;

import cn.iocoder.yudao.framework.activiti.core.web.ActivitiWebFilter;
import cn.iocoder.yudao.framework.common.enums.WebFilterOrderEnum;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YudaoActivitiConfiguration {

    /**
     * Activiti 流程图的生成器。目前管理后台的流程图 svg，通过它绘制生成。
     */
    @Bean
    public ProcessDiagramGenerator processDiagramGenerator() {
        return new DefaultProcessDiagramGenerator();
    }

    /**
     * ProcessEngineConfigurationConfigurer 实现类，设置使用 MyBatis SqlSessionFactory
     */
    @Bean
    public ProcessEngineConfigurationConfigurer processEngineConfigurationConfigurer(SqlSessionFactory sqlSessionFactory) {
        return springProcessEngineConfiguration -> springProcessEngineConfiguration.setSqlSessionFactory(sqlSessionFactory);
    }

    @Bean
    public FilterRegistrationBean<ActivitiWebFilter> activitiWebFilter() {
        FilterRegistrationBean<ActivitiWebFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ActivitiWebFilter());
        registrationBean.setOrder(WebFilterOrderEnum.ACTIVITI_FILTER);
        return registrationBean;
    }

}
