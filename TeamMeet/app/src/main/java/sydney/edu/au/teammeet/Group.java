package sydney.edu.au.teammeet;

import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Group {
    private ArrayList<String> coordinators;
    public ArrayList<String> members;
    private String groupName;

    private Map<String, ArrayList<Long>> bestTimes;
    private Long meetingDuration;
    private Long selectedMeetingTime; //with reference the bestTimes array, the nth chosen meeting time

    public Group(){}

    public Group(ArrayList<String> coordinators, ArrayList<String> members, String groupName) {
        this.coordinators = coordinators;
        this.members = members;
        this.groupName = groupName;
        this.bestTimes = new HashMap<>();
        bestTimes.put("times", new ArrayList<Long>());
        bestTimes.put("weights", new ArrayList<Long>());
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

    public void setBestTimes(Map<String, ArrayList<Long>> bestTimes) {
        this.bestTimes = bestTimes;
    }
    public Map<String, ArrayList<Long>> getBestTimes() {
        return bestTimes;
    }

    public void setMeetingDuration(Long meetingDuration) {
        this.meetingDuration = meetingDuration;
    }
    public Long getMeetingDuration() {
        return meetingDuration;
    }

    public void setSelectedMeetingTime(Long meetingTime) {
        this.selectedMeetingTime = meetingTime;
    }
    public Long getSelectedMeetingTime() {
        return selectedMeetingTime;
    }

    public void removeMember(String member) { members.remove(member); }

}