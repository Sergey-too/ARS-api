package com.example.backend;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
public class SupportController {
    @Autowired private SupportRepository supportRepository;
    @Autowired private SupportMessageRepository messageRepository;


    @GetMapping("/user/{userId}")
    public List<SupportRequest> getUserRequests(@PathVariable Integer userId) {
        return supportRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @GetMapping("/admin/all")
    public List<SupportRequest> getAllRequests() {
        return supportRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public SupportRequest createRequest(@RequestBody SupportRequest request) {
        return supportRepository.save(request);
    }

    @PutMapping("/{id}/status")
    public SupportRequest updateStatus(@PathVariable Integer id, @RequestParam Integer statusId) {
        SupportRequest req = supportRepository.findById(id).orElseThrow();
        req.setStatusId(statusId);
        return supportRepository.save(req);
    }

    @DeleteMapping("/{id}")
    public void deleteRequest(@PathVariable Integer id) {
        supportRepository.deleteById(id);
    }


    @GetMapping("/{requestId}/messages")
    public List<SupportMessage> getChatMessages(@PathVariable Integer requestId) {
        return messageRepository.findByRequestIdOrderByCreatedAtAsc(requestId);
    }

    @PostMapping("/messages")
    public SupportMessage sendMessage(@RequestBody SupportMessage message) {

        SupportRequest req = supportRepository.findById(message.getRequestId()).orElseThrow();
        if (!req.getUserId().equals(message.getSenderId()) && req.getStatusId() == 1) {
            req.setStatusId(3); 
            supportRepository.save(req);
        }
        return messageRepository.save(message);
    }
}