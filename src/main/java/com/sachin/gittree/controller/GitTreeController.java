package com.sachin.gittree.controller;

import com.sachin.gittree.dto.TreeRequestDto;
import com.sachin.gittree.dto.TreeResponseDto;
import com.sachin.gittree.service.GitTreeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tree")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class GitTreeController {

    private final GitTreeService gitTreeService;

    @PostMapping
    public ResponseEntity<TreeResponseDto> getRepositoryTree(@Valid @RequestBody TreeRequestDto request) {
        return ResponseEntity.ok(gitTreeService.generateTree(request));
    }
}