# Task 5: Database Schema Design Document

## **Database Schema for CARMA Review Screen**

### **Database Overview:**
- **Database Name:** `carma_database`
- **Database Version:** 1
- **Database Technology:** Room (SQLite wrapper for Android)
- **Purpose:** Store auto-response messages and user feedback locally

---

## **Table 1: Messages Table**

### **Table Name:** `messages`

### **Description:**
Stores all auto-response messages that were sent or cancelled through the app.

### **Columns:**

| Column Name | Data Type | Constraints | Description |
|-------------|-----------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique message identifier |
| `request_id` | TEXT | NOT NULL | ID from backend API request |
| `recipient_name` | TEXT | NOT NULL | Name of message recipient |
| `recipient_number` | TEXT | | Phone number/contact ID |
| `message_text` | TEXT | NOT NULL | Full text of auto-response |
| `timestamp` | INTEGER | NOT NULL | Unix timestamp (milliseconds) |
| `tone` | TEXT | | Message tone: "Formal", "Casual", "Brief" |
| `status` | TEXT | NOT NULL, DEFAULT 'pending' | "sent", "cancelled", "pending" |
| `context_activity` | TEXT | | Activity context (e.g., "In a meeting") |
| `context_sender` | TEXT | | Sender relationship (e.g., "Manager") |
| `context_urgency` | TEXT | | Urgency level (e.g., "Normal", "High") |
| `context_tags` | TEXT | | JSON string of additional context tags |
| `has_feedback` | INTEGER | NOT NULL, DEFAULT 0 | 1 if feedback given, 0 if not (boolean) |
| `created_at` | INTEGER | NOT NULL | Creation timestamp |
| `updated_at` | INTEGER | NOT NULL | Last update timestamp |

### **Indexes:**
```sql
CREATE INDEX idx_messages_timestamp ON messages(timestamp);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_has_feedback ON messages(has_feedback);
CREATE INDEX idx_messages_recipient ON messages(recipient_name);
```

### **Sample Data:**
```sql
INSERT INTO messages (
    request_id, recipient_name, recipient_number, message_text, 
    timestamp, tone, status, context_activity, context_sender, 
    context_urgency, has_feedback
) VALUES (
    'req_12345',
    'Sarah Jenkins',
    '+1234567890',
    'Hi Sarah, I''m currently in a design sync until 3 PM and unable to review the deck right now. I''ll take a look as soon as I''m out.',
    1710864300000,
    'Casual',
    'sent',
    'In a meeting',
    'Manager',
    'Normal',
    1
);
```

---

## **Table 2: Feedback Table**

### **Table Name:** `feedback`

### **Description:**
Stores user feedback (thumbs up/down) for each message.

### **Columns:**

| Column Name | Data Type | Constraints | Description |
|-------------|-----------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique feedback identifier |
| `message_id` | INTEGER | FOREIGN KEY → messages(id), NOT NULL | Reference to message |
| `feedback_type` | TEXT | NOT NULL | "thumbs_up" or "thumbs_down" |
| `comment` | TEXT | | Optional user comment |
| `timestamp` | INTEGER | NOT NULL | When feedback was given |
| `created_at` | INTEGER | NOT NULL | Creation timestamp |

### **Foreign Key Constraint:**
```sql
FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
```

### **Indexes:**
```sql
CREATE INDEX idx_feedback_message_id ON feedback(message_id);
CREATE INDEX idx_feedback_type ON feedback(feedback_type);
CREATE INDEX idx_feedback_timestamp ON feedback(timestamp);
```

### **Sample Data:**
```sql
INSERT INTO feedback (
    message_id, feedback_type, comment, timestamp
) VALUES (
    1,
    'thumbs_up',
    'Response was perfect for the situation',
    1710864900000
);
```

---

## **Entity Relationship Diagram (ERD):**

