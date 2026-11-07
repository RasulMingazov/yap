create table levels (
    code text primary key,
    rating_from integer,
    vocabulary text not null,
    max_words integer not null,
    idioms text not null,
    block text not null
);
