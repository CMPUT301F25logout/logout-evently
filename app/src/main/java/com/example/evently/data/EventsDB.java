package com.example.evently.data;

import static com.example.evently.data.generic.Promise.promise;
import static com.example.evently.data.generic.PromiseOpt.promiseOpt;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.TestOnly;

import com.example.evently.data.generic.Promise;
import com.example.evently.data.generic.PromiseOpt;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.EventEntrants;
import com.example.evently.data.model.EventFilter;

/**
 * The Event database for managing events
 * @author Ronan St. Amand
 */
public class EventsDB {
    private final FirebaseFirestore db;
    private final CollectionReference eventsRef;
    private final CollectionReference eventEntrantsRef;
    private final StorageReference storageRef;

    /**
     * Constructor for EventsDB
     */
    public EventsDB() {
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        eventEntrantsRef = db.collection("eventEntrants");
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    /**
     * Gets an event from a DocumentSnapshot if able
     * @param documentSnapshot DocumentSnapshot to retrieve event from
     * @return An optional with the retrieved event if one was retrieved
     * @throws NullPointerException When documentSnapshot has an incorrectly stored event
     */
    private static Optional<Event> getEventFromSnapshot(DocumentSnapshot documentSnapshot)
            throws NullPointerException {

        if (!documentSnapshot.exists()) return Optional.empty();

        Optional<Long> optionalEntrantLimit =
                Optional.ofNullable(documentSnapshot.getLong("entrantLimit"));

        return Optional.of(new Event(
                UUID.fromString(documentSnapshot.getId()),
                documentSnapshot.getString("name"),
                documentSnapshot.getString("description"),
                Category.valueOf(documentSnapshot.getString("category")),
                Objects.requireNonNullElse(documentSnapshot.getBoolean("requiresLocation"), false),
                documentSnapshot.getTimestamp("selectionTime"),
                documentSnapshot.getTimestamp("eventTime"),
                documentSnapshot.getString("organizer"),
                documentSnapshot.getLong("selectionLimit"),
                optionalEntrantLimit,
                Objects.requireNonNullElse(documentSnapshot.getBoolean("isFull"), false)));
    }

    /**
     * Gets an event from a DocumentSnapshot if able
     * @param documentSnapshot DocumentSnapshot to retrieve EventEntrants from
     * @return An optional with the retrieved event entrants struct if one was retrieved
     * @throws NullPointerException When documentSnapshot has an incorrectly stored event entrants struct.
     */
    private static Optional<EventEntrants> getEventEntrantsFromSnapshot(
            DocumentSnapshot documentSnapshot) throws NullPointerException {

        if (!documentSnapshot.exists()) return Optional.empty();

        return Optional.of(new EventEntrants(
                UUID.fromString(documentSnapshot.getId()),
                (List<String>) documentSnapshot.get("enrolledEntrants"),
                (List<String>) documentSnapshot.get("selectedEntrants"),
                (List<String>) documentSnapshot.get("acceptedEntrants"),
                (List<String>) documentSnapshot.get("cancelledEntrants"),
                (HashMap<String, GeoPoint>) Objects.requireNonNullElse(
                        documentSnapshot.get("entrantLocations"), new HashMap<>())));
    }

    /**
     * Stores an event in the database.
     * @param event event to be stored
     */
    public Promise<Void> storeEvent(Event event) {
        DocumentReference docRef = eventsRef.document(event.eventID().toString());
        return promise(docRef.set(event.toHashMap())).then(x -> {
            DocumentReference ref = eventEntrantsRef.document(event.eventID().toString());
            return promise(ref.set(new EventEntrants(event.eventID()).toHashMap()));
        });
    }

    /**
     * Add a user to the enrolled list of an event.
     * @param eventID Target event.
     * @param email Email of the user to enroll.
     */
    public Promise<Void> enroll(UUID eventID, String email) {
        return enroll(eventID, email, null);
    }

    /**
     * Add a user, alongside their current location, to the enrolled list of an event.
     * @param eventID Target event.
     * @param email Email of the user to enroll.
     * @param entrantLocation Location from where the entrant enrolled.
     */
    public Promise<Void> enroll(UUID eventID, String email, GeoPoint entrantLocation) {
        final var eventIDStr = eventID.toString();
        final var targetEventRef = eventsRef.document(eventIDStr);
        final var targetEventEntrantsRef = eventEntrantsRef.document(eventIDStr);
        return promise(FirebaseFirestore.getInstance().runTransaction(tx -> {
            // We must verify some information and run two updates to enroll.
            // All of these must pass at once in order for enroll to be successful.
            // We must use a transaction to ensure no half success and no race condition from other
            // people enrolling.
            final var event = getEventFromSnapshot(tx.get(targetEventRef)).orElseThrow();
            if (event.isFull()) {
                throw new IllegalArgumentException("Event is full");
            }
            if (event.selectionTime().toInstant().isBefore(Instant.now())) {
                throw new IllegalStateException("Event selection time has passed");
            }
            final var eventEntrants =
                    getEventEntrantsFromSnapshot(tx.get(targetEventEntrantsRef)).orElseThrow();

            // Add the user to the enrolled list.
            final var entrantUpdateMap = addEntrantUpdateObj(email, "enrolledEntrants");
            // Add the location if provided.
            if (entrantLocation != null) {
                entrantUpdateMap.put(FieldPath.of("entrantLocations", email), entrantLocation);
            }
            fieldPathUpdate(tx, targetEventEntrantsRef, entrantUpdateMap);
            // Mark the event full if we've hit the limit.
            event.optionalEntrantLimit().ifPresent(limit -> {
                if (eventEntrants.all().size() + 1 >= limit) {
                    final var eventUpdateMap = new HashMap<String, Object>();
                    eventUpdateMap.put("isFull", true);
                    tx.update(targetEventRef, eventUpdateMap);
                }
            });

            // Success!
            return null;
        }));
    }

    /**
     * Enroll without checking any conditions. Only for testing.
     * @param eventID Target event.
     * @param email Email of the user to enroll.
     */
    @TestOnly
    public Promise<Void> unsafeEnroll(UUID eventID, String email) {
        return unsafeEnroll(eventID, email, null);
    }

    /**
     * Enroll without checking any conditions. Only for testing.
     * @param eventID Target event.
     * @param email Email of the user to enroll.
     * @param entrantLocation Location from where the entrant enrolled.
     */
    @TestOnly
    public Promise<Void> unsafeEnroll(UUID eventID, String email, GeoPoint entrantLocation) {
        // Add the user to the enrolled list.
        final var entrantUpdateMap = addEntrantUpdateObj(email, "enrolledEntrants");
        // Add the location if provided.
        if (entrantLocation != null) {
            entrantUpdateMap.put(FieldPath.of("entrantLocations", email), entrantLocation);
        }
        return fieldPathUpdate(eventEntrantsRef.document(eventID.toString()), entrantUpdateMap);
    }

    /**
     * Remove user from waitlist of event.
     * @param eventID Target event.
     * @param email Target user email
     * @return Promise.
     */
    public Promise<Void> unenroll(UUID eventID, String email) {
        return removeEntrantFromList(eventID, email, "enrolledEntrants");
    }

    /**
     * Add a user to the selected list of an event.
     * @param eventID Target event.
     * @param email Email of the user to enroll.
     */
    public Promise<Void> addSelected(UUID eventID, String email) {
        return addEntrantToList(eventID, email, "selectedEntrants");
    }

    /**
     * Remove user from selected list of event and add them to cancelled.
     * @param eventID Target event.
     * @param email Target user email
     * @return Promise.
     */
    public Promise<Void> cancelSelectedUser(UUID eventID, String email) {
        final var updateMap = removeEntrantUpdateObj(email, "selectedEntrants");
        final var additionUpdate = addEntrantUpdateObj(email, "cancelledEntrants");
        updateMap.putAll(additionUpdate);
        return fieldPathUpdate(eventEntrantsRef.document(eventID.toString()), updateMap);
    }

    /**
     * Add a user to the accepted list of an event.
     * @param eventID Target event.
     * @param email Email of the user to enroll.
     */
    public Promise<Void> addAccepted(UUID eventID, String email) {
        return addEntrantToList(eventID, email, "acceptedEntrants");
    }

    /**
     * Add a user to the cancelled list of an event.
     * @param eventID Target event.
     * @param email Email of the user to enroll.
     */
    public Promise<Void> addCancelled(UUID eventID, String email) {
        return addEntrantToList(eventID, email, "cancelledEntrants");
    }

    // Helper to add a user to one of the lists.
    private Promise<Void> addEntrantToList(UUID eventID, String email, String field) {
        final var updateMap = addEntrantUpdateObj(email, field);
        return fieldPathUpdate(eventEntrantsRef.document(eventID.toString()), updateMap);
    }

    private HashMap<FieldPath, Object> addEntrantUpdateObj(String email, String field) {
        final var updateMap = new HashMap<FieldPath, Object>();
        updateMap.put(FieldPath.of(field), FieldValue.arrayUnion(email));
        return updateMap;
    }

    private HashMap<FieldPath, Object> removeEntrantUpdateObj(String email, String field) {
        final var updateMap = new HashMap<FieldPath, Object>();
        updateMap.put(FieldPath.of(field), FieldValue.arrayRemove(email));
        return updateMap;
    }

    // Helper to remove a user from one of the lists
    private Promise<Void> removeEntrantFromList(UUID eventID, String email, String field) {
        final var updateMap = removeEntrantUpdateObj(email, field);
        return fieldPathUpdate(eventEntrantsRef.document(eventID.toString()), updateMap);
    }

    /**
     * Fetch an event from database by UUID.
     * @param eventID UUID of the event
     */
    public PromiseOpt<Event> fetchEvent(UUID eventID) {
        return promiseOpt(promise(eventsRef.document(eventID.toString()).get())
                .map(EventsDB::getEventFromSnapshot));
    }

    /**
     * Fetch events from database by organizer email.
     * @param organizer email of the event's organizer
     */
    public Promise<List<Event>> fetchEventsByOrganizers(String organizer) {
        return promise(eventsRef.whereEqualTo("organizer", organizer).get())
                .map(querySnapshot -> querySnapshot.getDocuments().stream()
                        .map(EventsDB::getEventFromSnapshot)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }

    /**
     * Fetch events before or after a given date
     * @param dateConstraint date to constrain events by
     * @param isStart {@code true} for events after constraint, {@code false} for events before.
     */
    public Promise<List<Event>> fetchEventsByDate(Timestamp dateConstraint, boolean isStart) {
        final Query query;
        if (isStart) {
            query = eventsRef.whereGreaterThan("eventTime", dateConstraint);
        } else {
            query = eventsRef.whereLessThan("eventTime", dateConstraint);
        }
        return parseQuerySnapShots(query.get());
    }

    /**
     * Gets all events for admin viewing purposes
     * @return A list of events
     */
    public Promise<List<Event>> fetchAllEvents() {
        return promise(eventsRef.get()).map(querySnapshot -> querySnapshot.getDocuments().stream()
                .map(EventsDB::getEventFromSnapshot)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()));
    }

    /**
     * @param filters Filters to apply on the events.
     * @return All currently open (for enrollment) events as per given filters.
     */
    public Promise<List<Event>> fetchEventByFilters(EventFilter filters) {
        var query = eventsRef.whereGreaterThan("selectionTime", Timestamp.now());
        if (!filters.categories().isEmpty()) {
            query = eventsRef.whereIn("category", new ArrayList<>(filters.categories()));
        }
        if (filters.startTime().isPresent()) {
            final var startTime = filters.startTime().get();
            query = eventsRef.whereGreaterThanOrEqualTo("eventTime", startTime);
        }
        if (filters.endTime().isPresent()) {
            final var endTime = filters.endTime().get();
            query = eventsRef.whereLessThan("eventTime", endTime);
        }
        return parseQuerySnapShots(query.get());
    }

    /**
     * Fetch events from database in a date range.
     * @param startTime Date range start
     * @param endTime Date range end
     */
    public Promise<List<Event>> fetchEventsByDate(Timestamp startTime, Timestamp endTime) {
        return parseQuerySnapShots(eventsRef
                .whereGreaterThan("eventTime", startTime)
                .whereLessThan("eventTime", endTime)
                .get());
    }

    /**
     * Fetch events with an account enrolled.
     * @param enrollee email of enrolled account
     */
    public Promise<List<Event>> fetchEventsByEnrolled(String enrollee) {
        return promise(eventEntrantsRef
                        .whereArrayContains("enrolledEntrants", enrollee)
                        .get())
                .then(x -> {
                    final var eventIds = x.getDocuments().stream().map(DocumentSnapshot::getId);
                    // TODO: Maybe use ::whereIn (but it can only take 30 elements as match input).
                    final var eventGetTasks = eventIds.map(
                            entrantId -> promise(eventsRef.document(entrantId).get()));
                    return Promise.all(eventGetTasks);
                })
                .map(x -> x.stream()
                        .map(EventsDB::getEventFromSnapshot)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }

    public PromiseOpt<EventEntrants> fetchEventEntrants(UUID eventId) {
        return promiseOpt(promise(eventEntrantsRef.document(eventId.toString()).get())
                .map(EventsDB::getEventEntrantsFromSnapshot));
    }

    public Promise<List<EventEntrants>> fetchEventsEntrants(List<UUID> eventIds) {
        final var eventEntrantGetTasks = eventIds.stream()
                .map(eventId ->
                        promise(eventEntrantsRef.document(eventId.toString()).get()));
        return Promise.all(eventEntrantGetTasks).map(x -> x.stream()
                .map(EventsDB::getEventEntrantsFromSnapshot)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()));
    }

    /**
     * Remove given event alongside its relevant information.
     * @param eventID UUID of event
     */
    public Promise<Void> deleteEvent(UUID eventID) {
        final var eventIDStr = eventID.toString();
        return promise(eventsRef.document(eventIDStr).delete())
                .alongside(promise(eventEntrantsRef.document(eventIDStr).delete()))
                .alongside(deletePoster(eventID));
    }

    /**
     * Removes user with email from all lists of all events
     * @param email email of user
     */
    public Promise<Void> removeUserFromEvents(String email) {
        final var updateMap = new HashMap<FieldPath, Object>();
        // Remove the user from the entrant arrays.
        var arrayFields = new String[] {
            "enrolledEntrants", "selectedEntrants", "acceptedEntrants", "cancelledEntrants"
        };
        for (String field : arrayFields) {
            updateMap.put(FieldPath.of(field), FieldValue.arrayRemove(email));
        }
        // Remove their location as well.
        updateMap.put(FieldPath.of("entrantLocations", email), FieldValue.delete());

        WriteBatch batch = db.batch();
        // Why the hell does the Java firestore SDK not have `listDocuments`???
        // There's no need to get the whole document for all of them...
        return promise(eventEntrantsRef.get()).then(qs -> {
            for (var doc : qs) {
                fieldPathUpdate(batch, doc.getReference(), updateMap);
            }
            return promise(batch.commit());
        });
    }

    /**
     * Clear the whole firestore collection, and the firebase storage for images.
     * @return Promise.
     */
    @TestOnly
    public Promise<Void> nuke() {
        return promise(eventsRef.get())
                .with(promise(eventEntrantsRef.get()))
                .map(pair -> {
                    final var eventDocs = pair.first;
                    final var eventEntrantDocs = pair.second;
                    return Stream.concat(
                            eventDocs.getDocuments().stream(),
                            eventEntrantDocs.getDocuments().stream());
                })
                .then(docs -> {
                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
                    docs.forEach(doc -> batch.delete(doc.getReference()));
                    return promise(batch.commit());
                });
    }

    // Helper for parsing a QuerySnapshot yielding task.
    private Promise<List<Event>> parseQuerySnapShots(Task<QuerySnapshot> task) {
        return promise(task).map(querySnapshot -> querySnapshot.getDocuments().stream()
                .map(EventsDB::getEventFromSnapshot)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()));
    }

