package com.mysite1.core.servlets;

import com.google.gson.JsonObject;
import com.mysite1.core.services.RestoreArticle;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/newshub/restore",
                "sling.servlet.methods=POST"
        }
)
public class  RestoreArticleServlet extends SlingAllMethodsServlet {

    @Reference
    private RestoreArticle restoreArticle;

    @Override
    protected void doPost(SlingHttpServletRequest request,
                          SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String articlePath = request.getParameter("articlePath");

        JsonObject result = restoreArticle.restoreArticle(articlePath);

        response.getWriter().write(result.toString());
    }
}
