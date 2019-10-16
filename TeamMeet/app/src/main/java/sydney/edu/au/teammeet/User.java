package sydney.edu.au.teammeet;

import java.util.HashMap;

public class User {
    private String username;
    private String email;
    private String phone;
    private String photo;
    private HashMap<String, String> coordinates;
    private HashMap<String, String> isMemberOf;
    private String timetable;

    public User(){}
    public User(String username, String email, String phone, String photo, HashMap<String, String> coordinates, HashMap<String, String> isMemberOf, String timetable ){
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.photo = photo;
        this.coordinates = coordinates;
        this.isMemberOf = isMemberOf;
        this.timetable = timetable;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public HashMap<String, String> getCoordinates() {
        return coordinates;
    }

    public HashMap<String, String> getIsMemberOf() {
        return isMemberOf;
    }

    public void setCoordinates(HashMap<String, String> coordinates) {
        this.coordinates = coordinates;
    }

    public void setIsMemberOf(HashMap<String, String> isMemberOf) {
        this.isMemberOf = isMemberOf;
    }

    public boolean addToCoordinates(String groupId, String groupName) {
        if (coordinates == null) {
            setCoordinates(new HashMap<String, String>());
        }

        if (!coordinates.containsKey(groupId)) {
            coordinates.put(groupId, groupName);
            return true;
        }

        return false;
    }

    public void addToMemberOf(String groupId, String groupName) {
        if (isMemberOf == null) {
            setIsMemberOf(new HashMap<String, String>());
        }

        isMemberOf.put(groupId, groupName);
    }

    public void removeFromMembers(String groupID) {
        if (isMemberOf != null) {
            isMemberOf.remove(groupID);
        }
    }

    public void removeFromCoordinates(String groupID) {
        if (coordinates != null) {
            coordinates.remove(groupID);
        }
    }


    public String getTimetable() {
        return timetable;
    }

    public void setTimetable(String timetable) {
        this.timetable = timetable;
    }
}
