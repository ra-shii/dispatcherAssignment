package com.mysite1.core.servlets.check;


import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;

@Component(service={Servlet.class}
               , property={ "sling.servlet.paths=/bin/empty/inside" ,
        "sling.servlet.methods=GET"})

public class GetChildren extends SlingSafeMethodsServlet
{
   private static final long serialVersionUID = 1L ;

   @Override
    protected void doGet(final SlingHttpServletRequest request , final SlingHttpServletResponse response) throws ServletException,IOException
   {
       response.setContentType("text/plain");
       String path = request.getParameter("path") ;
       ResourceResolver resolver = request.getResourceResolver();
       Resource resource = resolver.getResource(path) ;
       Iterable<Resource> children = resource.getChildren();
       for(Resource child : children)
       {
           response.getWriter().write(child.getName()+"\n");
       }

   }
}
