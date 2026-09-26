package ru.mtuci.rbpo.repository;

import org.springframework.stereotype.Repository;

import ru.mtuci.rbpo.model.User;

@Repository
public class UserRepository extends InMemoryRepository<User> {
}