```
┌─────────────────────────────────────┐
│         MESSAGES TABLE              │
├─────────────────────────────────────┤
│ • id (PK)                           │
│ • request_id                        │
│ • recipient_name                    │
│ • recipient_number                  │
│ • message_text                      │
│ • timestamp                         │
│ • tone                              │
│ • status                            │
│ • context_activity                  │
│ • context_sender                    │
│ • context_urgency                   │
│ • context_tags                      │
│ • has_feedback                      │
│ • created_at                        │
│ • updated_at                        │
└─────────────────────────────────────┘
              │
              │ 1:N (One message can have
              │      one feedback entry)
              ▼
┌─────────────────────────────────────┐
│         FEEDBACK TABLE              │
├─────────────────────────────────────┤
│ • id (PK)                           │
│ • message_id (FK)                   │
│ • feedback_type                     │
│ • comment                           │
│ • timestamp                         │
│ • created_at                        │
└─────────────────────────────────────┘
```

---

## **Database Statistics Queries:**

### **Query 1: Total Messages Count**
```sql
SELECT COUNT(*) as total_messages FROM messages;
```

### **Query 2: Messages with Positive Feedback**
```sql
SELECT COUNT(*) as positive_count 
FROM messages m
INNER JOIN feedback f ON m.id = f.message_id
WHERE f.feedback_type = 'thumbs_up';
```

### **Query 3: Messages with Negative Feedback**
```sql
SELECT COUNT(*) as negative_count 
FROM messages m
INNER JOIN feedback f ON m.id = f.message_id
WHERE f.feedback_type = 'thumbs_down';
```

### **Query 4: Messages Without Feedback**
```sql
SELECT COUNT(*) as no_feedback_count 
FROM messages 
WHERE has_feedback = 0;
```

### **Query 5: Messages by Date Range**
```sql
SELECT * FROM messages 
WHERE timestamp BETWEEN ? AND ?
ORDER BY timestamp DESC;
```

### **Query 6: Today's Messages**
```sql
SELECT * FROM messages 
WHERE timestamp >= ? 
  AND timestamp < ?
ORDER BY timestamp DESC;
```

### **Query 7: Feedback Rate**
```sql
SELECT 
    (SELECT COUNT(*) FROM messages WHERE has_feedback = 1) * 100.0 / 
    (SELECT COUNT(*) FROM messages) as feedback_percentage;
```

---

## **Data Flow:**

### **1. Saving a Message (From Preview Screen):**
```
PreviewActivity
    ↓
User clicks "Send Now"
    ↓
Save message to database
    ├─ request_id (from API)
    ├─ recipient_name
    ├─ message_text
    ├─ timestamp (current time)
    ├─ tone
    ├─ status = "sent"
    ├─ context_activity
    ├─ context_sender
    └─ context_urgency
    ↓
Message saved in messages table
```

### **2. Viewing Messages (Review Screen):**
```
ReviewActivity
    ↓
Query: SELECT * FROM messages WHERE timestamp >= ? AND timestamp <= ?
    ↓
Display in RecyclerView
```

### **3. Giving Feedback:**
```
User clicks message in Review Screen
    ↓
Dialog opens (FeedbackDialog)
    ↓
User clicks thumbs up/down
    ↓
Save to feedback table
    ├─ message_id (FK)
    ├─ feedback_type ("thumbs_up" or "thumbs_down")
    ├─ comment (optional)
    └─ timestamp
    ↓
Update message: has_feedback = 1
    ↓
Update statistics in Review Screen
```

---

## 🔧 **Room Database Classes Needed:**

### **1. Entity Classes:**
- `MessageEntity.java` - Maps to messages table
- `FeedbackEntity.java` - Maps to feedback table

### **2. DAO (Data Access Object) Interfaces:**
- `MessageDao.java` - CRUD operations for messages
- `FeedbackDao.java` - CRUD operations for feedback

### **3. Database Class:**
- `AppDatabase.java` - Room database instance

### **4. Repository Classes:**
- `MessageRepository.java` - Business logic for messages
- `FeedbackRepository.java` - Business logic for feedback

---

## **SQL CREATE TABLE Statements:**

### **Messages Table:**
```sql
CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    request_id TEXT NOT NULL,
    recipient_name TEXT NOT NULL,
    recipient_number TEXT,
    message_text TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    tone TEXT,
    status TEXT NOT NULL DEFAULT 'pending',
    context_activity TEXT,
    context_sender TEXT,
    context_urgency TEXT,
    context_tags TEXT,
    has_feedback INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE INDEX idx_messages_timestamp ON messages(timestamp);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_has_feedback ON messages(has_feedback);
CREATE INDEX idx_messages_recipient ON messages(recipient_name);
```

