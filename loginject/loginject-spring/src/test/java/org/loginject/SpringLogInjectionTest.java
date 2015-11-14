//                                                                          //
// Copyright 2015 Mirko Raner                                               //
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
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.loginject.LogInject.loginject;
import static org.loginject.LogParameter.currentClassName;

public class SpringLogInjectionTest
{
    @Configuration
    static class Binder
    {
        @Bean
        TestClass getTestClass()
        {
            return new TestClass();
        }

        @Bean
        OtherClass getOtherClass()
        {
            return new OtherClass();
        }

        @Bean
        static BeanFactoryPostProcessor injectLogger()
        {
            return loginject(Logger::getLogger, currentClassName()).as(BeanFactoryPostProcessor.class);
        }
    }

    static class TestClass
    {
        @Inject
        Logger injectedLogger;

        @Inject
        OtherClass otherClass;
    }

    static class OtherClass
    {
        // No additional fields or methods...
    }

    @Test
    public void testGetLogger()
    {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Binder.class))
        {
            TestClass service = context.getBean(TestClass.class);
            assertEquals(TestClass.class.getName(), service.injectedLogger.getName());
            assertNotNull(service.otherClass);
        }
    }
}
