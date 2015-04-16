## loginject &nbsp; [![Build Status](https://travis-ci.org/raner/loginject.svg?branch=master)](https://travis-ci.org/raner/loginject)
The **loginject** library facilitates dependency injection for loggers.

Nowadays, many Java-based software systems use JSR-330 dependency injection provided by Guice, HK2 or Dagger to achieve loose coupling between classes.
Strangely though, when it comes to loggers, we still mostly see code like this:
```
    private Logger logger = Logger.getLogger(getClass());
```
or, even worse:
```
    private static Logger logger = Logger.getLogger(MyClass.class);
```
This can be observed even in projects that otherwise use dependency injection very consistently.
Instead of manually obtaining a logger instance, why aren't loggers just injected like this?
```
    @Inject
    private Logger logger;
```
The answer, of course, is that loggers differ from other injected dependencies in a very fundamental way: the logger object that needs to be injected is different for every class, or, in other words, it depends on the injection point. Context-aware injection is not a very well supported concept for most JSR-330 implementations. For example, a singleton binding will inject the same object at each injection point, or a per-lookup binding will create a new object every time, but it is not possible to inject specific objects based on where they are injected. Also, there can only be one binding per logger type, so how could a binding describe that two different classes need to be injected with two different loggers?

**loginject** solves this exact problem, in a way that is logger-agnostic and supports different dependency injection systems through a common API.

To inject a logger, you can use standard JSR-330 annotations:
```
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
```
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
        install(logger(Logger.class, Logger::getLogger, currentClassName()).as(Binder.class));
    }
}
```
The `install(...)` refers to the standard method from HK2's `AbstractBinder`.
The `logger(Logger.class, Logger::getLogger, currentClassName())` expression provides a generic logger binding for a `java.util.logging.Logger` that is obtained via `Logger.getLogger(...)` for the class into which it is injected. Note that `currentClassName()` is not the current class in which the expression appears (which would be `HK2LogBinder` in this case) but a placeholder for the class into which the logger will be injected. At injection time, this placeholder will evaluate to different classes for different injection points. Finally, by specifying `as(Binder.class)` you are telling **loginject** that you need a binding for HK2. If you were using Guice you would be using `as(Module.class)` instead.


