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

import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.loginject.LogInject.loginject;
import static org.loginject.LogParameter.currentClass;
import static org.loginject.LogParameter.parameter;

public class GuiceLoggerTest
{
    static class TestClass
    {
        @Inject
        Logger injectedLogger;
    }

    @Rule
    public TestName testName = new TestName();

    @Test
    public void testLoggerForClass()
    {
        Module module = loginject(Logger.class, LogManager::getLogger, currentClass()).as(Module.class);
        Injector injector = Guice.createInjector(module);
        TestClass service = injector.getInstance(TestClass.class);
        assertEquals(TestClass.class.getName(), service.injectedLogger.getName());
    }

    @Test
    public void testGetLoggerForClassWithInferredLoggerType()
    {
        Module module = loginject(LogManager::getLogger, currentClass()).as(Module.class);
        Injector injector = Guice.createInjector(module);
        TestClass service = injector.getInstance(TestClass.class);
        assertEquals(TestClass.class.getName(), service.injectedLogger.getName());
    }

    @Test
    public void testLoggerWithNoArguments()
    {
        Module module = loginject(Logger.class, LogManager::getLogger).as(Module.class);
        Injector injector = Guice.createInjector(module);
        TestClass service = injector.getInstance(TestClass.class);
        assertNotNull(service.injectedLogger.getName());
        // NOTE: logger name will not be the TestClass name, but refer to a lambda expression
    }

    @Test
    public void testGetLoggerWithNoArgumentsWithInferredLoggerType()
    {
        Module module = loginject(LogManager::getLogger).as(Module.class);
        Injector injector = Guice.createInjector(module);
        TestClass service = injector.getInstance(TestClass.class);
        assertNotNull(service.injectedLogger.getName());
        // NOTE: logger name will not be the TestClass name, but refer to a lambda expression
    }

    @Test
    public void testLoggerWithTwoArguments()
    {
        final MessageFactory MF = new MessageFormatMessageFactory();
        String name = testName.getMethodName();
        Module module = loginject(Logger.class, LogManager::getLogger, parameter(name), parameter(MF)).as(Module.class);
        Injector injector = Guice.createInjector(module);
        TestClass service = injector.getInstance(TestClass.class);
        assertEquals(name, service.injectedLogger.getName());
        assertEquals(MF, service.injectedLogger.getMessageFactory());
    }

    @Test
    public void testGetLoggerWithTwoArgumentsWithInferredLoggerType()
    {
        final MessageFactory MF = new MessageFormatMessageFactory();
        String name = testName.getMethodName();
        Module module = loginject(LogManager::getLogger, parameter(name), parameter(MF)).as(Module.class);
        Injector injector = Guice.createInjector(module);
        TestClass service = injector.getInstance(TestClass.class);
        assertEquals(name, service.injectedLogger.getName());
        assertEquals(MF, service.injectedLogger.getMessageFactory());
    }
}
