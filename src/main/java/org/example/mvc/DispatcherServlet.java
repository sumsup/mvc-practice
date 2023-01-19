package org.example.mvc;

import org.example.view.JspViewResolver;
import org.example.view.ModelAndView;
import org.example.view.View;
import org.example.view.ViewResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@WebServlet("/")
public class DispatcherServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(DispatcherServlet.class);
    private List<HandlerMapping> handlerMappings;
    private List<ViewResolver> viewResolvers;
    private List<HandlerAdapter> handlerAdapters;

    @Override
    public void init() throws ServletException {
        /*
         Handler란? 처리자.
         여기서는 Handler = Controller.

         Adapter란? 연결자. 중간처리자.
         여기서는 Servlet#service()에서 사용하는 Controller를 연결해주는 역할. handlerAdapter = controllerAdapter.

         네이밍을 그냥 requestMappingController. controllerAdapter 이런식으로 지었으면 더 직관적이었을 텐데.
         viewResolver가 아니라 jspResolver. 프레임워크에서는 의미를 포괄적으로 포함할 수 있는 단어를 써야 해서 그렇다고 하지만.

         예제에서는 그렇게 설명을 하고. 나중에 handler, view라는 용어로 변경 했으면 더 좋았을 것 같음.

         파악이 잘 안 될 때는 하나하나 주석을 달아가면서 이해하면 도움됨.
         */

        // interface로 구현된 Controller 찾기.
        RequestMappingHandler requestMappingHandler = new RequestMappingHandler();
        requestMappingHandler.init();

        // annotation으로 맵핑된 Controller 찾기.
        AnnotationHandlerMapping annotationHandlerMapping = new AnnotationHandlerMapping("org.example");
        annotationHandlerMapping.initialize();

        handlerMappings = Arrays.asList(requestMappingHandler, annotationHandlerMapping);
        handlerAdapters = Arrays.asList(new InterfaceHandlerAdapter(), new AnnotationHandlerAdapter());
        viewResolvers = Collections.singletonList(new JspViewResolver());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        log.info("[DispatcherServlet] service started.");
        String requestURI = request.getRequestURI();
        RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod());

        try {
            // #1.요청에 따른 Controller 찾기.
            Object handler = handlerMappings.stream()
                    .filter(hm -> hm.findHandler(new HandlerKey(requestMethod, requestURI)) != null)
                    .map(hm -> hm.findHandler(new HandlerKey(requestMethod, requestURI)))
                    .findFirst()
                    .orElseThrow(() -> new ServletException("no handler for [" + requestMethod + "," + requestURI + "]"));

            // #2.Controller 중간 처리 클래스 찾기.
            HandlerAdapter handlerAdapter = handlerAdapters.stream()
                    .filter(v -> v.supports(handler)) //
                    .findFirst()
                    .orElseThrow(() -> new ServletException("No adapter for handler [" + handler + "]"));

            // #3.jsp 페이지 이름 찾기.
            ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);

            // #4.redirect || forward 처리.
            for (ViewResolver viewResolver : viewResolvers) {
                View view = viewResolver.resolveView(modelAndView.getViewName());
                view.render(modelAndView.getModel(), request, response);
            }

        } catch (Exception e) {
            log.error("exception occurred: [{}]", e.getMessage(), e);
            throw new ServletException(e);
        }
    }
}
