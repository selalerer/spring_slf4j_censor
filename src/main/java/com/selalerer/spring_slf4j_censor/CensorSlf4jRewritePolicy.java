package com.selalerer.spring_slf4j_censor;

import com.selalerer.censor.Censor;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;

import java.util.concurrent.atomic.AtomicReference;

/***
 * Rewrites log messages using the censor.edit() method.
 */
@Plugin(name = "CensorSlf4jRewritePolicy", category = "Core", elementType = "rewritePolicy")
public class CensorSlf4jRewritePolicy implements RewritePolicy {

    static final AtomicReference<Censor> censorRef = new AtomicReference<>();

    @Override
    public LogEvent rewrite(final LogEvent event) {

        String originalMessage = event.getMessage().getFormattedMessage();
        String editedMessage = originalMessage;
        var censor = censorRef.get();
        if (censor != null) {
            editedMessage = censor.edit(originalMessage);
        }

        return Log4jLogEvent.newBuilder()
                .setLoggerName(event.getLoggerName())
                .setLoggerFqcn(event.getLoggerFqcn())
                .setLevel(event.getLevel())
                .setMessage(new SimpleMessage(editedMessage))
                .setThrown(event.getThrown())
                .setMarker(event.getMarker())
                .setTimeMillis(event.getTimeMillis())
                .setSource(event.getSource())
                .setThreadId(event.getThreadId())
                .setThreadName(event.getThreadName())
                .setThreadPriority(event.getThreadPriority())
                .build();

    }

    @PluginFactory
    public static CensorSlf4jRewritePolicy createPolicy() {
        return new CensorSlf4jRewritePolicy();
    }
}
