package com.xxxx.ddd.infrastructure.client;

import com.xxxx.ddd.domain.model.entity.TicketDetail;
import com.xxxx.ddd.domain.service.TicketDetailDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Profile("order")
@Slf4j
public class TicketDetailDomainServiceHttpImpl implements TicketDetailDomainService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String TICKET_SERVICE_URL = "http://localhost:8081/ticket";

    @Override
    public TicketDetail getTicketDetailById(Long ticketId) {
        log.info("HTTP call: getTicketDetailById for ticketId: {}", ticketId);
        String url = TICKET_SERVICE_URL + "/internal/" + ticketId + "/detail";
        try {
            return restTemplate.getForObject(url, TicketDetail.class);
        } catch (Exception e) {
            log.error("Failed to get ticket detail via HTTP for ticketId: {}", ticketId, e);
            return null;
        }
    }
}
