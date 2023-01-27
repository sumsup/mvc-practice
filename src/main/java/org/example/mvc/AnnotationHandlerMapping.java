package org.example.mvc;

import com.sun.deploy.cache.BaseLocalApplicationProperties;
import org.example.annotation.Controller;
import org.example.annotation.RequestMapping;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 여기서 Handler는 Controller를 의미.
 * HandlerAdapter는 Controller 객체를 사용하기 위한 중간 산출물.
 */
public class AnnotationHandlerMapping implements HandlerMapping{
    private final Object[] basePackage;
    private Map<HandlerKey, AnnotationHandler> handlers = new HashMap<>();

    public AnnotationHandlerMapping(Object... basePackage) {
        this.basePackage = basePackage;
    }

    public void initialize() {
        Reflections reflections = new Reflections(basePackage);

        // @Controller 에노테이션이 붙은 Controller 객체를 Heap 에서 모두 가져온다.
        Set<Class<?>> clazzesWithControllerAnnotation = reflections.getTypesAnnotatedWith(Controller.class);

        clazzesWithControllerAnnotation.forEach(clazz -> Arrays.stream(
                clazz.getDeclaredMethods()).forEach( // 그 클래스 안의 선언된 method 목록.
                        declaredMethod -> {
                            RequestMapping requestMapping = declaredMethod.getDeclaredAnnotation(RequestMapping.class);

                            // @RequestMapping이 붙은 method를 다 가져와서 HandlerKey와 Controller 클래스 정보를 담아준다.
                            Arrays.stream(requestMapping.method())
                                    .forEach(requestMethod -> handlers.put(
                                            new HandlerKey(requestMethod, requestMapping.value()),
                                            new AnnotationHandler(clazz, declaredMethod)
                                    ));
                        }));
    }

    @Override
    public Object findHandler(HandlerKey handlerKey) {
        return handlers.get(handlerKey);
    }
}