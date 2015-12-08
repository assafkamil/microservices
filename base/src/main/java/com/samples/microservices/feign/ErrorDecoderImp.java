package com.samples.microservices.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class ErrorDecoderImp implements ErrorDecoder {
    private Map<Integer, Class> exceptionMap;

    public ErrorDecoderImp(Map<Integer, Class> exceptionMap) {
        this.exceptionMap = exceptionMap;
    }

    private String readJson(Response response) throws IOException {
        try (BufferedReader buffer = new BufferedReader(response.body().asReader())) {
            return buffer.lines().collect(joining("\n"));
        }
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            int status = response.status();
            Class exceptionClass = exceptionMap.get(status);
            ObjectMapper mapper = new ObjectMapper();
            String json = readJson(response);
            Exception e = (Exception)mapper.readValue(json, exceptionClass);
            return e;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return new Exception();
    }
}
