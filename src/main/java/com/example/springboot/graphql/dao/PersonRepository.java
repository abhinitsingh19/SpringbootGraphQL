package com.example.springboot.graphql.dao;

import org.springframework.data.repository.CrudRepository;

import com.example.springboot.graphql.entity.Person;

public interface PersonRepository extends CrudRepository<Person, Integer> {

	Person findByEmail(String email);

}
