package org.elasticsearch.common.joda.Commands;


import org.joda.time.DateTimeFieldType;
import org.joda.time.MutableDateTime;

public class AdjustHourCommand extends AdjustDateFieldCommand {
    public AdjustHourCommand(int adjustmentValue) {
        super(adjustmentValue);
    }

    @Override
    public void adjust(MutableDateTime date) {
        date.addHours(adjustmentValue);
    }

    @Override
    public DateTimeFieldType getDateTimeFieldType() {
        return DateTimeFieldType.hourOfDay();
    }

    @Override
    public MutableDateTime.Property getPropertyToRound(MutableDateTime date) {
        return date.hourOfDay();
    }
}
