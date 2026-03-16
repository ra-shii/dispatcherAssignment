package com.mysite1.core.services.impl;

import com.mysite1.core.services.SearchAnyArticle;

import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.json.JSONArray;
import org.json.JSONObject;
import com.day.cq.search.*;
import com.day.cq.search.result.*;


@Component(service = SearchAnyArticle.class)
public class SearchAnyArticleimpl  implements SearchAnyArticle {

    private static final String SEARCH_ROOT = "/content/kaltak/en/news";
    private static final int LIMIT = 10;
    private static final String SUBSERVICE = "content-reader";

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public JSONArray searchArticles(String keyword) {

        JSONArray articles = new JSONArray();
        Map<String, Object> map = new HashMap<>();
        map.put(ResourceResolverFactory.SUBSERVICE, SUBSERVICE);

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(map)) {
            Session session = resolver.adaptTo(Session.class);
            Map<String, String> predicate = buildQuery(keyword);
            Query query = queryBuilder.createQuery(
                    PredicateGroup.create(predicate),
                    session
            );
            SearchResult result = query.getResult();
            for (Hit hit : result.getHits()) {
                Resource page = hit.getResource();
                Resource content = page.getChild("jcr:content");

                if (content == null) continue;
                ValueMap props = content.getValueMap();
                JSONObject article = new JSONObject();
                article.put("path", hit.getPath());
                article.put("title", props.get("jcr:title", ""));
                article.put("subtitle", props.get("subtitle", ""));
                articles.put(article);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return articles;
    }

    private Map<String,String> buildQuery(String keyword){

        Map<String,String> map = new HashMap<>();

        map.put("path", SEARCH_ROOT);
        map.put("type", "cq:Page");
        map.put("group.p.or","true");
        map.put("group.1_property","jcr:content/jcr:title");
        map.put("group.1_property.value","%"+keyword+"%");
        map.put("group.1_property.operation","like");
        map.put("group.2_property","jcr:content/subtitle");
        map.put("group.2_property.value","%"+keyword+"%");
        map.put("group.2_property.operation","like");
        map.put("orderby","@jcr:content/publishDate");
        map.put("orderby.sort","desc");
        map.put("p.limit", String.valueOf(LIMIT));

        return map;
    }
}