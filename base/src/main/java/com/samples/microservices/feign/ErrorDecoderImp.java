package com.samples.microservices.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samples.microservices.exceptions.ExceptionSerializer;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class ErrorDecoderImp implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ExceptionSerializer es = mapper.readValue(response.body().asReader(), ExceptionSerializer.class);
            return es.getException();
        } catch(Exception e) {
            ErrorDecoder.Default defaultDecoder = new ErrorDecoder.Default();
            return defaultDecoder.decode(methodKey, response);
        }
    }
}
