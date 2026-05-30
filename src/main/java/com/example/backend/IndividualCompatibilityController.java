package com.example.backend;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/individual-compatibility")
public class IndividualCompatibilityController {

    @Autowired
    private IndividualCompatibilityService individualCompatibilityService;

    @GetMapping("/matrix/{userId}")
    public ResponseEntity<List<IndividualCompatibilityDTO>> getMatrix(@PathVariable int userId) {
        List<IndividualCompatibilityDTO> matrix = individualCompatibilityService.getMatrixByUser(userId);
        return ResponseEntity.ok(matrix);
    }

    @PostMapping("/update")
    public ResponseEntity<Void> updateCompatibility(@RequestBody IndividualCompatibilityDTO dto) {
        individualCompatibilityService.update(dto);
        return ResponseEntity.ok().build();
    }
}