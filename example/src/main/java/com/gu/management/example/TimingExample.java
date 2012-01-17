package com.gu.management.example;

import com.gu.management.Timing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class TimingExample {


    public void doSomethingInterestingAndLog() {
        Timing.info(
                LoggerFactory.getLogger(TimingExample.class),
                "this is interesting",
                TimingMetrics.downtime(),
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return "hello";
                    }
                }
        );
    }
}


