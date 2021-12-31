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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.util.stream.StreamSupport.stream;

/**
* The {@link LogInject} class provides static methods for creating injectee-sensitive bindings for loggers. The
* resulting {@link LogInject} object's {@link #as(Class)} method will provide the actual binding object for a
* particular dependency injection framework (such as Guice, HK2, etc.).
* <p>
* {@link LogInject} allows you to bind loggers with standard JSR-330 annotations:
* <pre>
*        class TestClass
*        {
*            {@literal @}Inject
*            Logger logger;
*
*            // ...
*        }
* </pre>
* The {@link LogInject} class is agnostic of both, logging frameworks and dependency injection frameworks (and it has
* no dependencies on either). However, to provide bindings for a specific DI framework, a corresponding
* {@link LogInjectionService} needs to be installed. The available {@link LogInjectionService}s are discovered
* dynamically via Java's {@link ServiceLoader} mechanism, so it is sufficient to have the service implementation JAR
* on the class path (no additional configuration is required). No support is needed for specific logging frameworks.
* <p>
* For example, to use {@link LogInject} with the HK2 dependency injection framework {@link java.util.logging.Logger},
* you could create the following HK2 binder:
* <pre>
*        AbstractBinder binder = new AbstractBinder()
*        {
*            {@literal @}Override
*            protected void configure()
*            {
*                install(loginject(Logger::getLogger, currentClassName()).as(Binder.class));
*                addActiveDescriptor(TestClass.class); // other bindings
*                // ...
*            }
*        };
* </pre>
* <b>Known Limitations</b>
* <p>
* Currently, loginject only supports factory methods with zero, one, or two parameters.
*
* @param <_Logger_> the logger type (e.g., {@link java.util.logging.Logger})
*
* @author Mirko Raner
**/
public class LogInject<_Logger_>
{
    static enum ClassType {INTERFACE, IMPLEMENTATION}

    private ClassType type;
    private Class<LogInjectionService<?, _Logger_>> classLogInjectionService;
    private Class<_Logger_> loggerClass;
    private LogParameter<?>[] parameterTypes;
    private Function<Object[], _Logger_> loggerCreator;

    private LogInject(Class<_Logger_> loggerClass, Function<Object[], _Logger_> loggerCreator,
        LogParameter<?>... parameterTypes)
    {
        this(ClassType.INTERFACE, loggerClass, loggerCreator, parameterTypes);
    }

    @SuppressWarnings("unchecked")
    private LogInject(_Logger_ logger, Function<Object[], _Logger_> loggerCreator, LogParameter<?>... parameterTypes)
    {
        this(ClassType.IMPLEMENTATION, (Class<_Logger_>)logger.getClass(), loggerCreator, parameterTypes);
    }

    private LogInject(ClassType type, Class<_Logger_> logger, Function<Object[], _Logger_> loggerCreator,
        LogParameter<?>... parameterTypes)
    {
        this.type = type;
        this.loggerClass = logger;
        this.loggerCreator = loggerCreator;
        this.parameterTypes = parameterTypes;
        classLogInjectionService =
            new LogLiteral<LogInjectionService<?, _Logger_>>(LogInjectionService.class).getLiteral();
    }

    Class<_Logger_> getLoggerClass()
    {
        return loggerClass;
    }

    ClassType getLoggerClassType()
    {
        return type;
    }

    _Logger_ createLogger(Class<?> currentClass)
    {
        Stream<Object> parameters = Arrays.stream(parameterTypes).map(parameter -> parameter.getValue(currentClass));
System.err.println("loggerCreator=" + loggerCreator);
System.err.println("parameters=" + Arrays.stream(parameterTypes).map(parameter -> parameter.getValue(currentClass)).collect(Collectors.toList()));
try {
        _Logger_ logger =  loggerCreator.apply(parameters.toArray(Object[]::new));
System.err.println("logger=" + logger);
        return logger;
} catch (Throwable t) {t.printStackTrace();return null;}
    }

    /**
    * Creates dependency injection bindings for a specific DI framework.
    *
    * @param <_Binding_> the binding type
    * @param binding the binding class
    * @return the bindings
    * @throws LogInjectException if no implementation for the requested binding class is available
    **/
    public <_Binding_> _Binding_ as(Class<_Binding_> binding)
    {
        ServiceLoader<LogInjectionService<?, _Logger_>> serviceLoader = ServiceLoader.load(classLogInjectionService);
        Stream<LogInjectionService<?, _Logger_>> allImplementations = stream(serviceLoader.spliterator(), false);
        Predicate<LogInjectionService<?, _Logger_>> matchBinding = service -> service.supports(this, binding);
        Optional<LogInjectionService<?, _Logger_>> implementation = allImplementations.filter(matchBinding).findFirst();
        @SuppressWarnings("unchecked")
        _Binding_ bindings = (_Binding_)implementation.orElseThrow(noBindingFor(binding)).getBindings(this);
        return bindings;
    }

