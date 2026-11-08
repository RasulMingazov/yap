alter table topics add column status text not null default 'DRAFT'
    check (status in ('DRAFT', 'PUBLISHED'));

create unique index topics_published_release_date on topics (release_date)
    where status = 'PUBLISHED';
