package org.elasticsearch.common.joda.Commands;

import org.joda.time.DateTimeFieldType;
import org.joda.time.MutableDateTime;

import java.util.Arrays;
import java.util.List;

public abstract class AdjustDateFieldCommand implements Comparable {
    public AdjustDateFieldCommand(int adjustmentValue) {
        this.adjustmentValue = adjustmentValue;
        this.orderedDateFieldTypes = Arrays.asList(new DateTimeFieldType[] {

        });
    }

    List<DateTimeFieldType> orderedDateFieldTypes;

    protected int adjustmentValue;

    public abstract void adjust(MutableDateTime date);

    @Override
    public int compareTo(Object o) {
        AdjustDateFieldCommand other = (AdjustDateFieldCommand)o;
        DateTimeFieldType otherFieldType = other.getDateTimeFieldType();
        int currentIndex = this.orderedDateFieldTypes.indexOf(this.getDateTimeFieldType());
        int otherIndex = this.orderedDateFieldTypes.indexOf(otherFieldType);
        if (currentIndex == otherIndex) {
            return 0;
        } else if (currentIndex < otherIndex) {
            return -1;
        } else {
            return 1;
        }
    }

    public abstract DateTimeFieldType getDateTimeFieldType();

    public abstract MutableDateTime.Property getPropertyToRound(MutableDateTime date);
}
