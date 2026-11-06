create table characters (
    character_id uuid primary key,
    name text not null,
    history text not null,
    personality text not null,
    manner text not null
);
