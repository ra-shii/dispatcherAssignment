package com.mysite1.core.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ArticleArchiveJobProducer.class)
public class ArticleArchiveJobProducer {

    private static final String JOB_TOPIC = "kaltak/archive/articles";

    @Reference
    private JobManager jobManager;

    public void startArchiveJob() {

        Map<String, Object> props = new HashMap<>();

        jobManager.addJob(JOB_TOPIC, props);
    }
}