package com.inspiretmstech.api.src.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface Executor<Request> {

    Logger logger = LoggerFactory.getLogger(Executor.class);

    static <Request> void unsafely(Executor<Request> executor, Request request) throws Exception {
        executor.execute(request);
    }

    static <Request> void safely(Executor<Request> executor, Request request) {
        try {
            executor.execute(request);
        } catch (Exception e) {
            logger.error("an unhandled error occurred: {}", e.getMessage());
        }
    }

    void execute(Request request) throws Exception;

}
