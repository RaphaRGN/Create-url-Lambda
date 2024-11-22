package com.raphaelprojects.createUrlShortener;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*Classe main implementa o RequestHandler, passando como parametro um mapa com String e objeto e retornando
Um com String, String
 */

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {

    //Instancias das classes que serão usadas
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.builder().build();

    //Sobrescreve o método handle request
    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        String body = (String) input.get("body");

        Map<String, String> bodyMap;

        // Tenta ler o valor do body, se não joga uma excessão

            try {
                bodyMap = objectMapper.readValue(body, Map.class);

            } catch (JsonProcessingException exception){
                throw new RuntimeException("Error parsing Json" + exception.getMessage(), exception);

            }

            //Atributos

            String originalUrl = bodyMap.get("originalUrl");
            String expirationTime = bodyMap.get("expirationTime");
            long expirationTimeInSeconds = Long.parseLong(expirationTime);


            String shortUrlCode = UUID.randomUUID().toString().substring(0, 8);

            UrlData urlData = new UrlData(originalUrl, expirationTimeInSeconds);

            //Tenta colocar o objeto urldata em um bucket no aws, se não joga uma excessão

            try {
                String urlDataJson = objectMapper.writeValueAsString(urlData);
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket("url-shortener-storage-raphael")
                        .key(shortUrlCode + ".json")
                        .build();

                s3Client.putObject(request, RequestBody.fromString(urlDataJson));
            } catch (Exception exception){

                throw new RuntimeException("Error saving data to S3: " + exception.getMessage(), exception );

            }

            // Retorna uma resposta

            Map<String, String> response = new HashMap<>();
            response.put("code", shortUrlCode);

            return response;
    }

}