    /**
     * Uploads a selected poster to firebase
     * @param eventID the eventID of the poster.
     * @param uri the uri of the image
     * @return a promise of the upload task
     */
    public Promise<Void> storePoster(UUID eventID, Uri uri) {
        StorageReference imageRef = storageRef.child("posters/" + eventID.toString());

        // Stores the file in the database. Since the TaskSnapshot is not used, it is mapped to null
        // to return a Promise<Void>
        var posterStorageTask = imageRef.putFile(uri);
        return promise(posterStorageTask).map(taskSnapshot -> null);
    }

    // Delete the event associated poster if it exists. Ignore otherwise.
    private Promise<Void> deletePoster(UUID eventID) {
        return promise(getPosterStorageRef(eventID).delete().continueWith(res -> {
            final var exc = res.getException();
            if (exc == null) {
                return null;
            }
            if (exc instanceof StorageException storageExc
                    && storageExc.getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                return null;
            }
            throw exc;
        }));
    }

    /**
     * The function below fetches the storage references for all events with posters.
     * @return A promise of a map of eventID's to posters.
     */
    public Promise<Map<UUID, StorageReference>> fetchAllPosters() {
        StorageReference postersRef = storageRef.child("posters/");

        return promise(postersRef.listAll()).map(listResult -> {
            // Creates map, and gets items from the list.
            Map<UUID, StorageReference> dict = new HashMap<>();
            List<StorageReference> imageRefs = listResult.getItems();

            // Adds the eventID, and storageRef to the dictionary
            for (StorageReference imageRef : imageRefs) {
                UUID eventID = UUID.fromString(imageRef.getName());
                dict.put(eventID, imageRef);
            }

            return dict;
        });
    }

