package com.showtime.demo;

import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** Small endpoints used only to demonstrate syllabus concepts. */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final HibernateDemoService hibernateDemo;
    private final ApplicationContext context;     // injected by Spring = dependency injection

    public DemoController(HibernateDemoService hibernateDemo, ApplicationContext context) {
        this.hibernateDemo = hibernateDemo;
        this.context = context;
    }

    /** GET /api/demo/hibernate : SessionFactory/Session/Transaction + inheritance mapping. */
    @GetMapping("/hibernate")
    public Map<String, Object> hibernate() {
        return hibernateDemo.run();
    }

    /** GET /api/demo/context : BeanFactory / ApplicationContext facts. */
    @GetMapping("/context")
    public Map<String, Object> context() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("applicationContextType", context.getClass().getSimpleName());
        out.put("beanCount", context.getBeanDefinitionCount());
        out.put("hasBookingServiceBean", context.containsBean("bookingService"));
        out.put("bookingServiceIsSingleton", context.isSingleton("bookingService"));
        out.put("beanFactoryType", context.getAutowireCapableBeanFactory().getClass().getSimpleName());
        return out;
    }
}
