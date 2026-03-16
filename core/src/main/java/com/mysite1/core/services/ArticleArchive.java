package com.mysite1.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Article Archiver Configuration",
        description = "Configuration for automatic archiving old articles"
)
public @interface ArticleArchive {

    @AttributeDefinition(
            name = "Cron Expression",
            description = "Runs every day at 3 AM"
    )
    String scheduler_expression() default "0 0 3 * * ?";

    @AttributeDefinition(
            name = "News Root Path"
    )
    String news_root() default "/content/newshun/en/news";

    @AttributeDefinition(
            name = "Archive Root Path"
    )
    String archive_root() default "/content/newshub/en/archive";

    @AttributeDefinition(
            name = "Archive After Days"
    )
    int days_limit() default 365;

    @AttributeDefinition(
            name = "Batch Size"
    )
    int batch_size() default 20;
}