package sydney.edu.au.teammeet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class GroupMeetingAdapter extends ArrayAdapter<GroupMeetingAdapter.Meeting> {

    public static class Meeting {
        public final int time;
        public final int weighting;

        public Meeting(int time, int weighting) {
            this.time = time;
            this.weighting = weighting;
        }
    }

    public GroupMeetingAdapter(Context context, ArrayList<Meeting> meetings) {
        super(context, R.layout.group_meeting_timeslot, meetings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Meeting meeting = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.group_meeting_timeslot, parent, false);
        }
        // Lookup view for data population
        TextView meetingTime = (TextView) convertView.findViewById(R.id.meeting_time);
        TextView meetingWeighting = (TextView) convertView.findViewById(R.id.meeting_weighting);
        // Populate the data into the template view using the data object
        meetingTime.setText(timeToString(meeting.time));
        meetingWeighting.setText("Conflict Weighting: " + meeting.weighting);
        // Return the completed view to render on screen
        return convertView;
    }

    private static String timeToString(int index) {
        String[] dayStrings = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int col = index % Timetable.NUM_DAYS;
        int row = index / Timetable.NUM_DAYS;
        int hour = Timetable.START_HOUR + row / 2;
        String halfHourString = (row % 2 == 0) ? "00" : "30";

        return String.format("%s %d:%s", dayStrings[col], hour, halfHourString);
    }
}
