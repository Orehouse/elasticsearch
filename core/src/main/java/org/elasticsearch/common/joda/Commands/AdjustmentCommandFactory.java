package org.elasticsearch.common.joda.Commands;

public class AdjustmentCommandFactory {
    public AdjustYearCommand createAdjustYearCommand(int adjustmentValue){
        return new AdjustYearCommand(adjustmentValue);
    }

    public AdjustMonthCommand createAdjustMonthCommand(int adjustmentValue){
        return new AdjustMonthCommand(adjustmentValue);
    }

    public AdjustWeekCommand createAdjustWeekCommand(int adjustmentValue){
        return new AdjustWeekCommand(adjustmentValue);
    }

    public AdjustDayCommand createAdjustDayCommand(int adjustmentValue){
        return new AdjustDayCommand(adjustmentValue);
    }

    public AdjustHourCommand createAdjustHourCommand(int adjustmentValue){
        return new AdjustHourCommand(adjustmentValue);
    }

    public AdjustMinuteCommand createAdjustMinuteCommand(int adjustmentValue) {
        return new AdjustMinuteCommand(adjustmentValue);
    }

    public AdjustSecondCommand createAdjustSecondCommand(int adjustmentValue) {
        return new AdjustSecondCommand(adjustmentValue);
    }
}
