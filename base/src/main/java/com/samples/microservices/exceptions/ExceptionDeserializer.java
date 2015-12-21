package com.samples.microservices.exceptions;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ExceptionDeserializer extends JsonDeserializer<ExceptionSerializer> {
    @Override
    public ExceptionSerializer deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ExceptionSerializer exceptionSerializer = new ExceptionSerializer();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonParser);
        JsonNode classNameNode = node.path("className");
        String className = classNameNode.asText();
        exceptionSerializer.setClassName(className);

        Class cls = null;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found", e);
        }

        exceptionSerializer.setException((Exception)mapper.convertValue(node.path("exception"), cls));
        return exceptionSerializer;
    }
}
