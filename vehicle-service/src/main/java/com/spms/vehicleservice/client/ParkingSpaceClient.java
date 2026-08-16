package com.spms.vehicleservice.client;

import com.spms.vehicleservice.dto.ParkingSpaceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "parking-space-service")
public interface ParkingSpaceClient {

    @GetMapping("/api/parking-spaces/{id}")
    ParkingSpaceDto getParkingSpaceById(@PathVariable("id") Long id);

    @PostMapping("/api/parking-spaces/{id}/occupy")
    ParkingSpaceDto occupySpace(@PathVariable("id") Long id);

    @PostMapping("/api/parking-spaces/{id}/vacate")
    ParkingSpaceDto vacateSpace(@PathVariable("id") Long id);
}
