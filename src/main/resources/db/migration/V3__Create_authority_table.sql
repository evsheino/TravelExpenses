CREATE TABLE authority (
id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
user_id BIGINT not null,
authority varchar(50) not null,
constraint fk_authority_user foreign key(user_id) references user(id));
create unique index ix_authority_user_id on authority (user_id, authority);