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
exports.getGroupTimes = functions.firestore
	.document('Users/{userId}')
	.onUpdate((change, context) => { 

		const oldTable = change.before.data().timetable;
		const newTable = change.after.data().timetable;

		console.log(oldTable); //oldTable is undefined if it hasnt been created before

		if(oldTable !== newTable) {
/*
			promise = admin.firestore()
            .collection('Users')
            .doc(context.params.userId)
            .get()
            .then(user => {

            	//retrieve group ids which the user is a member or coordinator of
            	if(user.exists) {
            		const coordinatedGroups = Object.keys(user.data().coordinates);
            		const memberGroups = Object.keys(user.data().isMemberOf);
            		const allGroups = memberGroups.concat(coordinatedGroups);
            		return allGroups;

            	} else {
            		throw new Error(context.params.userId + " doesn't exist?");
            	}

            }).then(groupIds => {

            	//retrieve groups which the user is a part of
            	console.log(groupIds);

            	
            	for(i = 0; i < groupIds.length; i++){
            		groupPromise = admin.firestore()
		            .collection('Groups')
		            .doc(groupIds[i])
		            .get()
		            .then(group => {

		            	if(group.exists) {
		            		print(group.data().groupName);

		            		return null;

		            	} else {
		            		throw new Error(groupIds[i] + " doesn't exist?");
		            	}
		            });

		            return groupPromise;
            	}
            	



            });
*/
			promise = admin.firestore()
            .collection('Groups')
            .where('coordinators', 'array-contains', context.params.userId)
            .get()
            .then(coordinatedGroups => {

            	groups = [];

            	coordinatedGroups.forEach(coordinatedGroup => {
            		console.log(coordinatedGroup.data().groupName)
            		groups.push(coordinatedGroup.data());
            	});


            	return admin.firestore()
	            .collection('Groups')
	            .where('members', 'array-contains', context.params.userId)
	            .get()
	            .then(normalGroups => {

	            	normalGroups.forEach(normalGroup => {
	            		console.log(normalGroup.data().groupName)
	            		groups.push(normalGroup.data());
	            	});
	            	return groups;

	            });


            }).then(groups => {

            	
            	
            })

            return promise;
		}

		return null;
	});