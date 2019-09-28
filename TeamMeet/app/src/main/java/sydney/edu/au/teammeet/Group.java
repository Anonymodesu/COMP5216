package sydney.edu.au.teammeet;

import java.util.ArrayList;

public class Group {
    private ArrayList<String> coordinators;
    private ArrayList<String> members;
    private String groupName;
    private Timetable timetable;

    public Group(){}

    public Group(ArrayList<String> coordinators, ArrayList<String> members, String groupName) {
        this.coordinators = coordinators;
        this.members = members;
        this.groupName = groupName;
        this.timetable = new Timetable();
    }

    public void setCoordinators(ArrayList<String> coordinators) {
        this.coordinators = coordinators;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public ArrayList<String> getCoordinators() {
        return coordinators;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void addCoordinator(String coordinator) {
        coordinators.add(coordinator);
    }

    public void addMember(String member) {
        members.add(member);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    private void setTimetable(Timetable timetable) {
        this.timetable = timetable;
    }
}