import { getMessaging } from "firebase-admin/messaging";
import * as logger from "firebase-functions/logger";
import {
  onDocumentCreated,
  onDocumentUpdated,
} from "firebase-functions/v2/firestore";

import { initializeApp } from "firebase-admin/app";
import { FieldPath, getFirestore } from "firebase-admin/firestore";

// Constants for safe usage.
const EVENTS_COLL = "events";
const EVENT_ENTRANTS_COLL = "eventEntrants";
const NOTIFS_COLL = "notifications";

// The key for selection limit as stored in the database.
const EVENT_SELECTION_LIMIT_KEY = "selectionLimit";

type Channel = "All" | "Winners" | "Losers" | "Cancelled";

interface Notification {
  eventId: string;
  channel: Channel;
  title: string;
  description: string;
  // TODO (chase): Don't send notification to people who have seen it!
  seenBy: string[];
}

interface EventEntrants {
  enrolledEntrants: string[];
  selectedEntrants: string[];
  acceptedEntrants: string[];
  cancelledEntrants: string[];
}

const app = initializeApp();

const db = getFirestore(app);

export const createNotification = onDocumentCreated(
  `${NOTIFS_COLL}/{notificationID}`,
  async (event) => {
    const notificationID = event.params.notificationID;
    logger.info(`Executing createNotification for ID: ${notificationID}`);
    const snapshot = event.data;
    if (!snapshot) {
      logger.warn("No data associated with document creation event");
      return;
    }
    const notif = snapshot.data() as Notification;

    const eventEntrantsDoc = await db
      .collection(EVENT_ENTRANTS_COLL)
      .doc(notif.eventId)
      .get();
    if (!eventEntrantsDoc.exists) {
      logger.error(
        `Notification references non existent event with ID: ${notif.eventId}`
      );
      return;
    }
    const eventEntrants = eventEntrantsDoc.data() as EventEntrants;

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

class BenignError extends Error {
  constructor(message: string) {
    super(message);
  }
}

// Redraw winners when someone cancels.
export const redrawSelected = onDocumentUpdated(
  `${EVENT_ENTRANTS_COLL}/{eventID}`,
  async (fsEvent) => {
    // Wish there was a way to fire only if the cancelledEntrants field was updated...
    const eventID = fsEvent.params.eventID;
    logger.info(`Executing redrawSelected for ID: ${eventID}`);
    const snapshot = fsEvent.data;
    if (!snapshot) {
      logger.warn("No data associated with document updation event");
      return;
    }
    const oldEventEntrants = snapshot.before.data() as EventEntrants;
    const newEventEntrants = snapshot.after.data() as EventEntrants;

    const oldCancelled = new Set(oldEventEntrants.cancelledEntrants);
    const newCancelled = new Set(newEventEntrants.cancelledEntrants);
    // Note: New cancelled should strictly be a superset of oldCancelled.
    // Cancelled entrants are not supposed to be removed, only added.
    const diff = newCancelled.difference(oldCancelled);
    if (diff.size === 0) {
      // No change in cancelled set. Nohing to do.
      logger.info("Nothing to do");
      return;
    }

    const eventDoc = await db.collection(EVENTS_COLL).doc(eventID).get();
    if (!eventDoc.exists) {
      logger.error(
        `EventEntrants references non existent event with ID: ${eventID}`
      );
      return;
    }
    const selectionLimit = eventDoc.get(EVENT_SELECTION_LIMIT_KEY) as number;

    // Redraw (but be wary of concurrency bugs indeed)!
    // Note: Multiple instances of this function may be called
    // around the same time if two users cancel around the same time.
    // Thus: One must be wise in implementing redraw.
    const selfRef = db
      .collection(EVENT_ENTRANTS_COLL)
      .where(FieldPath.documentId(), "==", eventID)
      // It's important to be as specific as possible in what we're reading in a transaction.
      // We don't want the transaction to be retried just because an irrelevant part of the document was updated.
      .select(entrantsKey("enrolledEntrants"), entrantsKey("selectedEntrants"), entrantsKey("cancelledEntrants"))
      .limit(1);
    db.runTransaction(async (tx) => {
      const selfEntrantsRes = await tx.get(selfRef);
      if (selfEntrantsRes.empty) {
        throw new Error("EventEntrants selfRef query found no results");
      }

      const selfEntrants = selfEntrantsRes.docs[0].data() as Omit<
        EventEntrants,
        "acceptedEntrants"
      >;
      const currentWinners = new Set(selfEntrants.selectedEntrants);
      const cancelled = new Set(selfEntrants.cancelledEntrants);
      const eligibleWinners = currentWinners.difference(cancelled);
      if (eligibleWinners.size == selectionLimit) {
        // Another concurrent redraw has run and selected new winners. Nothing to be done.
        // We must cancel the transaction by throwing an error. It should be caught later and ignored.
        throw new BenignError("Redraw has already run!");
      }

      // Our chance to shine!
      const all = new Set(selfEntrants.enrolledEntrants);
      // People who already won (including those who cancelled) are not eligible.
      const eligible = all.difference(currentWinners.union(cancelled));
      // Draw for the remaining number of slots.
      const remainingSlots = selectionLimit - eligibleWinners.size;
      const additionalWinners = draw([...eligible], remainingSlots);
      // Update the selected entrants list. It should remove the cancelled entrants and add in the new winners.
      tx.update(db.collection(EVENT_ENTRANTS_COLL).doc(eventID), {
        selectedEntrants: [...eligibleWinners.union(new Set(additionalWinners))],
      });
    }).catch((e) => {
      if (!(e instanceof BenignError)) {
        throw e;
      }
    });
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
    data: { notificationID },
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
      return getTokensByEmails(entrants.enrolledEntrants);
    case "Winners":
      return getTokensByEmails(entrants.selectedEntrants);
    case "Losers": {
      const allSet = new Set(entrants.enrolledEntrants);
      // All the cancelled entrants were also winners at one point, not loser.
      const effectiveWinnersSet = new Set(
        entrants.selectedEntrants.concat(entrants.cancelledEntrants)
      );
      return getTokensByEmails([...allSet.difference(effectiveWinnersSet)]);
    }
    case "Cancelled":
      return getTokensByEmails(entrants.cancelledEntrants);
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

// Helper to draw N elements from given input array.
function draw<T>(input: T[], selectionN: number): T[] {
  if (input.length <= selectionN) {
    // No need to draw if the entire input fits under the limit.
    return input;
  } else {
    // Otherwise, choose N random elements.
    return chooseNRandom(input, selectionN);
  }
}

// Based on: https://stackoverflow.com/a/19270021/10305477
// This is Knuth shuffle: https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
function chooseNRandom<T>(arr: T[], n: number): T[] {
  const result = new Array(n);
  let len = arr.length;
  const taken = new Array(len);
  if (n > len) {
    throw new RangeError("chooseNRandom: more elements taken than available");
  }
  while (n--) {
    const x = Math.floor(Math.random() * len);
    result[n] = arr[x in taken ? taken[x] : x];
    taken[x] = --len in taken ? taken[len] : len;
  }
  return result;
}

// Type safe way to use the property names of the EventEntrants type as string.
function entrantsKey(name: keyof EventEntrants) {
    return name;
}
