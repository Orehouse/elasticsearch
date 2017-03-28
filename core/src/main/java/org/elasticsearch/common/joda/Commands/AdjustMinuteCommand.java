package org.elasticsearch.common.joda.Commands;


import org.joda.time.DateTimeFieldType;
import org.joda.time.MutableDateTime;

public class AdjustMinuteCommand extends AdjustDateFieldCommand {
    public AdjustMinuteCommand(int adjustmentValue) {
        super(adjustmentValue);
    }

    @Override
    public void adjust(MutableDateTime date) {
        date.addMinutes(adjustmentValue);
    }

    @Override
    public DateTimeFieldType getDateTimeFieldType() {
        return DateTimeFieldType.minuteOfHour();
    }

    @Override
    public MutableDateTime.Property getPropertyToRound(MutableDateTime date) {
        return date.minuteOfHour();
    }
}
