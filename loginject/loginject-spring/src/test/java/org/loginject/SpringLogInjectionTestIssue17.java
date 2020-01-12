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

import java.util.logging.Logger;
import javax.inject.Inject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import static org.junit.Assert.assertArrayEquals;
import static org.loginject.LogInject.loginject;
import static org.loginject.LogParameter.currentClassName;

public class SpringLogInjectionTestIssue17
{
    @Configuration
    @PropertySource("classpath:/test.properties")
    static class Binder
    {
        @Bean
        Config getTestClass()
        {
            return new Config();
        }

        @Bean 
        static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
        {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        static BeanFactoryPostProcessor injectLogger()
        {
            return loginject(Logger::getLogger, currentClassName()).as(BeanFactoryPostProcessor.class);
        }
    }

    static class Config
    {
      @Value("${host.url}")
      String hostUrl;

      @Inject
      Logger logger;
    }

    @Test
    public void testGetLogger()
    {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Binder.class))
        {
            Config service = context.getBean(Config.class);
            String[] expected = {Config.class.getName(), "https://loginject.org"};
            String[] actual = {service.logger.getName(), service.hostUrl};
            assertArrayEquals(expected, actual);
        }
    }
}
