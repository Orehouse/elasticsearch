package org.elasticsearch.common.joda.Commands;

import org.joda.time.DateTimeFieldType;
import org.joda.time.MutableDateTime;

public class AdjustWeekCommand extends AdjustDateFieldCommand {
    public AdjustWeekCommand(int adjustmentValue) {
        super(adjustmentValue);
    }

    @Override
    public void adjust(MutableDateTime date) {
        date.addWeeks(this.adjustmentValue);
    }

    @Override
    public DateTimeFieldType getDateTimeFieldType() {
        return DateTimeFieldType.weekOfWeekyear();
    }

    @Override
    public MutableDateTime.Property getPropertyToRound(MutableDateTime date) {
        return date.weekOfWeekyear();
    }
}
