package org.elasticsearch.common.joda.Commands;

import org.joda.time.DateTimeFieldType;
import org.joda.time.MutableDateTime;

public class AdjustDayCommand extends AdjustDateFieldCommand {
    public AdjustDayCommand(int adjustmentValue) {
        super(adjustmentValue);
    }

    @Override
    public void adjust(MutableDateTime date) {
        date.addDays(this.adjustmentValue);
    }

    @Override
    public DateTimeFieldType getDateTimeFieldType() {
        return DateTimeFieldType.dayOfMonth();
    }

    @Override
    public MutableDateTime.Property getPropertyToRound(MutableDateTime date) {
        return date.dayOfMonth();
    }
}
