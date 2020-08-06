TeamMeet
---
We propose a mobile application, TeamMeet, to address the issues of existing group management applications. The app GUI is designed to be intuitive and easy to use. Once a user’s personal information is set up, there is little need for further user input because group meetings will automatically be arranged by the app. We target TeamMeet towards university students, although it can be used in any group setting.

App functionalities 
---
- When users first download the app, they create a user account (email - in case of forgotten password, username, screen name, password, confirm password). They subsequently log in with their username and password.
- Once logged in, users can edit their personal timetable with a view similar to that of the USYD Personal Timetable; they can weight timeslots based on their priority.
- Users can edit their display name, phone number and profile picture.
- At least one person has to coordinate the group - they create a new group, give it a name and add members to it by entering their email. 
- Users have the ability to switch between groups and leave groups. Coordinators have the ability to add people, delete people from groups, grant coordinator privileges to other members of the group and delete the whole group. They also can specify the length of the meetings they wish to hold, and can recollect timetables as information is added/modified (e.g. new person is added, length of meeting changes)
- A deterministic scheduling algorithm (guarantees the same times given the same input) provides a list of the best meeting time(s) for the coordinators to choose from. When group members make changes to their personal timetable that result in a clash with this list, coordinators should get a notification prompting them to recollect a new list of best meeting times. 
