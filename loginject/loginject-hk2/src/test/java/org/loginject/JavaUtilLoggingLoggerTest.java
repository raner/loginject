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
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.loginject.LogInject.logger;
import static org.loginject.LogParameter.constantString;
import static org.loginject.LogParameter.currentClassName;

public class JavaUtilLoggingLoggerTest
{
    static class TestClass
    {
        @Inject
        Logger injectedLogger;
    }

    @Rule
    public TestName testName = new TestName();

    @Test
    public void testGetLoggerWithoutParameters()
    {
        AbstractBinder binder = new AbstractBinder()
        {
            @Override
            protected void configure()
            {
                install(logger(Logger.class, Logger::getAnonymousLogger).as(Binder.class));
                addActiveDescriptor(TestClass.class);
            }
        };
        ServiceLocator serviceLocator = ServiceLocatorUtilities.bind(testName.getMethodName(), binder);
        TestClass service = serviceLocator.getService(TestClass.class);
        assertNull(service.injectedLogger.getName());
    }

    @Test
	public void testGetLoggerWithStringParameter()
	{
		AbstractBinder binder = new AbstractBinder()
		{
			@Override
			protected void configure()
			{
				install(logger(Logger.class, Logger::getLogger, currentClassName()).as(Binder.class));
				addActiveDescriptor(TestClass.class);
			}
		};
		ServiceLocator serviceLocator = ServiceLocatorUtilities.bind(testName.getMethodName(), binder);
		TestClass service = serviceLocator.getService(TestClass.class);
		assertEquals(TestClass.class.getName(), service.injectedLogger.getName());
	}

    @Test
    public void testGetLoggerWithClassNameAndBundleNameParameters()
    {
        final String bundleName = "ResourceBundle";
        AbstractBinder binder = new AbstractBinder()
        {
            @Override
            protected void configure()
            {
                install(logger(Logger.class, Logger::getLogger, currentClassName(), constantString(bundleName)).as(Binder.class));
                addActiveDescriptor(TestClass.class);
            }
        };
        ServiceLocator serviceLocator = ServiceLocatorUtilities.bind(testName.getMethodName(), binder);
        TestClass service = serviceLocator.getService(TestClass.class);
        String[] expected = {TestClass.class.getName(), bundleName};
        String[] actual = {service.injectedLogger.getName(), service.injectedLogger.getResourceBundleName()};
        assertArrayEquals(expected, actual);
    }
}
