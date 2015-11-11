package org.loginject;

import java.util.logging.Logger;
import javax.inject.Inject;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.junit.Assert.assertEquals;
import static org.loginject.LogInject.loginject;

public class JavaUtilLoggingLoggerTest
{
	@Configuration
	static class Binder
	{
		@Bean
		TestClass getTestClass()
		{
			return new TestClass();
		}

		@Bean
		static BeanFactoryPostProcessor injectLogger()
		{
			return loginject(Logger::getLogger, LogParameter.currentClassName()).as(BeanFactoryPostProcessor.class);
		}
	}

	@Configuration
	static class BinderForSubClass
	{	    
	    @Bean
	    SubClass getSubClass()
	    {
	        return new SubClass();
	    }

	    @Bean
	    static BeanFactoryPostProcessor getBFPP() {
	        return loginject(Logger::getLogger, LogParameter.currentClassName()).as(BeanFactoryPostProcessor.class);
	    }
	}

	static class TestClass
    {
        @Inject
        Logger injectedLogger;
    }

    static class SubClass extends TestClass {}

    @Test
    public void testGetLogger()
    {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Binder.class))
        {
            TestClass service = context.getBean(TestClass.class);
            assertEquals(TestClass.class.getName(), service.injectedLogger.getName());
        }
    }

    @Test
    public void testGetLoggerForSubClass()
    {
    	try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BinderForSubClass.class))
    	{
    	    TestClass service = context.getBean(SubClass.class);
    	    assertEquals(SubClass.class.getName(), service.injectedLogger.getName());
    	}
    }
}
