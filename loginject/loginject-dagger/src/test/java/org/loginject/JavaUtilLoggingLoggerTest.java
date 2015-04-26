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
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.loginject.LogInject.loginject;
import static org.loginject.LogParameter.constantString;
import static org.loginject.LogParameter.currentClassName;

public class JavaUtilLoggingLoggerTest
{
    private final static String BUNDLE_NAME = "ResourceBundle";

    @Module(injects={TestClass1.class, TestClass2.class})
    public static class GetLoggerStringModule
    {
        @Provides
        Logger provideLogger()
        {
            return loginject(Logger::getLogger, currentClassName()).as(Logger.class);
        }
    }

    @Module(injects={TestClass1.class, TestClass2.class})
    public static class GetLoggerStringStringModule
    {
        @Provides
        Logger provideLogger()
        {
            return loginject(Logger::getLogger, currentClassName(), constantString(BUNDLE_NAME)).as(Logger.class);
        }
    }
    
    static class TestClass1
    {
        @Inject
        Logger injectedLogger;
    }
    
    static class TestClass2
    {
        @Inject
        Logger injectedLogger;
    }
    
    @Test
    public void testGetInferredLoggerWithStringParameter1()
    {
        ObjectGraph objectGraph = ObjectGraph.create(new GetLoggerStringModule());
        TestClass1 service = objectGraph.get(TestClass1.class);
        assertEquals(TestClass1.class.getName(), service.injectedLogger.getName());
    }
    
    @Test
    public void testGetInferredLoggerWithStringParameter2()
    {
        ObjectGraph objectGraph = ObjectGraph.create(new GetLoggerStringModule());
        TestClass2 service = objectGraph.get(TestClass2.class);
        assertEquals(TestClass2.class.getName(), service.injectedLogger.getName());
    }

    @Test
    public void testGetLoggerWithClassNameAndBundleNameParameters()
    {
        ObjectGraph objectGraph = ObjectGraph.create(new GetLoggerStringStringModule());
        TestClass1 service = objectGraph.get(TestClass1.class);
        String[] expected = {TestClass1.class.getName(), BUNDLE_NAME};
        String[] actual = {service.injectedLogger.getName(), service.injectedLogger.getResourceBundleName()};
        assertArrayEquals(expected, actual);
    }
}
