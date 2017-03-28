package org.elasticsearch.common.joda.Commands;

import org.joda.time.DateTimeFieldType;
import org.joda.time.MutableDateTime;

public class AdjustMonthCommand extends AdjustDateFieldCommand {
    public AdjustMonthCommand(int adjustmentValue) {
        super(adjustmentValue);
    }

    @Override
    public void adjust(MutableDateTime date) {
        date.addMonths(this.adjustmentValue);
    }

    @Override
    public DateTimeFieldType getDateTimeFieldType() {
        return DateTimeFieldType.monthOfYear();
    }

    @Override
    public MutableDateTime.Property getPropertyToRound(MutableDateTime date) {
        return date.monthOfYear();
    }
}
