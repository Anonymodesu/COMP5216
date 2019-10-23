const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

/*
// Take the text parameter passed to this HTTP endpoint and insert it into the
// Realtime Database under the path /messages/:pushId/original
exports.addMessage = functions.https.onRequest(async (req, res) => {
  // Grab the text parameter.
  const original = req.query.text;
  // Push the new message into the Realtime Database using the Firebase Admin SDK.
  const snapshot = await admin.database().ref('/messages').push({original: original});
  // Redirect with 303 SEE OTHER to the URL of the pushed object in the Firebase console.
  res.redirect(303, snapshot.ref.toString());
});

// Listens for new messages added to /messages/:pushId/original and creates an
// uppercase version of the message to /messages/:pushId/uppercase
exports.makeUppercase = functions.database.ref('/messages/{pushId}/original')
    .onCreate((snapshot, context) => {
      // Grab the current value of what was written to the Realtime Database.
      const original = snapshot.val();
      console.log('Uppercasing', context.params.pushId, original);
      const uppercase = original.toUpperCase();
      // You must return a Promise when performing asynchronous tasks inside a Functions such as
      // writing to the Firebase Realtime Database.
      // Setting an "uppercase" sibling in the Realtime Database returns a Promise.
      return snapshot.ref.parent.child('uppercase').set(uppercase);
    });
*/
exports.getGroupTimes = functions.https.onCall((data, context) => {

	//retrieve data from client
	const duration = data.duration;
	const numTimes = data.numTimes;
	const timetableLength = data.timetableLength;
	const groupID = data.groupID;

	function sortPriorities(priorities) {
		// Create items array
		var items = Object.keys(priorities).map(function(key) {
		return [Number(key), priorities[key]];
		});

		// Sort the array based on the second element
		items.sort((first, second) => {
		return first[1] - second[1];
		});

		return items
	}

	//returns a dictionary mapping meeting start times to their weighting
	//each personal timetable should have the same length
	//group times indicate timeslots that should not be considered for a group meeting
	//additionalTimes = number of total timeslots - 1
	function timetableWeights(personalTimetables, additionalTimes) {
		const numDays = 7; //adding numDays to an index increments the timeslot by 1 position forward in time
		//adding 1 to an index increments the timeslot 1 day forward in time

		//load database with initial values
		var priorities = {};
		for(var i = 0; i < timetableLength - additionalTimes * numDays; i++) {
			priorities[i] = 0;
		}

		//compile each personal timetable's weightings
		for(var j = 0; j < personalTimetables.length; j++) {
		var currentTimetable = personalTimetables[j];

			for(var startTime = 0; startTime < timetableLength  - additionalTimes * numDays; startTime++) {
				for(var timeslot = 0; timeslot <= additionalTimes ; timeslot++) {
					priorities[startTime] += currentTimetable[startTime + timeslot * numDays];
				}
			}
		}

		return priorities;
	}


	var db = admin.firestore();

	return db
	.collection('Groups')
	.doc(groupID)
	.get()
	.then(group => { //here we retrieve all the coordinator and member docs of the group

		const memberIDs = group.data().coordinators.concat(group.data().members);
		var memberDocs = []

		function pushMember(index) { //need a double function closure here since index doesn't update properly in for loop
			return function(user) {
				memberDocs.push(user);

				return db
				.collection('Users')
				.doc(memberIDs[index])
				.get()
			}
		}

		var promise = db
		.collection('Users')
		.doc(memberIDs[0])
		.get();

		for(i = 1; i < memberIDs.length; i++) { //chain promises in for loop
			promise = promise.then(pushMember(i));
		}

		promise = promise.then(user => {
			memberDocs.push(user);
			return memberDocs;
		})

		return promise;

	}).then(members => { //calculate timeslot priorities to return to client app
		var personalTimetables = [];

		for(i = 0; i < members.length; i++) {
			const timetable = members[i].data().timetable;

			//newly created users have an empty timetable; they can be ignored
			if(typeof(timetable) !== "undefined" && timetable !== null) {
				personalTimetables.push(JSON.parse(timetable).availabilities)
			}
		}

		var priorities = timetableWeights(personalTimetables, duration - 1);
		priorities = sortPriorities(priorities);
		console.log(priorities);

		var times = [];
		var weights = [];
		for(j = 0; j < numTimes; j++) {
			times.push(priorities[j][0]);
			weights.push(priorities[j][1]);
		}

		const bestTimeslots = {
			times : times,
			weights : weights
		};

		return db
		.collection('Groups')
		.doc(groupID)
		.update({
			bestTimes : bestTimeslots,
			meetingDuration : duration,
			selectedMeetingTime : -1 //-1 indicates that no meeting time has been selected
		})
	});

});



exports.timetableClash = functions.firestore
	.document('Users/{userId}')
	.onUpdate((change, context) => { 

		function arrayEquals(arr1, arr2) {
			for(i = 0; i < arr1.length; i++) {
				if(arr1[i] !== arr2[i]) {
					return false;
				}
			}

			return true;
		}

		var oldTable = change.before.data().timetable;
		var newTable = change.after.data().timetable;

		/*
		if(typeof(oldTable) === "undefined" || oldTable === null) {
			oldTable 
		}

		if(oldTable !== newTable) {

		}
		*/

		return null;
	});