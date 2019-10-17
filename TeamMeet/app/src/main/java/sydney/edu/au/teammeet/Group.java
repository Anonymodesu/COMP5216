package sydney.edu.au.teammeet;

import java.util.ArrayList;

public class Group {
    private ArrayList<String> coordinators;
    public ArrayList<String> members;
    private String groupName;
    private int[] timetable;

    public Group(){}

    public Group(ArrayList<String> coordinators, ArrayList<String> members, String groupName) {
        this.coordinators = coordinators;
        this.members = members;
        this.groupName = groupName;
        this.timetable = new int[Timetable.NUM_CELLS];
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

    public boolean addCoordinator(String coordinator) {
        if (!coordinators.contains(coordinator)) {
            coordinators.add(coordinator);
            return true;
        }

        return false;
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

    private void setTimetable(int[] timetable) {
        this.timetable = timetable;
    }

    private int[] getTimetable(int[] timetable) {
        return timetable;
    }

    public void removeMember(String member) { members.remove(member); }

}