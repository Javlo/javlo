package org.javlo.component.meta;

import org.javlo.context.ContentContext;

import java.time.LocalDateTime;

public interface ITimeRange {

    default public boolean isTimeRangeValid(ContentContext ctx) {
        return getTimeRangeStart(ctx) != null && getTimeRangeEnd(ctx) != null;
    }

    public LocalDateTime getTimeRangeStart(ContentContext ctx);

    public LocalDateTime getTimeRangeEnd(ContentContext ctx);
}
