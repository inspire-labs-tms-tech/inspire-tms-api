package com.inspiretmstech.api.src.utils.inouttimes;

import com.inspiretmstech.api.src.utils.WithLogger;
import com.inspiretmstech.api.src.utils.inouttimes.processors.AfterShipProcessor;
import com.inspiretmstech.api.src.utils.inouttimes.processors.PrincetonTMXProcessor;
import com.inspiretmstech.api.src.utils.inouttimes.processors.Processes;
import com.inspiretmstech.api.src.utils.inouttimes.processors.SaveToDatabaseProcessor;

import java.util.List;

public class InOutTimesProcessor extends WithLogger {

    private final List<TimeProcessor> processors;

    public InOutTimesProcessor(List<TimeProcessor> processors) {
        super(InOutTimesProcessor.class);
        this.processors = processors;
    }

    public InOutTimesProcessor() {
        super(InOutTimesProcessor.class);
        this.processors = List.of(
                new SaveToDatabaseProcessor(),
                new AfterShipProcessor(),
                new PrincetonTMXProcessor()
        );
    }

    public void arrived(InOutTimes request) {
        this.process(Processes.ARRIVED, request);
    }

    public void departed(InOutTimes request) {
        this.process(Processes.DEPARTED, request);
    }

    private void process(Processes type, InOutTimes request) {
        this.logger.info("processing {} time ({} active processors)", type, this.processors.size());
        for (TimeProcessor processor : this.processors) {
            try {
                this.logger.trace("processing {}", processor.getClass().getSimpleName());
                processor.process(type, request);
                this.logger.trace("processed {}", processor.getClass().getSimpleName());
            } catch (Exception e) {
                this.logger.error("an unhandled exception occurred while processing {} time using {}: {}", type, processor.getClass().getSimpleName(), e.getMessage());
            }
        } // end for-loop
    }

}