    /**
    * Provides a {@link LogInject} binding that binds the specified logger class to a zero-argument factory method.
    * <p>
    * Example use:
    * <pre>
    *     loginject(Logger.class, LogManager::getLogger)
    * </pre>
    * @param <_Logger_> the logger type
    * @param loggerClass the logger class
    * @param factory the logger supplier
    * @return the {@link LogInject} object presenting the abstract (framework-independent) bindings
    **/
    public static <_Logger_> LogInject<_Logger_> loginject(Class<_Logger_> loggerClass, Supplier<_Logger_> factory)
    {
        // NOTE: for some reason, Eclipse's compiler is O.K. with using the diamond operator (i.e., new LogInject<>) here,
        //       but javac says it's ambiguous and fails on the command line...
        @SuppressWarnings("unused")
        LogInject<_Logger_> loginject = new LogInject<_Logger_>(loggerClass, noParameters -> factory.get());
        return loginject;
    }

    /**
    * @deprecated use {@link #loginject(Class, Supplier)}
    * @param <_Logger_> the logger type
    * @param loggerClass the logger class
    * @param loggerSupplier the logger supplier
    * @return the {@link LogInject} object presenting the abstract (framework-independent) bindings
    **/
    @Deprecated
    public static <_Logger_> LogInject<_Logger_> logger(Class<_Logger_> loggerClass, Supplier<_Logger_> loggerSupplier)
    {
        return loginject(loggerClass, loggerSupplier);
    }

    /**
    * Provides a {@link LogInject} binding that binds the specified logger class to a one-argument factory method.
    * <p>
    * Example use:
    * <pre>
    *     loginject(Logger.class, LogManager::getLogger, currentClass())
    * </pre>
    * @param <_Logger_> the logger type
    * @param <_Parameter_> the log parameter type
    * @param loggerClass the logger class
    * @param loggerFactory the logger factory
    * @param parameter the {@link LogParameter} to be passed to the factory method
    * @return the {@link LogInject} object presenting the abstract (framework-independent) bindings
    **/
    public static <_Logger_, _Parameter_> LogInject<_Logger_> loginject(Class<_Logger_> loggerClass,
        Function<_Parameter_, _Logger_> loggerFactory, LogParameter<_Parameter_> parameter)
    {
        @SuppressWarnings("unchecked")
        Function<Object[], _Logger_> loggerFunction = oneParameter -> loggerFactory.apply((_Parameter_)oneParameter[0]);
        return new LogInject<>(loggerClass, loggerFunction, parameter);
    }

    /**
    * @deprecated use {@link #loginject(Class, Function, LogParameter)}
    * @param <_Logger_> the logger type
    * @param <_Parameter_> the log parameter type
    * @param loggerClass the logger class
    * @param loggerFactory the logger factory
    * @param parameter the {@link LogParameter} to be passed to the factory method
    * @return the {@link LogInject} object presenting the abstract (framework-independent) bindings
    **/
    @Deprecated
    public static <_Logger_, _Parameter_> LogInject<_Logger_> logger(Class<_Logger_> loggerClass,
        Function<_Parameter_, _Logger_> loggerFactory, LogParameter<_Parameter_> parameter)
    {
        return loginject(loggerClass, loggerFactory, parameter);
    }

    /**
    * Provides a {@link LogInject} binding that binds the specified logger class to a two-argument factory method.
    * <p>
    * Example use:
    * <pre>
    *     loginject(Logger.class, LogManager::getLogger, currentClass(), parameter(myMessageFactory))
    * </pre>
    * @param <_Logger_> the logger type
    * @param <_Parameter0_> the first log parameter type
    * @param <_Parameter1_> the first log parameter type
    * @param loggerClass the logger class
    * @param loggerFactory the logger factory
    * @param parameter0 the first {@link LogParameter} to be passed to the factory method
    * @param parameter1 the second {@link LogParameter} to be passed to the factory method
    * @return the {@link LogInject} object presenting the abstract (framework-independent) bindings
    **/
    public static <_Logger_, _Parameter0_, _Parameter1_> LogInject<_Logger_> loginject(Class<_Logger_> loggerClass,
        BiFunction<_Parameter0_, _Parameter1_, _Logger_> loggerFactory,
        LogParameter<_Parameter0_> parameter0, LogParameter<_Parameter1_> parameter1)
    {
        @SuppressWarnings("unchecked")
        Function<Object[], _Logger_> function =
            parameters -> loggerFactory.apply((_Parameter0_)parameters[0], (_Parameter1_)parameters[1]);
        return new LogInject<>(loggerClass, function, parameter0, parameter1);
    }

