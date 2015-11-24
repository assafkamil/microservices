package com.samples.microservices.feign;

import feign.Response;
import feign.codec.ErrorDecoder;

public class ErrorDecoderImp implements ErrorDecoder{
    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        return new Exception();
    }
}
