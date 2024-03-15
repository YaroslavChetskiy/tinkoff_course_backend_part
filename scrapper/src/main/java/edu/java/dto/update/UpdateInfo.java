package edu.java.dto.update;

import java.time.OffsetDateTime;

public record UpdateInfo(boolean isNewUpdate, OffsetDateTime updateTime, String message) {
}
