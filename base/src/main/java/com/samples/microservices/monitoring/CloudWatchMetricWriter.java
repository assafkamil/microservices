package com.samples.microservices.monitoring;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@ExportMetricWriter
public class CloudWatchMetricWriter implements MetricWriter {
    @Autowired
    private AmazonCloudWatch amazonCloudWatch;

    @Autowired
    private CounterService counterService;

    @Override
    public void increment(Delta<?> delta) {
        MetricDatum datum = new MetricDatum();
        datum.setMetricName(delta.getName());
        datum.setTimestamp(new Date());
        datum.setValue(delta.getValue().doubleValue());
        PutMetricDataRequest request = new PutMetricDataRequest().withMetricData(datum).withNamespace("microservice");
        amazonCloudWatch.putMetricData(request);
    }

    @Override
    public void set(Metric<?> value) {
        MetricDatum datum = new MetricDatum();
        datum.setMetricName(value.getName());
        datum.setTimestamp(new Date());
        datum.setValue((double)value.getValue().doubleValue());
        PutMetricDataRequest request = new PutMetricDataRequest().withMetricData(datum).withNamespace("microservice");
        amazonCloudWatch.putMetricData(request);
    }

    @Override
    public void reset(String metricName) {

    }
}
