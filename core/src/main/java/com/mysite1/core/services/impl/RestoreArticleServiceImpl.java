package com.mysite1.core.services.impl;

import com.google.gson.JsonObject;
import com.mysite1.core.services.RestoreArticle;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;

@Component(service = RestoreArticle.class)
public class RestoreArticleServiceImpl implements RestoreArticle {

    private static final Logger LOG = LoggerFactory.getLogger(RestoreArticleServiceImpl.class);

    private static final String ARCHIVE_PATH = "/content/newshub/en/archive";
    private static final String NEWS_PATH    = "/content/newshub/en/news";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public JsonObject restoreArticle(String articlePath) {
        JsonObject response = new JsonObject();

        if (articlePath == null || articlePath.isEmpty()) {
            response.addProperty("success", false);
            response.addProperty("message", "articlePath is required");
            return response;
        }
        if (!articlePath.startsWith(ARCHIVE_PATH)) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid article path. Must start with " + ARCHIVE_PATH);
            return response;
        }

        Map<String, Object> params = new HashMap<>();
        params.put(ResourceResolverFactory.SUBSERVICE, "content-writer");

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(params)) {

            PageManager pageManager = resolver.adaptTo(PageManager.class);
            if (pageManager == null) {
                response.addProperty("success", false);
                response.addProperty("message", "Could not obtain PageManager");
                return response;
            }

            Page articlePage = pageManager.getPage(articlePath);
            if (articlePage == null) {
                response.addProperty("success", false);
                response.addProperty("message", "Article not found at: " + articlePath);
                return response;
            }

            String restoredPath = NEWS_PATH + "/" + articlePage.getName();
            Session session = resolver.adaptTo(Session.class);
            if (session == null) {
                response.addProperty("success", false);
                response.addProperty("message", "Could not obtain JCR Session");
                return response;
            }

            session.move(articlePath, restoredPath);
            session.save();

            response.addProperty("success", true);
            response.addProperty("message", "Article restored successfully");
            response.addProperty("sourcePath", articlePath);
            response.addProperty("restoredPath", restoredPath);

        } catch (Exception e) {
            LOG.error("Error restoring article at path: {}", articlePath, e);
            response.addProperty("success", false);
            response.addProperty("message", "Error while restoring article: " + e.getMessage());
        }

        return response;
    }
}
