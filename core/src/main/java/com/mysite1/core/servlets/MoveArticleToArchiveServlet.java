package com.mysite1.core.servlets;

import java.io.IOException;

import com.mysite1.core.services.SearchAnyArticle;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.sling.api.servlets.ServletResolverConstants;
@Component(
        service = Servlet.class,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/archive/archive",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST
        }
)
public class MoveArticleToArchiveServlet extends SlingAllMethodsServlet
{
    @Reference
    private SearchAnyArticle archiveService;
    @Override
    protected void doPost(SlingHttpServletRequest request,
                          SlingHttpServletResponse response)
            throws ServletException, IOException {

        String articlePath = request.getParameter("articlePath");
        if (articlePath == null || articlePath.trim().isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("Article Path has not been given");
            return;
        }

        boolean archived =
                archiveService.archiveArticle(articlePath);

        if (archived) {
            response.getWriter().write("Successfully- Archived is done");

        } else {
            response.setStatus(500);
            response.getWriter().write("Failed to archive article");
        }
    }
}