### **Feedback Table:**
```sql
CREATE TABLE feedback (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    message_id INTEGER NOT NULL,
    feedback_type TEXT NOT NULL,
    comment TEXT,
    timestamp INTEGER NOT NULL,
    created_at INTEGER NOT NULL,
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
);

CREATE INDEX idx_feedback_message_id ON feedback(message_id);
CREATE INDEX idx_feedback_type ON feedback(feedback_type);
CREATE INDEX idx_feedback_timestamp ON feedback(timestamp);
```

---

## **Data Types Mapping:**

| SQL Type | Java Type | Room Annotation | Description |
|----------|-----------|-----------------|-------------|
| INTEGER | long | @ColumnInfo | IDs, timestamps |
| INTEGER | int | @ColumnInfo | Flags, counts |
| INTEGER | boolean | @ColumnInfo | 0 = false, 1 = true |
| TEXT | String | @ColumnInfo | Text fields |

---

## **Design Decisions:**

### **1. Why Two Tables?**
- **Separation of Concerns:** Messages and feedback are different entities
- **One-to-One Relationship:** Each message can have at most one feedback
- **Data Integrity:** Foreign key ensures feedback always references valid message
- **Query Efficiency:** Can query messages without loading feedback

### **2. Why `has_feedback` Flag?**
- **Quick Filtering:** Can quickly find messages without feedback
- **Performance:** Avoids JOIN query when just checking feedback status
- **Statistics:** Easy to count messages with/without feedback

### **3. Why Store Context as Separate Columns?**
- **Querying:** Can filter by specific context (e.g., all "Manager" messages)
- **Indexing:** Can create indexes on individual context fields
- **Flexibility:** `context_tags` JSON field for additional tags

### **4. Why Unix Timestamp (Long)?**
- **Standardization:** Same format across Android and backend
- **Sorting:** Easy to sort chronologically
- **Date Math:** Easy to calculate date ranges
- **Timezone:** Milliseconds since epoch (UTC)

---

## **Database Size Estimation:**

### **Assumptions:**
- Average message: 150 characters
- Average context data: 100 characters
- Average feedback comment: 50 characters

### **Storage per Message:**
```
Message row: ~500 bytes
Feedback row: ~200 bytes (if given)
Total per message: ~700 bytes
```

### **100 messages:**
```
100 messages × 700 bytes = 70 KB
```

### **1000 messages:**
```
1000 messages × 700 bytes = 700 KB
```

**Conclusion:** Database will remain very small (<1 MB for typical usage)

---

## **Data Retention Policy:**

### **Recommendation:**
- **Keep messages:** 30 days
- **Delete old messages:** Automatically after 30 days
- **Export before delete:** Option to export to CSV

### **Implementation:**
```java
// Delete messages older than 30 days
public void deleteOldMessages() {
    long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
    messageDao.deleteMessagesBefore(thirtyDaysAgo);
}
```

---

## **Data Privacy & Security:**

### **Considerations:**
1. **Local Storage Only:** Data never leaves the device
2. **No Cloud Sync:** All data stored in local SQLite
3. **User Control:** User can delete all data anytime
4. **Encryption:** Consider encrypting sensitive fields (future enhancement)

### **GDPR Compliance:**
- User can view all stored data
- User can delete all data (clear database)
- User can export data (CSV export)

---

## **Schema Validation Checklist:**

- [x] All required fields identified
- [x] Primary keys defined
- [x] Foreign keys defined with cascading
- [x] Indexes on frequently queried columns
- [x] Default values set appropriately
- [x] Data types match Java types
- [x] Timestamps use consistent format
- [x] Boolean flags use INTEGER (0/1)
- [x] Relationships clearly defined
- [x] Sample data provided
- [x] Common queries documented

---

## **Schema Summary:**

| Aspect | Details |
|--------|---------|
| **Tables** | 2 (messages, feedback) |
| **Total Columns** | 19 (15 in messages, 4 in feedback) |
| **Indexes** | 7 total |
| **Foreign Keys** | 1 (feedback.message_id → messages.id) |
| **Primary Keys** | 2 (auto-increment) |
| **Required Fields** | 8 (NOT NULL constraints) |
| **Default Values** | 2 (status, has_feedback) |

---



