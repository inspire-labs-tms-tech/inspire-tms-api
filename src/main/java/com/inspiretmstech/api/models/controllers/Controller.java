package com.inspiretmstech.api.models.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Controller {

    protected final Logger logger;

    public Controller(Class<?> controllerClass) {
        logger = LoggerFactory.getLogger(controllerClass);
    }

}
