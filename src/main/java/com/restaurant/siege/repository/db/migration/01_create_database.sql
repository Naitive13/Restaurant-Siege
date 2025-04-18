create DATABASE siege;
\c siege
create user siege_user with PASSWORD '12345678';
grant all privileges on database siege to siege_user;
grant all privileges on all tables in SCHEMA public to siege_user;
grant all on SCHEMA public to siege_user;

\c siege siege_user
