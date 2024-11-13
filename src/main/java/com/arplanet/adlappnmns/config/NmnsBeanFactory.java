package com.arplanet.adlappnmns.config;

import com.arplanet.adlappnmns.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class NmnsBeanFactory {

    private final ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    public <T> NmnsService<T> getNmnsService(String serviceName) {
        try {
            Object bean = applicationContext.getBean(serviceName);

            if (bean instanceof NmnsService) {
                return (NmnsService<T>) bean;
            }

            throw new IllegalArgumentException("Bean is not an instance of NmnsService: " + serviceName);

        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalArgumentException("Unknown service: " + serviceName, e);
        }
    }
}
