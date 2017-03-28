package org.elasticsearch.common.joda.Commands;


import org.joda.time.DateTimeFieldType;
import org.joda.time.MutableDateTime;

public class AdjustSecondCommand extends AdjustDateFieldCommand {
    public AdjustSecondCommand(int adjustmentValue) {
        super(adjustmentValue);
    }

    @Override
    public void adjust(MutableDateTime date) {
        date.addSeconds(adjustmentValue);
    }

    @Override
    public DateTimeFieldType getDateTimeFieldType() {
        return DateTimeFieldType.secondOfMinute();
    }

    @Override
    public MutableDateTime.Property getPropertyToRound(MutableDateTime date) {
        return date.secondOfMinute();
    }
}
