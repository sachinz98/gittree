package com.sachin.gittree.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeRequestDto {

    @NotBlank(message = "GitHub repository URL is required")
    private String repoUrl;

    private String branch;
}