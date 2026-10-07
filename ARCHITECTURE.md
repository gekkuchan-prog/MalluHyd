# Mallu Gang — Cross-Platform System Architecture & Technical Specifications

## 1. System Overview & Platform Strategy

**Mallu Gang** is a private social and gaming clubhouse application built for intimate friend groups and close-knit communities. The system combines:
1. Instant Messaging (WhatsApp-style 1-on-1 and Group chats with emoji reactions, voice notes, rich media, search, pin, replies)
2. Interactive Real-Time Multiplayer Games (Ludo, Snake & Ladders, Carrom, Mini-games) with in-game live communications
3. Community & Social Hub (Meetup Events with RSVP, Polls with multiple choices, Group Expenses / Bill Splitting, Memories & Photo Albums)
4. Private Governance & Moderation (Formal Complaint Box with review lifecycles, 3-tier Role-Based Access Control: Super Admin, Admin 1/2, Member; immutable Admin Audit Logs, Gang Constitution / Rules)
5. Responsive & Adaptive UI: Mobile-first ergonomic touch UI with bottom bars on Android, expanding seamlessly into multi-column split views and navigation rails on tablets and desktop-class screens (Windows).

---

## 2. Shared Multi-Platform Data Schema (Supabase/PostgreSQL & Android/Room)

The data model is normalized and platform-agnostic. Both the Android native client and future Windows/Flutter desktop builds bind to the same core entities:

```sql
-- Core Accounts & Profiles
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER', -- 'SUPER_ADMIN', 'ADMIN_1', 'ADMIN_2', 'MEMBER'
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'SUSPENDED', 'INVITED'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    gender VARCHAR(20),
    date_of_birth DATE NOT NULL,
    about TEXT,
    avatar_url TEXT,
    privacy_last_seen VARCHAR(20) DEFAULT 'EVERYONE',
    privacy_profile VARCHAR(20) DEFAULT 'EVERYONE',
    privacy_phone VARCHAR(20) DEFAULT 'CONTACTS',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Groups & Memberships
CREATE TABLE groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    avatar_url TEXT,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE group_members (
    group_id UUID REFERENCES groups(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    is_admin BOOLEAN DEFAULT FALSE,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (group_id, user_id)
);

-- Real-time Messages & Reactions
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id VARCHAR(100) NOT NULL, -- group_id or sorted (user1_user2)
    sender_id UUID REFERENCES users(id),
    content TEXT NOT NULL,
    message_type VARCHAR(20) DEFAULT 'TEXT', -- 'TEXT', 'IMAGE', 'VOICE', 'GAME_INVITE', 'EVENT', 'EXPENSE'
    attachment_url TEXT,
    reply_to_id UUID REFERENCES messages(id),
    status VARCHAR(20) DEFAULT 'SENT', -- 'SENDING', 'SENT', 'DELIVERED', 'READ'
    is_pinned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE message_reactions (
    message_id UUID REFERENCES messages(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    reaction VARCHAR(10) NOT NULL,
    PRIMARY KEY (message_id, user_id)
);

-- Community Events & RSVP
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    location VARCHAR(200),
    event_date TIMESTAMP WITH TIME ZONE NOT NULL,
    organizer_id UUID REFERENCES users(id),
    category VARCHAR(50) DEFAULT 'MEETUP',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE event_rsvps (
    event_id UUID REFERENCES events(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL, -- 'GOING', 'MAYBE', 'NOT_GOING'
    PRIMARY KEY (event_id, user_id)
);

-- Polls & Voting
CREATE TABLE polls (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question TEXT NOT NULL,
    creator_id UUID REFERENCES users(id),
    is_multiple_choice BOOLEAN DEFAULT FALSE,
    is_anonymous BOOLEAN DEFAULT FALSE,
    closes_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE poll_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    poll_id UUID REFERENCES polls(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    votes_count INT DEFAULT 0
);

-- Expense Sharing (Bill Splitter)
CREATE TABLE expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,
    payer_id UUID REFERENCES users(id),
    split_type VARCHAR(20) DEFAULT 'EQUAL',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE expense_participants (
    expense_id UUID REFERENCES expenses(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    share_amount NUMERIC(10, 2) NOT NULL,
    is_settled BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (expense_id, user_id)
);

-- Multiplayer Game Rooms & Authoritative State
CREATE TABLE game_rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_code VARCHAR(10) UNIQUE NOT NULL,
    game_type VARCHAR(50) NOT NULL, -- 'LUDO', 'SNAKE_AND_LADDERS', 'CARROM', 'RACE'
    host_id UUID REFERENCES users(id),
    max_players INT DEFAULT 4,
    status VARCHAR(20) DEFAULT 'WAITING', -- 'WAITING', 'IN_PROGRESS', 'COMPLETED'
    game_state JSONB NOT NULL,
    voice_room_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Private Complaint Box & Moderation
CREATE TABLE complaints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tracking_code VARCHAR(20) UNIQUE NOT NULL, -- e.g. MG-024
    complainant_id UUID REFERENCES users(id),
    accused_id UUID REFERENCES users(id),
    category VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    evidence_urls JSONB,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(30) DEFAULT 'SUBMITTED', -- 'SUBMITTED', 'UNDER_REVIEW', 'RESPONSE_REQUESTED', 'RESOLVED', 'DISMISSED'
    admin_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Immutable Admin Audit Logs
CREATE TABLE admin_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    target_entity VARCHAR(50) NOT NULL,
    target_id VARCHAR(100),
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

---

## 3. Role & Permission Matrix

| Capability | Super Admin | Admin 1 / Admin 2 | Member |
|---|:---:|:---:|:---:|
| Full messaging & chat | Yes | Yes | Yes |
| Create game rooms & play | Yes | Yes | Yes |
| Voice room join / mute / speak | Yes | Yes | Yes |
| Create Events, Polls, Expenses | Yes | Yes | Yes |
| Submit private complaints | Yes | Yes | Yes |
| View own submitted complaints | Yes | Yes | Yes |
| View all Gang complaints | Yes | Yes | No |
| Change complaint status & add admin notes | Yes | Yes | No |
| Appoint / Remove Admin 1 & 2 | Yes | No | No |
| Suspend / Restore Members | Yes | Yes | No |
| Demote or Remove Super Admin | **STRICTLY FORBIDDEN** | **STRICTLY FORBIDDEN** | **STRICTLY FORBIDDEN** |
| Edit Gang Constitution / Rules | Yes | No | No |
| View Admin Audit Trail | Yes | Yes | No |

---

## 4. Cross-Platform Responsive Design Strategy

- **Compact (<600dp, Android Mobile Phones):**
  - Navigation: Material 3 Bottom Navigation bar with active badges.
  - Chat: Full-screen edge-to-edge chat view with attachment picker bottom-sheet.
  - Games: Game canvas at top/center with collapsible in-game chat drawer and voice bar at bottom.
- **Medium & Expanded (600dp - 1200dp+, Tablets & Windows Desktop):**
  - Navigation: Vertical Navigation Rail or Permanent Sidebar.
  - Chat: Dual-pane master-detail (Chat conversations list on left, active thread with message details on right).
  - Games: Side-by-side split layout (Game board on left 65%, interactive chat + live player voice controls on right 35%).
