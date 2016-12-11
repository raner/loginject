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
import org.junit.Test;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import static org.junit.Assert.assertEquals;
import static org.loginject.LogInject.loginject;
import static org.loginject.LogParameter.currentClass;

public class DaggerLogInjectLog4JTest
{
    static class TestClass
    {
        @Inject
        Logger logger;
    }

    @Module(injects=TestClass.class)
    public static class DaggerModule
    {
        @Provides
        Logger provideLogger()
        {
            return loginject(LogManager::getLogger, currentClass()).as(Logger.class);
        }
    }

    @Test
    public void testInferredLog4J()
    {
        ObjectGraph objectGraph = ObjectGraph.create(new DaggerModule());
        TestClass service = objectGraph.get(TestClass.class);
        assertEquals(TestClass.class.getName(), service.logger.getName());
    }
}