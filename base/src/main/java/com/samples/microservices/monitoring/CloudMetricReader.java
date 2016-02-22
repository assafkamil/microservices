package com.samples.microservices.monitoring;

import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.reader.MetricReader;
import org.springframework.stereotype.Service;

@Service
public class CloudMetricReader implements MetricReader {
    @Override
    public Metric<?> findOne(String metricName) {
        return null;
    }

    @Override
    public Iterable<Metric<?>> findAll() {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }
}
