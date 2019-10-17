-- :name create-users-table
-- :command :execute
-- :result :raw
create table if not exists users (
  id            uuid primary key,
  email         varchar,
  email_lower   varchar as lower(email),
  passhash      varchar,
  name          varchar,
  surname       varchar
);
create unique index if not exists email_unique on users (email_lower);

-- :name insert-user :!
insert into users (id, email, passhash, name, surname)
values (:id, :email, :passhash, :name, :surname)

-- :name user-by-id :? :1
select * from users
where id = :id
