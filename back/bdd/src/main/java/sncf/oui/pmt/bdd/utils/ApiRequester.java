/*
 *
 *  * Copyright (C) 2018 VSCT
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package sncf.oui.pmt.bdd.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.Consumer;

public class ApiRequester {

    private String apiHost;
    private int apiPort;
    private CloseableHttpClient client;

    public ApiRequester(String apiHost, int apiPort) {
        this.apiHost = apiHost;
        this.apiPort = apiPort;
        client = makeClient();
    }

    public static String resBody(HttpResponse res) {
        try {
            return EntityUtils.toString(res.getEntity(), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getResponseFor(HttpUriRequest req, Consumer<HttpResponse> callback) {
        try (CloseableHttpResponse response = client.execute(req)) {
            callback.accept(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CloseableHttpClient makeClient() {
        int timeout = 5000;
        RequestConfig config = RequestConfig.custom()
                //.setConnectTimeout(timeout)
                //.setSocketTimeout(timeout)
                .build();
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .build();
    }

    private URI buildUri(String path, Map<String, String> params) {
        URIBuilder builder = new URIBuilder()
                .setScheme("http")
                .setHost(apiHost)
                .setPort(apiPort)
                .setPath(path);
        params.forEach(builder::setParameter);
        try {
            return builder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void get(String path, Map<String, String> params, Consumer<HttpResponse> callback) {
        getResponseFor(new HttpGet(buildUri(path, params)), callback);
    }

    public void post(String path, Map<String, String> params, Map<String, Object> body,
                     Consumer<HttpResponse> callback) {
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        StringEntity entity = new StringEntity(json, ContentType.create("application/json", "UTF-8"));
        HttpPost req = new HttpPost(buildUri(path, params));
        req.setEntity(entity);
        getResponseFor(req, callback);
    }

    public void postFormWithFile(String path, Map<String, String> params, Map<String, Object> body, InputStream input, String filename, Consumer<HttpResponse> callback) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        body.forEach((key, value) -> builder.addTextBody(key, (String) value));
        builder.addPart("file", new InputStreamBody(input, filename));
        HttpEntity multipart = builder.build();
        HttpPost req = new HttpPost(buildUri(path, params));
        req.setEntity(multipart);
        getResponseFor(req, callback);
    }

    public void put(String path, Map<String, String> params, Map<String, Object> body, Consumer<HttpResponse> callback) {
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        StringEntity entity = new StringEntity(json, ContentType.create("application/json", "UTF-8"));
        HttpPut req = new HttpPut(buildUri(path, params));
        req.setEntity(entity);
        getResponseFor(req, callback);
    }
}
