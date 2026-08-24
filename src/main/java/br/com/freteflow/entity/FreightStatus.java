package br.com.freteflow.entity;

import java.util.Set;

public enum FreightStatus {
    PENDING,
    IN_PROGRESS,
    DELIVERED,
    CANCELED;

    public boolean canTransitionTo(FreightStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == IN_PROGRESS || newStatus == CANCELED;
            case IN_PROGRESS -> newStatus == DELIVERED || newStatus == CANCELED;
            case DELIVERED, CANCELED -> false;
        };
    }
}
