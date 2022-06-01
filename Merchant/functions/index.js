const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();


exports.getGoogleApiKey = functions.https.onCall(() => {

    return admin.firestore()
        .collection("admin_settings")
        .doc("google_api_key")
        .get()
        .then(function (snapshot) {

            return snapshot.data().value;
        });
});