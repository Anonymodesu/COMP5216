package sydney.edu.au.teammeet;

import java.util.HashMap;

public class User {
    private String username;
    private String email;
    private String phone;
    private String photo;
    private HashMap<String, String> coordinates;
    private HashMap<String, String> isMemberOf;

    public User(){}
    public User(String username, String email, String phone, String photo, HashMap<String, String> coordinates, HashMap<String, String> isMemberOf ){
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.photo = photo;
        this.coordinates = coordinates;
        this.isMemberOf = isMemberOf;
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

    public void addToCoordinates(String groupId, String groupName) {
        if (coordinates == null) {
            setCoordinates(new HashMap<String, String>());
        }

        coordinates.put(groupId, groupName);
    }

    public void addToMemberOf(String groupId, String groupName) {
        if (isMemberOf == null) {
            setIsMemberOf(new HashMap<String, String>());
        }

        isMemberOf.put(groupId, groupName);
    }
}