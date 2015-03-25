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

import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import static java.util.stream.StreamSupport.stream;

public class LogInject<_Logger_>
{
	private Class<LogInjectionService<?, _Logger_>> classLogInjectionService;
	private Class<_Logger_> loggerClass;
	private LogParameter<?>[] parameterTypes;
    private Function<Object[], _Logger_> loggerCreator;

	private LogInject(Class<_Logger_> loggerClass, Function<Object[], _Logger_> loggerCreator, LogParameter<?>... parameterTypes)
	{
	    this.loggerClass = loggerClass;
        this.loggerCreator = loggerCreator;
        this.parameterTypes = parameterTypes;
        classLogInjectionService = new LogLiteral<LogInjectionService<?, _Logger_>>(LogInjectionService.class).getLiteral();
	}

	Class<_Logger_> getLoggerClass()
	{
	    return loggerClass;
	}

	_Logger_ createLogger(Class<?> currentClass)
	{
	    Stream<Object> parameters = Arrays.stream(parameterTypes).map(parameter -> parameter.getValue(currentClass));
	    return loggerCreator.apply(parameters.toArray(Object[]::new));
	}

	public <_Binding_> _Binding_ as(Class<_Binding_> binding)
	{
		ServiceLoader<LogInjectionService<?, _Logger_>> serviceLoader = ServiceLoader.load(classLogInjectionService);
		Stream<LogInjectionService<?, _Logger_>> allImplementations = stream(serviceLoader.spliterator(), false);
		Optional<LogInjectionService<?, _Logger_>> implementation = allImplementations.filter(service -> service.getBindingType().equals(binding)).findFirst();
		@SuppressWarnings("unchecked") _Binding_ bindings = (_Binding_)implementation.orElseThrow(noBindingFor(binding)).getBindings(this);
		return bindings;
	}

	public static <_Logger_> LogInject<_Logger_> logger(Class<_Logger_> loggerClass, Supplier<_Logger_> loggerSupplier)
	{
		return new LogInject<_Logger_>(loggerClass, noParameters -> loggerSupplier.get());
	}

	public static <_Logger_,_Parameter_> LogInject<_Logger_> logger
	(
	    Class<_Logger_> loggerClass,
	    Function<_Parameter_, _Logger_> loggerFactory,
	    LogParameter<_Parameter_> parameter
	)
	{
		@SuppressWarnings("unchecked")
        Function<Object[], _Logger_> loggerFunction = oneParameter -> loggerFactory.apply((_Parameter_)oneParameter[0]);
        return new LogInject<_Logger_>(loggerClass, loggerFunction, parameter);
	}

	public static <_Logger_, _Parameter0_, _Parameter1_> LogInject<_Logger_> logger
	(
	    Class<_Logger_> loggerClass,
	    BiFunction<_Parameter0_, _Parameter1_, _Logger_> loggerFactory,
	    LogParameter<_Parameter0_> parameterType1,
	    LogParameter<_Parameter1_> parameterType2
	)
	{
		@SuppressWarnings("unchecked")
        Function<Object[], _Logger_> function = parameters -> loggerFactory.apply((_Parameter0_)parameters[0], (_Parameter1_)parameters[1]);
        return new LogInject<_Logger_>(loggerClass, function, parameterType1, parameterType2);
	}

	private Supplier<LogInjectException> noBindingFor(Class<?> bindingClass)
	{
	    return () -> new LogInjectException("no implementation present that can return a " + bindingClass.getName());
	}
}
