package com.spms.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserClient {

    @PostMapping("/api/users/{id}/logs")
    void logUserActivity(
            @PathVariable("id") Long id,
            @RequestParam("activityType") String activityType,
            @RequestParam("description") String description);
}
