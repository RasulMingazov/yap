create table topics (
    topic_id uuid primary key,
    title text not null,
    release_date date not null,
    duration_minutes integer check (duration_minutes >= 1),
    description text not null,
    setting text not null,
    character_id uuid references characters (character_id),
    guest text,
    role text not null,
    opening_english text not null,
    opening_translation text not null,
    check (character_id is null or guest is null)
);

create table topic_steps (
    topic_id uuid not null references topics (topic_id) on delete cascade,
    position integer not null,
    task text not null,
    pass_when text not null,
    outcome text not null,
    primary key (topic_id, position)
);
