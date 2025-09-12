package com.rocmine.wip.hydrify.model;

public class UserProfile {
    private long id;
    private double weight;
    private String gender;
    private int age;
    private double height;
    private String wakeTime;
    private String sleepTime;
    private int dailyGoal;

    // Default constructor
    public UserProfile() {}

    public UserProfile(String gender, int age, double height, String wakeTime, String sleepTime) {
        this.gender = gender;
        this.age = age;
        this.height = height;
        this.wakeTime = wakeTime;
        this.sleepTime = sleepTime;
    }

    public UserProfile(long id, double weight, String gender, int age, double height, String wakeTime, String sleepTime, int dailyGoal) {
        this.id = id;
        this.weight = weight;
        this.gender = gender;
        this.age = age;
        this.height = height;
        this.wakeTime = wakeTime;
        this.sleepTime = sleepTime;
        this.dailyGoal = dailyGoal;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getWakeTime() { return wakeTime; }
    public void setWakeTime(String wakeTime) { this.wakeTime = wakeTime; }
    
    public String getSleepTime() { return sleepTime; }
    public void setSleepTime(String sleepTime) { this.sleepTime = sleepTime; }
    
    public int getDailyGoal() { return dailyGoal; }
    public void setDailyGoal(int dailyGoal) { this.dailyGoal = dailyGoal; }
} 