    /**
    * @deprecated use {@link #loginject(Class, BiFunction, LogParameter, LogParameter)}
    * @param <_Logger_> the logger type
    * @param <_Parameter0_> the first log parameter type
    * @param <_Parameter1_> the first log parameter type
    * @param loggerClass the logger class
    * @param loggerFactory the logger factory
    * @param parameter0 the first {@link LogParameter} to be passed to the factory method
    * @param parameter1 the second {@link LogParameter} to be passed to the factory method
    * @return the {@link LogInject} object presenting the abstract (framework-independent) bindings
    **/
    @Deprecated
    public static <_Logger_, _Parameter0_, _Parameter1_> LogInject<_Logger_> logger(Class<_Logger_> loggerClass,
        BiFunction<_Parameter0_, _Parameter1_, _Logger_> loggerFactory, LogParameter<_Parameter0_> parameter0,
        LogParameter<_Parameter1_> parameter1)
    {
        return loginject(loggerClass, loggerFactory, parameter0, parameter1);
    }

    /**
    * Provides a {@link LogInject} binding that binds a zero-argument logger factory method. The logger type is
    * inferred from the factory method.
    * <p>
    * Example use:
    * <pre>
    *     loginject(LogManager::getLogger)
    * </pre>
    * @param <_Logger_> the logger type
    * @param <_Parameter_> the log parameter type
    * @param loggerFactory the logger supplier
    * @return the {@link LogInject} object presenting the abstract (framework-independent) bindings
    **/
    public static <_Logger_, _Parameter_> LogInject<_Logger_> loginject(Supplier<_Logger_> loggerFactory)
    {
        return new LogInject<>(loggerFactory.get(), noParameters -> loggerFactory.get());
    }

    /**
    * Provides a {@link LogInject} binding that binds a one-argument logger factory method. The logger type is
    * inferred from the factory method.
    * <p>
    * Example use:
    * <pre>
    *     loginject(LogManager::getLogger, currentClass())
    * </pre>
    * @param <_Logger_> the logger type
    * @param <_Parameter_> the log parameter type
    * @param loggerFactory the logger factory
    * @param parameter the {@link LogParameter} to be passed to the factory method
    * @return the {@link LogInject} object presenting the abstract (framework-independent) bindings
    **/
    public static <_Logger_, _Parameter_> LogInject<_Logger_> loginject(Function<_Parameter_, _Logger_> loggerFactory,
        LogParameter<_Parameter_> parameter)
    {
        _Logger_ prototypeLogger = loggerFactory.apply(parameter.getValue(LogInject.class));
        @SuppressWarnings("unchecked")
        Function<Object[], _Logger_> function = parameters -> loggerFactory.apply((_Parameter_)parameters[0]);
        return new LogInject<>(prototypeLogger, function, parameter);
    }
    
    /**
    * Provides a {@link LogInject} binding that binds a two-argument logger factory method. The logger type is
    * inferred from the factory method.
    * <p>
    * Example use:
    * <pre>
    *     loginject(LogManager::getLogger, currentClass(), parameter(myMessageFactory))
    * </pre>
    * @param <_Logger_> the logger type
    * @param <_Parameter0_> the first log parameter type
    * @param <_Parameter1_> the first log parameter type
    * @param loggerFactory the logger factory
    * @param parameter0 the first {@link LogParameter} to be passed to the factory method
    * @param parameter1 the second {@link LogParameter} to be passed to the factory method
    * @return the {@link LogInject} object presenting the abstract (framework-independent) bindings
    **/
    public static <_Logger_, _Parameter0_, _Parameter1_> LogInject<_Logger_> loginject(
        BiFunction<_Parameter0_, _Parameter1_, _Logger_> loggerFactory,
        LogParameter<_Parameter0_> parameter0, LogParameter<_Parameter1_> parameter1)
    {
        _Logger_ prototypeLogger =
            loggerFactory.apply(parameter0.getValue(LogInject.class), parameter1.getValue(LogInject.class));
        @SuppressWarnings("unchecked")
        Function<Object[], _Logger_> function =
            parameters -> loggerFactory.apply((_Parameter0_)parameters[0], (_Parameter1_)parameters[1]);
        return new LogInject<>(prototypeLogger, function, parameter0, parameter1);
    }

    private Supplier<LogInjectException> noBindingFor(Class<?> bindingClass)
    {
        return () -> new LogInjectException("no implementation present that can return a " + bindingClass.getName());
    }
}
