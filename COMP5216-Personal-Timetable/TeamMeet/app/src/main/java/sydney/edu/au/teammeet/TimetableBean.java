package sydney.edu.au.teammeet;

import org.litepal.crud.LitePalSupport;

public class TimetableBean extends LitePalSupport {

    //private int timetableID; //timelots index
    private String activities;
    private int weight;

    /*public int getTimetableID(){
        return timetableID;
    }

    public void setTimetableID(int timetableID){
        this.timetableID = timetableID;
    }*/

    public String getActivities(){
        return activities;
    }

    public void setActivities(String activities){
        this.activities = activities;
    }

    public int getWeighting(){
        return weight;
    }

    public void setWeighting(int weight){
        this.weight = weight;
    }
}
