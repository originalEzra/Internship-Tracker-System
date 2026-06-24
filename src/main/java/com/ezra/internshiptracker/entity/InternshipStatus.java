package com.ezra.internshiptracker.entity;

import java.util.Map;
import java.util.Set;

public enum InternshipStatus {
    DRAFT,
    APPLIED,
    ONLINE_ASSESSMENT,
    TECH_INTERVIEW,
    HR_INTERVIEW,
    OFFER,
    REJECTED,
    WITHDRAWN;

    private static final Map<InternshipStatus, Set<InternshipStatus>> ALLOWED_TRANSITIONS = Map.of(
            DRAFT, Set.of(APPLIED, WITHDRAWN),
            APPLIED, Set.of(ONLINE_ASSESSMENT, TECH_INTERVIEW, HR_INTERVIEW, OFFER, REJECTED, WITHDRAWN),
            ONLINE_ASSESSMENT, Set.of(TECH_INTERVIEW, HR_INTERVIEW, OFFER, REJECTED, WITHDRAWN),
            TECH_INTERVIEW, Set.of(HR_INTERVIEW, OFFER, REJECTED, WITHDRAWN),
            HR_INTERVIEW, Set.of(OFFER, REJECTED, WITHDRAWN),
            OFFER, Set.of(WITHDRAWN),
            REJECTED, Set.of(),
            WITHDRAWN, Set.of()
    );

    public boolean canTransitionTo(InternshipStatus nextStatus) {
        return this == nextStatus || ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(nextStatus);
    }
}