    /**
     * The code below returns the storage reference to the selected poster.
     * @param eventID the eventID of the poster
     * @return The storage reference to the poster
     */
    public StorageReference getPosterStorageRef(UUID eventID) {
        // The following code is based on the downloading files section from the firebase docs:
        // https://firebase.google.com/docs/storage/android/download-files?_gl=1
        return storageRef.child("posters/" + eventID.toString());
    }

    // Update using map of FieldPath to object.
    // For some god forsaken reason, this doesn't already exist in firestore's Java SDK.
    private static Promise<Void> fieldPathUpdate(
            DocumentReference docRef, Map<FieldPath, Object> updater) {
        if (updater.isEmpty()) {
            return Promise.of(null);
        }
        final var arr = flattenMapEntries(updater);
        // If there is at least one element in the map, the array length must be 2.
        final var firstFieldPath = (FieldPath) arr[0];
        final var firstFieldVal = arr[1];
        return promise(docRef.update(
                firstFieldPath, firstFieldVal, Arrays.copyOfRange(arr, 2, arr.length)));
    }

    // Like above, but for transactions.
    // Yes this could easily be CSEd with lambdas but java lambdas suck.
    private static Transaction fieldPathUpdate(
            Transaction tx, DocumentReference doc, Map<FieldPath, Object> updater) {
        if (updater.isEmpty()) {
            return tx;
        }
        final var arr = flattenMapEntries(updater);
        // If there is at least one element in the map, the array length must be 2.
        final var firstFieldPath = (FieldPath) arr[0];
        final var firstFieldVal = arr[1];
        return tx.update(
                doc, firstFieldPath, firstFieldVal, Arrays.copyOfRange(arr, 2, arr.length));
    }

    // Like above, but for batches.
    // Yes this could easily be CSEd with lambdas but java lambdas suck.
    private static WriteBatch fieldPathUpdate(
            WriteBatch batch, DocumentReference doc, Map<FieldPath, Object> updater) {
        if (updater.isEmpty()) {
            return batch;
        }
        final var arr = flattenMapEntries(updater);
        // If there is at least one element in the map, the array length must be 2.
        final var firstFieldPath = (FieldPath) arr[0];
        final var firstFieldVal = arr[1];
        return batch.update(
                doc, firstFieldPath, firstFieldVal, Arrays.copyOfRange(arr, 2, arr.length));
    }

    private static <T, V> Object[] flattenMapEntries(Map<T, V> m) {
        final var entries = new ArrayList<>(m.entrySet());
        // Two elements per entry (key and value).
        final var res = new Object[entries.size() * 2];
        for (int i = 0; i < entries.size(); i++) {
            final var entry = entries.get(i);
            final var arrayIx = i * 2;
            res[arrayIx] = entry.getKey();
            res[arrayIx + 1] = entry.getValue();
        }
        return res;
    }
}
