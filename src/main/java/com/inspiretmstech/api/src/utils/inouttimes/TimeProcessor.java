package com.inspiretmstech.api.src.utils.inouttimes;

import com.inspiretmstech.api.src.utils.Executor;
import com.inspiretmstech.api.src.utils.WithLogger;
import com.inspiretmstech.api.src.utils.inouttimes.processors.Processes;
import com.inspiretmstech.db.enums.IntegrationTypes;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public abstract class TimeProcessor extends WithLogger {

    public TimeProcessor(Class<?> cls) {
        super(cls);
    }

    protected void process(Processes process, InOutTimes request) {
        switch (process) {
            case ARRIVED -> this.arrived(request);
            case DEPARTED -> this.departed(request);
        }
    }

    private void arrived(InOutTimes request) {
        Executor.safely(this.getArrivalProcessor(), request);
    }

    private void departed(InOutTimes request) {
        Executor.safely(this.getDepartureProcessor(), request);
    }

    protected abstract @NotNull boolean supports(@Nullable IntegrationTypes type);

    protected abstract @NotNull Executor<InOutTimes> getArrivalProcessor();

    protected abstract @NotNull Executor<InOutTimes> getDepartureProcessor();
}
