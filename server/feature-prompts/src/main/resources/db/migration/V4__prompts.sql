create table prompt_versions (
    prompt_key text not null,
    number integer not null,
    text text not null,
    saved_at timestamp with time zone not null,
    primary key (prompt_key, number)
);

create table current_prompts (
    prompt_key text primary key,
    number integer not null,
    foreign key (prompt_key, number) references prompt_versions (prompt_key, number)
);
