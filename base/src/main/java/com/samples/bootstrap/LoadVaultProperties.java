package com.samples.bootstrap;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class LoadVaultProperties implements PropertySourceLocator{
    @Value("${vault.bucket}")
    private String vaultBucket;

    @Value("${vault.file}")
    private String vaultFile;

    @Override
    public PropertySource<?> locate(Environment environment) {
        ObjectMapper m = new ObjectMapper();
        try {
            //In production the credentials should come from Instance Profile
            //In DEV machines, the credentials should reside in ~/.aws/credentials
            AmazonS3 s3 = new AmazonS3Client();
            S3Object s3Vault = s3.getObject(new GetObjectRequest(vaultBucket, vaultFile));

            Map<String, Object> map = new HashMap<String, Object>();
            map = m.readValue(s3Vault.getObjectContent(), new TypeReference<Map<String, String>>(){});
            return new MapPropertySource("vaultProperty",
                    Collections.<String, Object>singletonMap("spring.datasource.url", map.get("db")));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new MapPropertySource("vaultProperty", new HashMap<>());
    }
}
