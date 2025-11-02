import { getMessaging } from "firebase-admin/messaging";
import * as logger from "firebase-functions/logger";
import { onDocumentCreated } from "firebase-functions/v2/firestore";

import * as admin from "firebase-admin";

type Channel = "All" | "Winners" | "Cancelled";

// TODO (chase): Make sure this lines up with notifications DB integration work once done.
interface Notification {
  eventId: string;
  channel: Channel;
  title: string;
  description: string;
  // TODO (chase): Don't send notification to people who have seen it!
  seenBy: string[];
}

// TODO (chase): Verify collection structure.
interface EventEntrants {
  entrants: string[];
  winners: string[];
  cancelled: string[];
}

// The activity to start on notification click.
const INTENT_ACTIVITY = "AuthActivity";

admin.initializeApp();

const db = admin.firestore();

export const createNotification = onDocumentCreated(
  "notifications/{notificationID}",
  async (event) => {
    const notificationID = event.params.notificationID;
    logger.info(`Executing createNotification for ID: ${notificationID}`);
    const snapshot = event.data;
    if (!snapshot) {
      logger.warn("No data associated with document creation event");
      return;
    }
    const notif = snapshot.data() as Notification;

    // TODO (chase): Verify collection name.
    const eventDoc = await db.collection("events").doc(notif.eventId).get();
    if (!eventDoc.exists) {
      logger.error(
        `Notification references non existent event with ID: ${notif.eventId}`
      );
      return;
    }
    const eventEntrants = eventDoc.data() as EventEntrants;

    const tokens = await getTokensForChannel(notif.channel, eventEntrants);
    logger.info(`Sending to ${tokens.length} devices`);
    await sendNotification(
      tokens,
      notificationID,
      notif.title,
      notif.description
    );
    logger.info("Notifications sent!");
  }
);

// Send push notification to devices identified by given tokens.
async function sendNotification(
  registrationTokens: string[],
  notificationID: string,
  title: string,
  body: string
) {
  const message = {
    tokens: registrationTokens,
    notification: { title, body },
    data: { id: notificationID },
    android: {
      notification: {
        clickAction: INTENT_ACTIVITY,
      },
    },
  };

  const response = await getMessaging().sendEachForMulticast(message);
  if (response.failureCount > 0) {
    const failedTokens: string[] = [];
    response.responses.forEach((resp, idx) => {
      if (!resp.success) {
        failedTokens.push(registrationTokens[idx]);
      }
    });
    logger.error("List of tokens that caused failures: " + failedTokens);
  }
}

// Get tokens for emails that belong to a particular channel.
// Note: This will drop any participant emails for which tokens are not found. See error log.
async function getTokensForChannel(
  channel: Channel,
  entrants: EventEntrants
): Promise<string[]> {
  switch (channel) {
    case "All":
      return getTokensByEmails(entrants.entrants);
    case "Winners":
      return getTokensByEmails(entrants.winners);
    case "Cancelled":
      return getTokensByEmails(entrants.cancelled);
  }
}

// Note: This drops emails for which tokens are not found but does log an error.
async function getTokensByEmails(emails: string[]): Promise<string[]> {
  const res = await Promise.all(
    emails.map((email) =>
      db
        .collection("fcmTokens")
        .doc(email)
        .get()
        .then((tokenDoc) => {
          if (tokenDoc.exists) {
            // TODO (chase): Verify schema.
            const r = tokenDoc.data() as { token: string };
            return [r.token];
          } else {
            logger.error(`No token found for email: ${email}`);
            return [];
          }
        })
    )
  );
  return res.flat();
}
