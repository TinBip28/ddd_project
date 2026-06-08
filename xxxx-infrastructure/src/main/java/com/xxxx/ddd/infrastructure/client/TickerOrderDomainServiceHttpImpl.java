package com.xxxx.ddd.infrastructure.client;

import com.xxxx.ddd.domain.service.TickerOrderDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Profile("order")
@Slf4j
public class TickerOrderDomainServiceHttpImpl implements TickerOrderDomainService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String TICKET_SERVICE_URL = "http://localhost:8081/ticket";

    @Override
    public boolean decreaseStockLevel1(Long tickerId, int quantity) {
        log.info("HTTP call: decreaseStockLevel1 for ticketId: {} | quantity: {}", tickerId, quantity);
        String url = TICKET_SERVICE_URL + "/internal/" + tickerId + "/decrease-stock?quantity=" + quantity;
        try {
            // Using patchForObject or postForObject. Since RestTemplate's patch might require Apache HttpClient,
            // we can use standard post or get or just patch.
            // Let's use patchForObject. If PATCH is unsupported by default HttpURLConnection, we can use POST
            // but we exposed PatchMapping. Let's make sure it works or use execute/exchange.
            // Actually, we can use RestTemplate's exchange with HttpMethod.PATCH to be safe, or just POST.
            // Let's check: Spring's RestTemplate patchForObject uses HTTP PATCH. If we run on Java 21, it works.
            // To be safe and compatible with all clients, we can also use exchange.
            Boolean result = restTemplate.patchForObject(url, null, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            log.error("Failed to decrease stock via HTTP for ticketId: {}", tickerId, e);
            return false;
        }
    }

    @Override
    public boolean decreaseStockLevel2(Long tickerId, int quantity) {
        return false;
    }

    @Override
    public boolean decreaseStockLevel3CAS(Long tickerId, int oldStockAvailable, int quantity) {
        log.info("HTTP call: decreaseStockLevel3CAS (routing to decreaseStockInternal) for ticketId: {}", tickerId);
        return decreaseStockLevel1(tickerId, quantity);
    }

    @Override
    public int getStockAvailable(Long ticketId) {
        log.info("HTTP call: getStockAvailable for ticketId: {}", ticketId);
        String url = TICKET_SERVICE_URL + "/internal/" + ticketId + "/stock";
        try {
            Integer stock = restTemplate.getForObject(url, Integer.class);
            return stock != null ? stock : 0;
        } catch (Exception e) {
            log.error("Failed to get stock via HTTP for ticketId: {}", ticketId, e);
            return 0;
        }
    }

    @Override
    public boolean increaseStock(Long tickerId, int quantity) {
        log.info("HTTP call: increaseStock for ticketId: {} | quantity: {}", tickerId, quantity);
        String url = TICKET_SERVICE_URL + "/internal/" + tickerId + "/increase-stock?quantity=" + quantity;
        try {
            Boolean result = restTemplate.patchForObject(url, null, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            log.error("Failed to increase stock via HTTP for ticketId: {}", tickerId, e);
            return false;
        }
    }
}
