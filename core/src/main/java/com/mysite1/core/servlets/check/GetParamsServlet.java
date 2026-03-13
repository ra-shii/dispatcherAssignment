package com.mysite1.core.servlets.check;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.request.RequestParameter;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/getparams", // Access via /bin/example/getparams
                "sling.servlet.methods=GET"
        }
)
public class GetParamsServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        try {
            // 1. Get a single parameter value (null if missing)
            String name = request.getParameter("name");

            // 2. Get multiple values for the same parameter
            String[] tags = request.getParameterValues("tag");

            // 3. Get as RequestParameter object (preserves encoding, file uploads, etc.)
            RequestParameter param = request.getRequestParameter("name");

            // 4. Get all parameters as a map
            var paramMap = request.getParameterMap();

            // Build JSON-like output
            StringBuilder output = new StringBuilder();
            output.append("{");
            output.append("\"name\":\"").append(name).append("\",");
            output.append("\"tags\":[");
            if (tags != null) {
                for (int i = 0; i < tags.length; i++) {
                    output.append("\"").append(tags[i]).append("\"");
                    if (i < tags.length - 1) output.append(",");
                }
            }
            output.append("],");
            output.append("\"totalParams\":").append(paramMap.size());
            output.append("}");

            response.getWriter().write(output.toString());

        } catch (Exception e) {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid request\"}");
        }
    }
}
