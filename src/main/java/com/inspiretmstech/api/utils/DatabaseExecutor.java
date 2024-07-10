package com.inspiretmstech.api.utils;

import org.jooq.DSLContext;

@FunctionalInterface
public interface DatabaseExecutor<T> {

    public T with(DSLContext context) throws Exception;

}
