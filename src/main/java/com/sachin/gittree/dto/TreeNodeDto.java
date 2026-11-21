package com.sachin.gittree.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeNodeDto {
    private int id;
    private String prefix;
    private String text;
    private String type;
    private String formattedSize;
}