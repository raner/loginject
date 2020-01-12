//                                                                          //
// Copyright 2020 Mirko Raner                                               //
//                                                                          //
// Licensed under the Apache License, Version 2.0 (the "License");          //
// you may not use this file except in compliance with the License.         //
// You may obtain a copy of the License at                                  //
//                                                                          //
//     http://www.apache.org/licenses/LICENSE-2.0                           //
//                                                                          //
// Unless required by applicable law or agreed to in writing, software      //
// distributed under the License is distributed on an "AS IS" BASIS,        //
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. //
// See the License for the specific language governing permissions and      //
// limitations under the License.                                           //
//                                                                          //

package org.loginject;

import java.beans.PropertyDescriptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;

/**
 * The {@link SpringLogInjectionService} implements log injection for the Spring Framework. It exposes logger
 * bindings as a Spring {@link BeanFactoryPostProcessor}.
 *
 * @param <_Logger_> the logger type
 *
 * @author Mirko Raner
 */
public class SpringLogInjectionService<_Logger_> implements LogInjectionService<BeanFactoryPostProcessor, _Logger_>
{
    @Override
    public BeanFactoryPostProcessor getBindings(LogInject<_Logger_> logInject)
    {
        return new BeanFactoryPostProcessor()
        {
            ThreadLocal<Object> injectee = new ThreadLocal<>();

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
            {
                DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory)beanFactory;
                AutowireCandidateResolver defaultResolver = defaultListableBeanFactory.getAutowireCandidateResolver();
                AutowireCandidateResolver resolver = new ContextAnnotationAutowireCandidateResolver()
                {
                    @Override
                    public Object getSuggestedValue(DependencyDescriptor descriptor)
                    {
                        if (descriptor.getDependencyType().equals(logInject.getLoggerClass()))
                        {
                            return logInject.createLogger(injectee.get().getClass());
                        }
                        return defaultResolver.getSuggestedValue(descriptor);
                    }
                };
                AutowiredAnnotationBeanPostProcessor beanPostProcessor = new AutowiredAnnotationBeanPostProcessor()
                {
                    @Override
                    public PropertyValues postProcessPropertyValues(PropertyValues values, PropertyDescriptor[] descriptors,
                        Object bean, String beanName) throws BeansException
                    {
                        injectee.set(bean);
                        return super.postProcessPropertyValues(values, descriptors, bean, beanName);
                    }
                };
                beanPostProcessor.setBeanFactory(defaultListableBeanFactory);
                defaultListableBeanFactory.addBeanPostProcessor(beanPostProcessor);
                defaultListableBeanFactory.setAutowireCandidateResolver(resolver);
            }
        };
    }
}
