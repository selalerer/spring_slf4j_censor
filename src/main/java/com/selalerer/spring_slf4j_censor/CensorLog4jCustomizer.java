package com.selalerer.spring_slf4j_censor;

import com.selalerer.censor.Censor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.rewrite.RewriteAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CensorLog4jCustomizer {

    private final Censor censor;

    /***
     * Adds rewrite appenders to all the existing appenders on ContextRefreshEvent.
     * The rewrite appenders edit the messages using the censor.edit() method.
     * Any secret added to the Censor bean with its censor() method will be censored from the log messages.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void censorLog4j() {

        final var ctx = (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
        final var config = ctx.getConfiguration();

        CensorSlf4jRewritePolicy.censorRef.set(censor);
        var policy = new CensorSlf4jRewritePolicy();

        var appenders = config.getAppenders();

        for (var entry : appenders.entrySet()) {
            var appenderName = entry.getKey();

            var appenderRef = AppenderRef.createAppenderRef(appenderName, null, null);
            var appenderRefs = new AppenderRef[] { appenderRef };

            var rewriteAppender = RewriteAppender.createAppender(
                    "censored-" + appenderName,
                    "true",
                    appenderRefs,
                    config,
                    policy,
                    null
            );

            rewriteAppender.start();
            config.addAppender(rewriteAppender);

            // Remove the original appender and add the new one
            var rootLoggerConfig = config.getRootLogger();
            rootLoggerConfig.removeAppender(appenderName);
            rootLoggerConfig.addAppender(rewriteAppender, null, null);
        }

        ctx.updateLoggers();
    }
}