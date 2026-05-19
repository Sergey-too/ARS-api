package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/action-types")
public class ActionTypeController {

    @Autowired
    private ActionTypeRepository actionTypeRepository;

    @GetMapping
    public List<ActionType> getAllActionTypes() {
        return actionTypeRepository.findAll();
    }
}