package com.inspiretmstech.api.src.utils.inouttimes;

import com.inspiretmstech.api.src.utils.inouttimes.processors.AfterShipProcessor;
import com.inspiretmstech.api.src.utils.inouttimes.processors.Processes;
import com.inspiretmstech.api.src.utils.inouttimes.processors.SaveToDatabaseProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InOutTimesProcessor {

    private static final Logger logger = LoggerFactory.getLogger(InOutTimesProcessor.class);
    private static final List<TimeProcessor> processors = List.of(
            new SaveToDatabaseProcessor(),
            new AfterShipProcessor()
    );

    public static void arrived(InOutTimes request) {
        process(Processes.ARRIVED, request);
    }

    public static void departed(InOutTimes request) {
        process(Processes.DEPARTED, request);
    }

    private static void process(Processes type, InOutTimes request) {
        logger.info("processing {} time ({} active processors)", type, processors.size());
        for (TimeProcessor processor : processors) {
            try {
                logger.trace("processing {}", processor.getClass().getSimpleName());
                processor.process(type, request);
                logger.trace("processed {}", processor.getClass().getSimpleName());
            } catch (Exception e) {
                logger.error("an unhandled exception occurred while processing {} time using {}: {}", type, processor.getClass().getSimpleName(), e.getMessage());
            }
        } // end for-loop
    }

}
