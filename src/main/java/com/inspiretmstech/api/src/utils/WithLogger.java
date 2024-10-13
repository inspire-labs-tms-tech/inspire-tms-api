package com.inspiretmstech.api.src.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WithLogger {

    protected final Logger logger;

    public WithLogger(Class<?> controllerClass) {
        logger = LoggerFactory.getLogger(controllerClass);
    }

}
