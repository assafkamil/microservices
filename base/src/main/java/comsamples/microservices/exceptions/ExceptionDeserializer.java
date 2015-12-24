package com.samples.microservices.exceptions;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ExceptionDeserializer extends JsonDeserializer<ExceptionSerializeWrapper> {
    @Override
    public ExceptionSerializeWrapper deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ExceptionSerializeWrapper exceptionSerializeWrapper = new ExceptionSerializeWrapper();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonParser);
        JsonNode classNameNode = node.path("className");
        String className = classNameNode.asText();
        exceptionSerializeWrapper.setClassName(className);

        Class cls = null;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found", e);
        }

        exceptionSerializeWrapper.setException((ExceptionBase) mapper.convertValue(node.path("exception"), cls));
        return exceptionSerializeWrapper;
    }
}
