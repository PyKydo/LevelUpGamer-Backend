package com.levelupgamer.config;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

@Configuration
public class ValidationConfig {

    @Bean
    public LocalValidatorFactoryBean validator(final AutowireCapableBeanFactory autowireCapableBeanFactory) {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.setConstraintValidatorFactory(new SpringConstraintValidatorFactory(autowireCapableBeanFactory));
        return factoryBean;
    }
}
