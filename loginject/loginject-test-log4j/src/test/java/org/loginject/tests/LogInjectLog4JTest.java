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

package org.loginject.tests;

import javax.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import static org.junit.Assert.assertEquals;
import static org.loginject.LogInject.logger;
import static org.loginject.LogParameter.currentClass;

public class LogInjectLog4JTest
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
        AbstractBinder binder = new AbstractBinder()
        {
            @Override
            protected void configure()
            {
                install(logger(Logger.class, LogManager::getLogger, currentClass()).as(Binder.class));
                addActiveDescriptor(TestClass.class);
            }
        };
        ServiceLocator serviceLocator = ServiceLocatorUtilities.bind(testName.getMethodName(), binder);
        TestClass service = serviceLocator.getService(TestClass.class);
        assertEquals(TestClass.class.getName(), service.logger.getName());
    }
}
