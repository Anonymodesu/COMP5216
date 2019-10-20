package sydney.edu.au.teammeet;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class GroupMeetingAdapter extends ArrayAdapter<GroupMeetingAdapter.Meeting> {

    private final int duration;
    private int meetingAdapterPosition;

    public static class Meeting {
        public final int time;
        public final int weighting;

        public Meeting(int time, int weighting) {
            this.time = time;
            this.weighting = weighting;
        }
    }

    public GroupMeetingAdapter(Context context, ArrayList<Meeting> meetings, int duration) {
        super(context, R.layout.group_meeting_timeslot, meetings);
        this.duration = duration;
        meetingAdapterPosition = -1;
    }

    //position refers to position in adapter, not timetable position
    public void selectMeetingTime(int position) {
        meetingAdapterPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Meeting meeting = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.group_meeting_timeslot, parent, false);
        }

        //highlight selected meeting time
        int colour = ContextCompat.getColor(getContext(), R.color.normal_meeting_colour);
        if(meetingAdapterPosition == position) {
            colour = ContextCompat.getColor(getContext(), R.color.selected_meeting_colour);
        }
        convertView.setBackgroundColor(colour);

        // Lookup view for data population
        TextView meetingTime = (TextView) convertView.findViewById(R.id.meeting_time);
        TextView meetingWeighting = (TextView) convertView.findViewById(R.id.meeting_weighting);
        // Populate the data into the template view using the data object
        meetingTime.setText(timeToString(meeting.time, duration));
        meetingWeighting.setText("Conflict Level: " + meeting.weighting);
        // Return the completed view to render on screen
        return convertView;
    }

    //index refers to the meeting start position in reference to the personal timetable
    //not in reference to the list view
    //rows are times, columns are days
    public static String timeToString(int index, int duration) {
        String[] dayStrings = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int col = index % Timetable.NUM_DAYS;

        int startRow = index / Timetable.NUM_DAYS;
        int startHour = Timetable.START_HOUR + startRow / 2;
        String startHalfHourString = (startRow % 2 == 0) ? "00" : "30";

        int endRow = startRow + duration;
        int endHour = Timetable.START_HOUR + endRow / 2;
        String endHalfHourString = (endRow % 2 == 0) ? "00" : "30";

        return String.format("%s %d:%s to %d:%s", dayStrings[col], startHour, startHalfHourString, endHour, endHalfHourString);
    }
}
