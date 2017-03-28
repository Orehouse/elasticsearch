package org.elasticsearch.common.joda.Commands;

import org.joda.time.DateTimeFieldType;
import org.joda.time.MutableDateTime;

public class AdjustYearCommand extends AdjustDateFieldCommand {
    public AdjustYearCommand(int adjustmentValue) {
        super(adjustmentValue);
    }

    @Override
    public void adjust(MutableDateTime date) {
        date.addYears(this.adjustmentValue);
    }

    @Override
    public DateTimeFieldType getDateTimeFieldType() {
        return DateTimeFieldType.yearOfCentury();
    }

    @Override
    public MutableDateTime.Property getPropertyToRound(MutableDateTime date) {
        return date.yearOfCentury();
    }
}
