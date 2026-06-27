package com.ezra.internshiptracker.dto.assistant;

import com.ezra.internshiptracker.entity.InternshipStatus;
import lombok.Data;

import java.util.List;

@Data
public class AssistantAdviceResponse {

    private Long internshipId;

    private InternshipStatus status;

    private String summary;

    private List<String> suggestions;

    private AssistantAdviceSource source = AssistantAdviceSource.RULE_BASED;
}
