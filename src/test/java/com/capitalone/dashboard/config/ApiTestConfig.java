package com.capitalone.dashboard.config;


import com.capitalone.dashboard.auth.AuthProperties;
import com.capitalone.dashboard.settings.ApiSettings;
import com.capitalone.dashboard.util.URLConnectionFactory;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.Order;


/**
 * Spring context configuration for Testing purposes
 */
@Order(1)
@Configuration
@ComponentScan(basePackages ={"com.capitalone.dashboard.service", "com.capitalone.dashboard.model"}, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value=ApiSettings.class))
public class ApiTestConfig {

    @Bean
    public ApiSettings settings() {

        return new ApiSettings();
    }

    @Bean
    public AuthProperties authProperties(){
        return  Mockito.mock(AuthProperties.class);
    }
    @Bean
    public URLConnectionFactory urlConnectionFactory(){
        return Mockito.mock(URLConnectionFactory.class);
    }

}
