package com.mysite1.core.servlets;


import java.io.IOException;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.*;

import org.osgi.service.component.annotations.*;


@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/start-article-archive",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class ArticleArchiveServlet extends SlingAllMethodsServlet {

    @Reference
    private ArticleArchiveJobProducer producer;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
            throws IOException {

        producer.startArchiveJob();

        response.getWriter().write("Archive Sling Job triggered");
    }
}