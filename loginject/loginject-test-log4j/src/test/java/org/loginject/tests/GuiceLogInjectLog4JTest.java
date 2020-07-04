//                                                                          //
// Copyright 2015 - 2020 Mirko Raner                                        //
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

package org.loginject.tests;

import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import static org.junit.Assert.assertEquals;
import static org.loginject.LogInject.loginject;
import static org.loginject.LogParameter.currentClass;

public class GuiceLogInjectLog4JTest
{
    static class TestClass
    {
        @Inject
        Logger logger;
    }

    @Rule
    public TestName testName = new TestName();

    @Test
    public void testLog4J()
    {
        Module logger = loginject(LogManager::getLogger, currentClass()).as(Module.class);
        Module binder = new AbstractModule()
        {
            @Override
            protected void configure()
            {
                install(logger);
            }
        };
        Injector injector = Guice.createInjector(binder);
        TestClass service = injector.getInstance(TestClass.class);
        assertEquals(TestClass.class.getName().replace('$', '.'), service.logger.getName());
    }
    
    @Test
    public void testInferredLog4J()
    {
        Module logger = loginject(LogManager::getLogger, currentClass()).as(Module.class);
        Module binder = new AbstractModule()
        {
            @Override
            protected void configure()
            {
                install(logger);
            }
        };
        Injector injector = Guice.createInjector(binder);
        TestClass service = injector.getInstance(TestClass.class);
        assertEquals(TestClass.class.getName().replace('$', '.'), service.logger.getName());
    }
}
