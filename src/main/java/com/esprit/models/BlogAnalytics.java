package com.esprit.models;

import java.time.LocalDateTime;

/**
 * Model class for blog analytics data
 */
public class BlogAnalytics {
    private int id;
    private int blogId;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private int shareCount;
    private LocalDateTime lastUpdated;
    
    // Daily stats
    private int dailyViews;
    private int dailyLikes;
    private int dailyComments;
    private int dailyShares;
    
    // Weekly stats
    private int weeklyViews;
    private int weeklyLikes;
    private int weeklyComments;
    private int weeklyShares;
    
    // Monthly stats
    private int monthlyViews;
    private int monthlyLikes;
    private int monthlyComments;
    private int monthlyShares;

    public BlogAnalytics() {
        this.lastUpdated = LocalDateTime.now();
    }
    
    public BlogAnalytics(int blogId) {
        this.blogId = blogId;
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.shareCount = 0;
        this.lastUpdated = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBlogId() {
        return blogId;
    }

    public void setBlogId(int blogId) {
        this.blogId = blogId;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getDailyViews() {
        return dailyViews;
    }

    public void setDailyViews(int dailyViews) {
        this.dailyViews = dailyViews;
    }

    public int getDailyLikes() {
        return dailyLikes;
    }

    public void setDailyLikes(int dailyLikes) {
        this.dailyLikes = dailyLikes;
    }

    public int getDailyComments() {
        return dailyComments;
    }

    public void setDailyComments(int dailyComments) {
        this.dailyComments = dailyComments;
    }

    public int getDailyShares() {
        return dailyShares;
    }

    public void setDailyShares(int dailyShares) {
        this.dailyShares = dailyShares;
    }

    public int getWeeklyViews() {
        return weeklyViews;
    }

    public void setWeeklyViews(int weeklyViews) {
        this.weeklyViews = weeklyViews;
    }

    public int getWeeklyLikes() {
        return weeklyLikes;
    }

    public void setWeeklyLikes(int weeklyLikes) {
        this.weeklyLikes = weeklyLikes;
    }

    public int getWeeklyComments() {
        return weeklyComments;
    }

    public void setWeeklyComments(int weeklyComments) {
        this.weeklyComments = weeklyComments;
    }

    public int getWeeklyShares() {
        return weeklyShares;
    }

    public void setWeeklyShares(int weeklyShares) {
        this.weeklyShares = weeklyShares;
    }

    public int getMonthlyViews() {
        return monthlyViews;
    }

    public void setMonthlyViews(int monthlyViews) {
        this.monthlyViews = monthlyViews;
    }

    public int getMonthlyLikes() {
        return monthlyLikes;
    }

    public void setMonthlyLikes(int monthlyLikes) {
        this.monthlyLikes = monthlyLikes;
    }

    public int getMonthlyComments() {
        return monthlyComments;
    }

    public void setMonthlyComments(int monthlyComments) {
        this.monthlyComments = monthlyComments;
    }

    public int getMonthlyShares() {
        return monthlyShares;
    }

    public void setMonthlyShares(int monthlyShares) {
        this.monthlyShares = monthlyShares;
    }
}
