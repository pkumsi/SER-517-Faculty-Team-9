package com.example.carma_android_app.models;

/**
 * Review item for displaying daily message statistics
 */
public class StatisticsReviewItem extends ReviewItem {
    private DailyMessageCount dailyCount;

    public StatisticsReviewItem(DailyMessageCount dailyCount) {
        super(ItemType.STATISTICS);
        this.dailyCount = dailyCount;
    }

    public DailyMessageCount getDailyCount() {
        return dailyCount;
    }

    public void setDailyCount(DailyMessageCount dailyCount) {
        this.dailyCount = dailyCount;
    }

    @Override
    public String getDisplayText() {
        return dailyCount.getDisplayText();
    }
}