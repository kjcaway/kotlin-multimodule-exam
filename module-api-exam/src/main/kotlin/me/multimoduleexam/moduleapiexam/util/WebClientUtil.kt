package me.multimoduleexam.moduleapiexam.util

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap


object WebClientUtil {
    private const val MAX_BUFFER_SIZE = 10 * 1024 * 1024  // 10MB
    private val timeoutClients = ConcurrentHashMap<Int, WebClient>()

    // Default timeout of 60 seconds
    private const val DEFAULT_TIMEOUT = 60

    // You'd need to add this logger declaration
    private val logger = LoggerFactory.getLogger(WebClientUtil::class.java)

    init {
        // Pre-initialize common timeouts
        getClientWithTimeout(3)
        getClientWithTimeout(DEFAULT_TIMEOUT)
    }

    private fun getClientWithTimeout(timeoutSeconds: Int): WebClient {
        return timeoutClients.computeIfAbsent(timeoutSeconds) { createWebClient(it) }
    }

    private fun createWebClient(timeoutSeconds: Int): WebClient {
        val httpClient = HttpClient.create(
            ConnectionProvider.builder("http-pool-$timeoutSeconds")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(60))
                .maxLifeTime(Duration.ofSeconds(120))
                .evictInBackground(Duration.ofSeconds(120))
                .lifo()
                .build()
        ).responseTimeout(Duration.ofSeconds(timeoutSeconds.toLong()))

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_BUFFER_SIZE) }
                .build())
            .build()
    }

    // Standard GET with default timeout
    fun get(url: String, path: String, headers: Map<String, String>, params: Map<String, String>): Mono<String> {
        return get(url, path, headers, params, DEFAULT_TIMEOUT)
    }

    // GET with custom timeout
    fun get(
        url: String, path: String, headers: Map<String, String>,
        params: Map<String, String>, timeoutSeconds: Int
    ): Mono<String> {
        val multiValueMap = LinkedMultiValueMap<String, String>()
        params.forEach { (key, value) -> multiValueMap.add(key, value) }
        val uri = UriComponentsBuilder.fromUriString(url)
            .path(path)
            .queryParams(multiValueMap)
            .build()
            .toUri()

        return getClientWithTimeout(timeoutSeconds).get()
            .uri(uri)
            .headers { httpHeaders -> httpHeaders.setAll(headers) }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(String::class.java)
            .onErrorMap { handleError(it) }
    }

    fun get(uri: String, headers: Map<String, String>): Mono<String> {
        return get(uri, headers, DEFAULT_TIMEOUT)
    }

    fun get(uri: String, headers: Map<String, String>, timeoutSeconds: Int): Mono<String> {
        return getClientWithTimeout(timeoutSeconds).get()
            .uri(uri)
            .headers { httpHeaders -> httpHeaders.setAll(headers) }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(String::class.java)
            .onErrorMap { handleError(it) }
    }

    fun post(uri: String, headers: Map<String, String>, body: Any): Mono<String> {
        return post(uri, headers, body, DEFAULT_TIMEOUT)
    }

    fun post(uri: String, headers: Map<String, String>, body: Any, timeoutSeconds: Int): Mono<String> {
        return getClientWithTimeout(timeoutSeconds).post()
            .uri(uri)
            .headers { httpHeaders -> httpHeaders.setAll(headers) }
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(body))
            .retrieve()
            .bodyToMono(String::class.java)
            .onErrorMap { handleError(it) }
    }

    // Previous postWithTimeout3 method now handled by standard post with timeout param
    fun postWithTimeout3(uri: String, headers: Map<String, String>, body: Any): Mono<String> {
        return post(uri, headers, body, 3)
    }

    fun put(uri: String, headers: Map<String, String>, body: Any): Mono<String> {
        return put(uri, headers, body, DEFAULT_TIMEOUT)
    }

    fun put(uri: String, headers: Map<String, String>, body: Any, timeoutSeconds: Int): Mono<String> {
        return getClientWithTimeout(timeoutSeconds).put()
            .uri(uri)
            .headers { httpHeaders -> httpHeaders.setAll(headers) }
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(body))
            .retrieve()
            .bodyToMono(String::class.java)
            .onErrorMap { handleError(it) }
    }

    fun delete(uri: String, headers: Map<String, String>, params: Map<String, String>): Mono<String> {
        return delete(uri, headers, params, DEFAULT_TIMEOUT)
    }

    fun delete(
        uri: String, headers: Map<String, String>, params: Map<String, String>,
        timeoutSeconds: Int
    ): Mono<String> {
        val multiValueMap = LinkedMultiValueMap<String, String>()
        params.forEach { (key, value) -> multiValueMap.add(key, value) }

        return getClientWithTimeout(timeoutSeconds).delete()
            .uri { uriBuilder -> uriBuilder.path(uri).queryParams(multiValueMap).build() }
            .headers { httpHeaders -> httpHeaders.setAll(headers) }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(String::class.java)
            .onErrorMap { handleError(it) }
    }

    fun delete(uri: String, headers: Map<String, String>): Mono<String> {
        return delete(uri, headers, DEFAULT_TIMEOUT)
    }

    fun delete(uri: String, headers: Map<String, String>, timeoutSeconds: Int): Mono<String> {
        return getClientWithTimeout(timeoutSeconds).delete()
            .uri(uri)
            .headers { httpHeaders -> httpHeaders.setAll(headers) }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(String::class.java)
            .onErrorMap { handleError(it) }
    }

    private fun handleError(e: Throwable): Throwable {
        // Enhanced error handling could go here
        if (e is WebClientResponseException) {
            logger.error("HTTP error: ${e.statusCode} ${e.statusText}")
        }
        return e
    }
}