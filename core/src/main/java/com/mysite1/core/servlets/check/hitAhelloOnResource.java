package com.mysite1.core.servlets.check;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import org.apache.sling.api.resource.Resource ;
import javax.servlet.Servlet;
import java.io.IOException;

@Component(service ={Servlet.class})
@SlingServletResourceTypes(
        resourceTypes = "mysite1/components/page",
        methods = HttpConstants.METHOD_GET ,
        selectors = {"leaveme"},
        extensions = "json"

)
@ServiceDescription("This is servlet is made only for my practise")
public class hitAhelloOnResource extends SlingSafeMethodsServlet
{
    private static final long serialVersionUID = 1L ;

    @Override
    protected void doGet (final SlingHttpServletRequest request , final SlingHttpServletResponse response)throws SecurityException , IOException
    {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write("{ \"I\" :\" created a json\" }");

        Resource res = request.getResource();
        ValueMap properties = res.getValueMap();
        String title = properties.get("jcr:title",String.class);
        String page = properties.get("sling:resourceType",String.class);
        String ab = properties.get("pageTitle",String.class);
        response.getWriter().write(title+"\n"+page+"\n"+ab);

    }

}
