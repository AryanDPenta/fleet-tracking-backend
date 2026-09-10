package com.fleet.tracking.websocket;

import com.fleet.tracking.dto.AlertDTO;
import com.fleet.tracking.dto.LiveTruckDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

// Central place that pushes real-time updates out over STOMP topics.
// Frontend subscribes to:
//   /topic/company/{companyId}/locations   -> LiveTruckDTO, one message per ping
//   /topic/company/{companyId}/alerts      -> AlertDTO, pushed to admin dashboard
//   /topic/driver/{driverId}/alerts        -> AlertDTO, pushed to a specific driver
@Component
@RequiredArgsConstructor
public class LocationBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastLocation(Long companyId, LiveTruckDTO dto) {
        messagingTemplate.convertAndSend("/topic/company/" + companyId + "/locations", dto);
    }

    public void broadcastAlertToAdmin(Long companyId, AlertDTO dto) {
        messagingTemplate.convertAndSend("/topic/company/" + companyId + "/alerts", dto);
    }

    public void broadcastAlertToDriver(Long driverId, AlertDTO dto) {
        messagingTemplate.convertAndSend("/topic/driver/" + driverId + "/alerts", dto);
    }
}
