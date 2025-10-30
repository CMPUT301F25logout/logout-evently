### Class: **User**
### Responsibilities:
- Represents app user
- Join events
- Leave events
- View and filter events
- View event details
- View and track event history
- Respond to invitations
- Manage profile (create, update, delete)
- Recieve and manage notifications
- Store location
- Authenticate via device
### Collaborators:
- Organizer
- Admin
- Event
- Notification

---

### Class: **Organizer**
### Responsibilities:
- Create events
- Manage event details (title, description, location, time)
- Manage event posters (upload, update)
- Set event requirements (registration period, geolocation, entrant limit, selection amount)
- Delete owned events
- View and manage entrants (cancel entrants, replace cancelled entrants)
- Export enrolled entrant list in CSV
- Send notifications
### Collaborators:
- User
- Event
- Notification

---

### Class: **Admin**
### Responsibilities:
- Browse events, images, and users
- Remove events, images, and users
- View notification logs
### Collaborators:
- User
- Event
- Notification

---

### Class: **Event**
### Responsibilities:
- Store event details (name, description, time, location, poster, organizer, requirements, entrant limit, selection limit)
- Store lists of entrants (all, chosen, cancelled, enrolled)
- Create QR code
### Collaborators:
- User
- Organizer
- Event
- Notification

---

### Class: **Notification**
### Responsibilites:
- Represents self
- Appears
- Manages opt in/out setting behaviour
- Log messages
### Collaborators:
- Organizer
- Event
- Admin
- User

#### Properties (model)
- Associated event ID (String)
- Channel (ALL, WINNERS, LOSERS, CANCELLED)
- Message (String)
- SeenBy (List<String> of account emails, lowercased)