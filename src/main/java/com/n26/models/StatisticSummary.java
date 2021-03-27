package com.n26.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.text.DecimalFormat;
import java.util.DoubleSummaryStatistics;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class StatisticSummary {
    private String sum;
    private String avg;
    private String max;
    private String min;
    private long count;

    public StatisticSummary(DoubleSummaryStatistics totalStatistics) {

        this.sum = new DecimalFormat("###0.00").format(totalStatistics.getSum());
        this.avg = new DecimalFormat("###0.00").format(totalStatistics.getAverage());
        this.max = new DecimalFormat("###0.00").format(totalStatistics.getMax());
        this.min = new DecimalFormat("###0.00").format(totalStatistics.getMin());
        this.count = totalStatistics.getCount();
    }
}
