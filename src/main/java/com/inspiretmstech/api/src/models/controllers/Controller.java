package com.inspiretmstech.api.src.models.controllers;

import com.inspiretmstech.api.src.utils.WithLogger;

public abstract class Controller extends WithLogger {

    public Controller(Class<?> controllerClass) {
        super(controllerClass);
    }

}
