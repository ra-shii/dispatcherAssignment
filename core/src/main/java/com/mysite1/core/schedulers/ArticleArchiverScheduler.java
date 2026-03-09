package com.mysite1.core.schedulers;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
@Designate(ocd = ArticleArchiverScheduler.Config.class)
@Component(service = Runnable.class)
public class ArticleArchiverScheduler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ArticleArchiverScheduler.class);

    private static final String NEWS_PATH    = "/content/newshub/en/news";
    private static final String ARCHIVE_PATH = "/content/newshub/en/archive";
    private static final String PUBLISH_DATE_PROPERTY = "publishDate";
    private static final int    BATCH_SIZE   = 20;
    private static final int    MAX_AGE_DAYS = 365;

    @ObjectClassDefinition(
            name = "NewsHub — Automatic Article Archiver",
            description = "Moves articles older than 365 days from /news to /archive every day at 3:00 AM"
    )
    public static @interface Config {

        @AttributeDefinition(name = "Cron-job expression")
        String scheduler_expression() default "0 0 3 * * ?";  // 3:00 AM every day

        @AttributeDefinition(name = "Concurrent task",
                description = "Whether to allow concurrent execution of this scheduler")
        boolean scheduler_concurrent() default false;
    }

    @Reference
    private ResourceResolverFactory resolverFactory;
    @Activate
    protected void activate(final Config config) {
        LOG.info("ArticleArchiverScheduler activated. Cron: {}", config.scheduler_expression());
    }
    @Override
    public void run() {
        LOG.info("ArticleArchiverScheduler — starting archival run");

        Map<String, Object> params = new HashMap<>();
        params.put(ResourceResolverFactory.SUBSERVICE, "content-writer");

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(params)) {

            PageManager pageManager = resolver.adaptTo(PageManager.class);
            if (pageManager == null) {
                LOG.error("Could not obtain PageManager — aborting archival run");
                return;
            }
            Session session = resolver.adaptTo(Session.class);
            if (session == null) {
                LOG.error("Could not obtain JCR Session — aborting archival run");
                return;
            }
            List<String> articlesToArchive = findOldArticles(resolver);

            if (articlesToArchive.isEmpty()) {
                LOG.info("ArticleArchiverScheduler — no articles to archive");
                return;
            }

            LOG.info("ArticleArchiverScheduler — found {} articles to archive", articlesToArchive.size());

            int archivedCount = 0;
            int failedCount   = 0;

            List<String> currentBatch = new ArrayList<>();

            for (String articlePath : articlesToArchive) {
                try {
                    Page articlePage = pageManager.getPage(articlePath);
                    if (articlePage == null) {
                        LOG.warn("Article not found, skipping: {}", articlePath);
                        failedCount++;
                        continue;
                    }

                    String destinationPath = ARCHIVE_PATH + "/" + articlePage.getName();

                    session.move(articlePath, destinationPath);
                    currentBatch.add(articlePath);
                    if (currentBatch.size() >= BATCH_SIZE) {
                        session.save();
                        archivedCount += currentBatch.size();
                        LOG.info("Committed batch of {} articles", currentBatch.size());
                        currentBatch.clear();
                    }

                } catch (Exception e) {
                    LOG.error("Failed to archive article: {} — {}", articlePath, e.getMessage(), e);
                    failedCount++;
                    if (!currentBatch.isEmpty()) {
                        try {
                            session.refresh(false); // discard all pending (unsaved) moves
                        } catch (Exception refreshEx) {
                            LOG.error("Failed to revert batch: {}", refreshEx.getMessage());
                        }
                        LOG.warn("Reverted uncommitted batch of {} articles due to failure on: {}",
                                currentBatch.size(), articlePath);
                        currentBatch.clear();
                    }
                }
            }
            if (!currentBatch.isEmpty()) {
                try {
                    session.save();
                    archivedCount += currentBatch.size();
                    LOG.info("Committed final batch of {} articles", currentBatch.size());
                } catch (Exception e) {
                    LOG.error("Failed to commit final batch: {}", e.getMessage(), e);
                    failedCount += currentBatch.size();
                    session.refresh(false);
                }
            }

            LOG.info("ArticleArchiverScheduler — SUMMARY: archived={}, failed={}", archivedCount, failedCount);
        } catch (Exception e) {
            LOG.error("ArticleArchiverScheduler — fatal error during archival run", e);
        }
    }
    private List<String> findOldArticles(ResourceResolver resolver) {
        List<String> result = new ArrayList<>();
        Resource newsResource = resolver.getResource(NEWS_PATH);
        if (newsResource == null) {
            LOG.warn("News path not found: {}", NEWS_PATH);
            return result;
        }
        Calendar cutoff = Calendar.getInstance();
        cutoff.add(Calendar.DAY_OF_YEAR, -MAX_AGE_DAYS);
        Iterator<Resource> children = newsResource.listChildren();
        while (children.hasNext()) {
            Resource child = children.next();
            try {
                Node jcrContent = child.adaptTo(Node.class);
                if (jcrContent == null) {
                    continue;
                }
                Resource contentResource = child.getChild("jcr:content");
                if (contentResource == null) {
                    continue;
                }
                Node contentNode = contentResource.adaptTo(Node.class);
                if (contentNode == null || !contentNode.hasProperty(PUBLISH_DATE_PROPERTY)) {
                    continue;
                }
                Calendar publishDate = contentNode.getProperty(PUBLISH_DATE_PROPERTY).getDate();
                if (publishDate.before(cutoff)) {
                    result.add(child.getPath());
                }

            } catch (Exception e) {
                LOG.warn("Could not read publishDate for resource: {} — {}", child.getPath(), e.getMessage());
            }
        }
        return result;
    }
}
