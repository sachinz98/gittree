package com.sachin.gittree.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeResponseDto {
    private String repoName;
    private String branch;
    private String totalFormattedSize;
    private List<TreeNodeDto> treeContents;
    private String markdown;
}