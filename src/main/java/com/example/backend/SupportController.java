package com.example.backend;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support")
public class SupportController {
    @Autowired private SupportRepository supportRepository;

    @GetMapping("/user/{userId}")
    public List<SupportRequest> getUserRequests(@PathVariable Integer userId) {
        return supportRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @PostMapping
    public SupportRequest createRequest(@RequestBody SupportRequest request) {
        return supportRepository.save(request);
    }

    @PutMapping("/{id}")
    public SupportRequest updateRequest(@PathVariable Integer id, @RequestBody SupportRequest details) {
        SupportRequest req = supportRepository.findById(id).orElseThrow();
        req.setSubject(details.getSubject());
        req.setContent(details.getContent());
        return supportRepository.save(req);
    }

    @DeleteMapping("/{id}")
    public void deleteRequest(@PathVariable Integer id) {
        supportRepository.deleteById(id);
    }
}