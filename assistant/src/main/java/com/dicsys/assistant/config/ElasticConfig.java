package com.dicsys.assistant.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 1. Forzamos los encabezados exactos para evitar el error de Media-Type
        Header[] compatibilityHeaders = new Header[]{
                new BasicHeader("Accept", "application/vnd.elasticsearch+json;compatible-with=8"),
                new BasicHeader("Content-Type", "application/vnd.elasticsearch+json;compatible-with=8")
        };

        // 2. Construimos el cliente de bajo nivel apuntando a tu clúster local
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http"))
                .setDefaultHeaders(compatibilityHeaders)
                .build();

        // 3. Agregamos el mapeador de JSON (Jackson) para que la IA y ES se entiendan
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper()
        );

        // 4. Devolvemos el cliente oficial listo para usarse en tus @Tools
        return new ElasticsearchClient(transport);
    }
}