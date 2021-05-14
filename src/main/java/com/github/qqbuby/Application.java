package com.github.qqbuby;

import java.io.File;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

public class Application {
    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8081);
        tomcat.getConnector();

        // Servlet
        Context ctx = tomcat.addContext("/", new File(".").getAbsolutePath());
        tomcat.addServlet("/", "hello", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                    throws ServletException, IOException {
                resp.getWriter().print("Hello Servletx!");
            }
        });
        ctx.addServletMappingDecoded("/*", "hello");

        // Filter
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName("foobar");
        filterDef.setFilter(new HttpFilter() {
            @Override
            protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                response.setHeader("FOO", "BAR");
                super.doFilter(request, response, chain);
            }
        });
        ctx.addFilterDef(filterDef);
        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(filterDef.getFilterName());
        filterMap.addURLPattern("/*");
        ctx.addFilterMap(filterMap);

        // Listener
        ctx.addApplicationListener(HttpServletRequestListener.class.getName());

        tomcat.start();
        tomcat.getServer().await();
    }

    public static class HttpServletRequestListener implements ServletRequestListener {
        public HttpServletRequestListener() {
        }

        @Override
        public void requestInitialized(ServletRequestEvent sre) {
            String requestURI = ((HttpServletRequest) sre.getServletRequest()).getRequestURI();
            System.out.println(requestURI);
        }
    }
}
