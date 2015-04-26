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

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
* The {@link LogInjectionService} implementation for Dagger ended up being the fewest lines of code, but it also
* turned out to be the lamest and hackiest solution of all. Dagger supports no customization of the injection process
* whatsoever. Furthermore, Dagger's unique approach of using annotation processing during compilation means that
* Dagger {@link dagger.Module Module}s cannot be created as objects in memory, like for HK2 and Guice. As Dagger's
* instrumentation needs to process the module file, there is no support for modules that do not exist as source and
* class files.
* <p/>
* To bring loginject's value to Dagger, {@link DaggerLogInjectionService} simply determines the injection point based
* on the call stack at the time when the logger is created. This is not a great solution, because it makes some
* assumptions about Dagger's generated class names.
* <p/>
* To establish a generic binding for loggers in Dagger, follow this example code:
* <pre>
*    {@literal @}Module(injects=TestClass.class)
*    public class DaggerModule
*    {
*        {@literal @}Provides
*        Logger provideLogger()
*        {
*            return loginject(Logger::getLogger, currentClassName()).as(Logger.class);
*        }
*    }
* </pre>
* Note that unlike the binding for other DI frameworks, loginject in this case returns the logger itself, rather than
* a binder or a module. This also means that both type parameters of {@link LogInjectionService} remain unbound for
* {@link DaggerLogInjectionService} (it implements {@code LogInjectionService<_Logger_, _Logger_>}, rather than
* something like {@code LogInjectionService<Binder, _Logger_>}).
* <p/>
*
* @param <_Logger_> the logger type
*
* @author Mirko Raner
**/
public class DaggerLogInjectionService<_Logger_> implements LogInjectionService<_Logger_, _Logger_>
{
    private final static String INJECT_ADAPTER = "$$InjectAdapter";

    @Override
    public boolean supports(LogInject<_Logger_> loginject, Class<?> bindingType)
    {
        return bindingType.isAssignableFrom(loginject.getLoggerClass());
    }

    @Override
    public _Logger_ getBindings(LogInject<_Logger_> loginject)
    {
        Stream<StackTraceElement> stack = Stream.of(new Throwable().getStackTrace());
        Predicate<StackTraceElement> injectAdapter = frame -> frame.getClassName().endsWith(INJECT_ADAPTER);
        Optional<StackTraceElement> injectionCall = stack.filter(injectAdapter).findFirst();
        String className = injectionCall.get().getClassName();
        try
        {
            Class<?> injectee = Class.forName(className.substring(0, className.length()-INJECT_ADAPTER.length()));
            return loginject.createLogger(injectee);
        }
        catch (ClassNotFoundException noSuchClass)
        {
            throw new NoClassDefFoundError(noSuchClass.getMessage());
        }
    }
}
