package com.mysite1.core.servlets;

import com.mysite1.core.services.SearchAnyArticle;
import com.google.gson.JsonObject;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/newshub/search",
                "sling.servlet.methods=GET"
        }
)
public class SearchAnyArticleServlet extends SlingSafeMethodsServlet {

    @Reference
    private SearchAnyArticle articleSearchService;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String keyword = request.getParameter("q");

        if (keyword == null || keyword.trim().isEmpty()) {

            JsonObject error = new JsonObject();
            error.addProperty("error", "Query parameter 'q' is required");

            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(error.toString());
            return;
        }

        JsonObject searchResult = articleSearchService.searchArticles(keyword.trim());

        response.setStatus(SlingHttpServletResponse.SC_OK);
        response.getWriter().write(searchResult.toString());
    }
}
