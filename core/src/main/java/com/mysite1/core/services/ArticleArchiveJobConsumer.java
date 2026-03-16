package com.mysite1.core.services;
import java.util.*;
import javax.jcr.Session;
import org.apache.sling.api.resource.*;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.*;
import com.day.cq.search.result.*;


@Component(
        service = JobConsumer.class,
        property = {
                JobConsumer.PROPERTY_TOPICS + "=kaltak/archive/articles"
        }
)
@Designate(ocd = ArticleArchive.class)
public class ArticleArchiveJobConsumer implements JobConsumer {

    private static final Logger LOG =
            LoggerFactory.getLogger(ArticleArchiveJobConsumer.class);

    private static final String SUBSERVICE = "content-writer";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private QueryBuilder queryBuilder;

    private ArticleArchive;

    @Activate
    @Modified
    protected void activate(ArticleArchive config) {

        this.config = config;
    }

    @Override
    public JobResult process(Job job) {

        LOG.info("Starting Sling Job Archiver...");

        int successCount = 0;
        int failureCount = 0;

        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(ResourceResolverFactory.SUBSERVICE, SUBSERVICE);

        try (ResourceResolver resolver =
                     resolverFactory.getServiceResourceResolver(serviceMap)) {

            Session session = resolver.adaptTo(Session.class);

            List<String> oldArticles = findOldArticles(session);

            LOG.info("Articles eligible for archiving: {}", oldArticles.size());

            int batchSize = config.batch_size();

            for (int i = 0; i < oldArticles.size(); i += batchSize) {

                int endIndex = Math.min(i + batchSize, oldArticles.size());

                List<String> batch = oldArticles.subList(i, endIndex);

                try {

                    for (String articlePath : batch) {

                        String articleName =
                                articlePath.substring(articlePath.lastIndexOf("/") + 1);

                        String destination =
                                config.archive_root() + "/" + articleName;

                        LOG.info("Archiving article {}", articlePath);

                        session.move(articlePath, destination);
                    }

                    session.save();

                    successCount += batch.size();

                } catch (Exception e) {

                    LOG.error("Batch failed. Reverting batch starting with {}",
                            batch.get(0), e);

                    session.refresh(false);

                    failureCount += batch.size();
                }
            }

        } catch (Exception e) {

            LOG.error("Error executing archive job", e);

            return JobResult.FAILED;
        }

        LOG.info("Archiving completed -> Success: {}, Failed: {}",
                successCount, failureCount);

        return JobResult.OK;
    }

    private List<String> findOldArticles(Session session) {

        List<String> results = new ArrayList<>();

        Calendar limitDate = Calendar.getInstance();

        limitDate.add(Calendar.DAY_OF_YEAR, -config.days_limit());

        Map<String, String> map = new HashMap<>();

        map.put("path", config.news_root());
        map.put("type", "cq:Page");

        map.put("1_property", "jcr:content/cq:lastReplicated");
        map.put("1_property.operation", "exists");

        map.put("2_daterange.property", "jcr:content/cq:lastReplicated");
        map.put("2_daterange.upperBound", limitDate.toInstant().toString());
        map.put("2_daterange.upperOperation", "<");

        map.put("p.limit", "-1");

        Query query =
                queryBuilder.createQuery(PredicateGroup.create(map), session);

        SearchResult result = query.getResult();

        try {

            for (Hit hit : result.getHits()) {

                results.add(hit.getPath());
            }

        } catch (Exception e) {

            LOG.error("Error retrieving old articles", e);
        }

        return results;
    }
}