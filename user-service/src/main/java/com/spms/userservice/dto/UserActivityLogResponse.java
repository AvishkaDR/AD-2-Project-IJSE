package com.spms.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityLogResponse {

    private Long id;
    private Long userId;
    private String activityType;
    private String description;
    private LocalDateTime timestamp;
}
