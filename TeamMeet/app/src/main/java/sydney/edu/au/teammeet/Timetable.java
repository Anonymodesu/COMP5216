package sydney.edu.au.teammeet;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.litepal.crud.LitePalSupport;

public class Timetable extends LitePalSupport{

    public static final int START_HOUR = 8;
    public static final int NUM_DAYS = 7;
    public static final int NUM_HALF_HOURS = 24;
    public static final int NUM_CELLS = NUM_DAYS * NUM_HALF_HOURS;

    private final String[] activities;

    private final int[] availabilities;

    public Timetable() {

        activities = new String[NUM_CELLS];
        availabilities = new int[NUM_CELLS];

        for(int i = 0; i < availabilities.length; i++) {
            availabilities[i] = i % 4;
        }

        for(int i = 0; i < activities.length; i++) {
            activities[i] = null;
        }
    }

    public int getLength(){
        return NUM_CELLS;
    }

    public String getActivity(int day, int halfHour) {
        return activities[getIndex(day, halfHour)];
    }

    public String getActivity(int index) {
        return activities[index];
    }

    public int getWeighting(int day, int halfHour) {
        return availabilities[getIndex(day, halfHour)];
    }

    public int getWeighting(int index) {
        return availabilities[index];
    }

    public void setWeighting(int index, int weight) {
        availabilities[index] = weight;
    }

    public void setActivity(int index, String activity) {
        activities[index] = activity;
    }

    private int getIndex(int day, int halfHour) {
        return day * NUM_HALF_HOURS + halfHour;
    }


}
