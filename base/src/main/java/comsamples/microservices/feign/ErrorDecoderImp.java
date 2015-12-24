package com.samples.microservices.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samples.microservices.exceptions.ExceptionSerializeWrapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class ErrorDecoderImp implements ErrorDecoder {
    private ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ExceptionSerializeWrapper es = mapper.readValue(response.body().asReader(), ExceptionSerializeWrapper.class);
            return es.getException();
        } catch(Exception e) {
            return defaultDecoder.decode(methodKey, response);
        }
    }
}
