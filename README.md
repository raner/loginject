## loginject &nbsp; [![build](https://github.com/raner/loginject/workflows/build/badge.svg)](https://github.com/raner/loginject/actions/workflows/build.yml?query=branch%3Amaster) [![Maven Central](https://img.shields.io/maven-central/v/org.loginject/loginject.svg)](https://oss.sonatype.org/content/repositories/releases/org/loginject/)
The **loginject** library facilitates dependency injection for loggers.

Nowadays, many Java-based software systems use JSR-330 dependency injection provided by Spring, Guice, HK2 or Dagger to achieve loose coupling between classes.
Strangely though, when it comes to loggers, we still mostly see code like this:
```java
    private Logger logger = Logger.getLogger(getClass());
```
or, even worse:
```java
    private static Logger logger = Logger.getLogger(MyClass.class);
```
This can be observed even in projects that otherwise use dependency injection very consistently.
Instead of manually obtaining a logger instance, why aren't loggers just injected like this?
```java
    @Inject
    private Logger logger;
```
The answer, of course, is that loggers differ from other injected dependencies in a very fundamental way: the logger object that needs to be injected is different for every class, or, in other words, it depends on the injection point. Context-aware injection is not a very well supported concept for most JSR-330 implementations. For example, a singleton binding will inject the same object at each injection point, or a per-lookup binding will create a new object every time, but it is not possible to inject specific objects based on where they are injected. Also, there can only be one binding per logger type, so how could a binding describe that two different classes need to be injected with two different loggers?

**loginject** solves this exact problem, in a way that is logger-agnostic and supports different dependency injection systems through a common API.

To inject a logger, you can use standard JSR-330 annotations:
```java
class A
{
    @Inject
    private Logger logger; // will inject a logger specific to class A
}

class B
{
    @Inject
    private Logger logger; // will inject a logger specific to class B
}
```
You also need to provide **loginject** with some more detailed instructions as to what logger should be injected. For example, if you are writing a binder for the HK2 dependency injection framework you could do it like this:
```java
import java.util.logging.Logger;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import static org.loginject.LogInject.*;
import static org.loginject.LogParameter.*;

public class HK2LogBinder extends AbstractBinder
{
    @Override
    protected void configure()
    {
        install(loginject(Logger::getLogger, currentClassName()).as(Binder.class));
    }
}
```
The `install(...)` refers to the standard method from HK2's `AbstractBinder`.
The `loginject(Logger::getLogger, currentClassName())` expression provides a generic logger binding for a `java.util.logging.Logger` that is obtained via `Logger.getLogger(...)` for the class into which it is injected. Note that `currentClassName()` is not the current class in which the expression appears (which would be `HK2LogBinder` in this case) but a placeholder for the class into which the logger will be injected. At injection time, this placeholder will evaluate to different classes for different injection points. Finally, by specifying `as(Binder.class)` you are telling **loginject** that you need a binding for HK2. If you were using Guice you would be using `as(Module.class)` instead.

## FAQ

### What Maven dependencies do I need for using loginject?
Assuming that you already have all the necessary dependencies for your dependency injection framework (e.g., Spring, Guice) and for your logging framework (e.g., Log4J, SLF4J), you will need two additional dependencies for **loginject**. You will always need the common **loginject** API (```org.loginject:loginject-api```), but you will also need its specific implementation that supports the dependency injection framework you use. For example, for using **loginject** in conjunction with Guice, you would add the following dependencies:
```xml
  <dependency>
   <groupId>org.loginject</groupId>
   <artifactId>loginject-api</artifactId>
   <version>1.1.0</version>
  </dependency>
  <dependency>
   <groupId>org.loginject</groupId>
   <artifactId>loginject-guice</artifactId>
   <version>1.1.0</version>
   <scope>runtime</scope>
  </dependency>
```
Note that the second dependency is always strictly a ```runtime``` dependency: you don't need it to compile your code, but you do need it to actually run your code.
If you are using a dependency management system other than Maven (e.g., Gradle or SBT), please use the same dependency coordinates in conjunction with the appropriate syntax.

### How can I use loginject with Spring?
In addition to ```org.loginject:loginject-api```, you need to also include ```org.loginject:loginject-spring```:
```xml
  <dependency>
   <groupId>org.loginject</groupId>
   <artifactId>loginject-spring</artifactId>
   <version>1.1.0</version>
   <scope>runtime</scope>
  </dependency>
```
In your ```@Configuration``` class, you need to add the loginject bindings as a ```BeanFactoryPostProcessor```:
```java
...
import java.util.logging.Logger;
import static org.loginject.LogInject.loginject;
import static org.loginject.LogParameter.currentClassName;
...

    @Bean
    static BeanFactoryPostProcessor injectLogger()
    {
        return loginject(Logger::getLogger, currentClassName()).as(BeanFactoryPostProcessor.class);
    }
...
```
### How can I use loginject with Guice?
The necessary runtime and compile-time Maven dependencies can be found in the earlier section ["What Maven dependencies do I need for using loginject"](#user-content-what-maven-dependencies-do-i-need-for-using-loginject).

At a code level, **loginject** is used as a Guice ```Module```:
```java
        Module module = loginject(LogManager::getLogger, currentClass()).as(Module.class);
        Injector injector = Guice.createInjector(module);
```
The module can also be combined with other pre-existing Guice modules.

### What is new in loginject 1.1.0?
Several bugs and limitations were addressed in loginject 1.1.0:
* [Issue #17](https://github.com/raner/loginject/issues/17) - Spring Injection Service breaks resolution of other Spring dependencies
* [Issue #18](https://github.com/raner/loginject/issues/18) - Upgrade org.springframework:spring-core to the latest version
* [Issue #19](https://github.com/raner/loginject/issues/19) - Make LogInjectionService public
