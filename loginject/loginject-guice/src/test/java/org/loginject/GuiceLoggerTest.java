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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.Test;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import static org.junit.Assert.assertEquals;
import static org.loginject.LogInject.logger;
import static org.loginject.LogParameter.currentClass;

public class GuiceLoggerTest
{
    static class TestClass
    {
        @Inject
        Logger injectedLogger;
    }

    @Test
    public void testGetAnonymousLogger()
    {
        Module module = logger(Logger.class, LogManager::getLogger, currentClass()).as(Module.class);
        Injector injector = Guice.createInjector(module);
        TestClass service = injector.getInstance(TestClass.class);
        assertEquals(TestClass.class.getName(), service.injectedLogger.getName());
    }
}
