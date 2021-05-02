package com.example.springboot.graphql.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot.graphql.dao.PersonRepository;
import com.example.springboot.graphql.entity.Person;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;

@RestController
@RequestMapping("/api/v1")
public class PersonController {
	@Autowired
	private PersonRepository resposRepository;

	@Value("classpath:person.graphqls")
	private Resource schemaResource;

	private GraphQL graphQL;

	@PostConstruct
	public void loadSchema() throws IOException {
		File schemaFile = schemaResource.getFile();
		TypeDefinitionRegistry registry = new graphql.schema.idl.SchemaParser().parse(schemaFile);
		RuntimeWiring wiring = buildWiring();
		GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(registry, wiring);
		graphQL = GraphQL.newGraphQL(schema).build();
	}

	private RuntimeWiring buildWiring() {
		DataFetcher<List<Person>> fetcher1 = data -> {
			return (List<Person>) resposRepository.findAll();
		};
		DataFetcher<Person> fetcher2 = data -> {
			return (Person) resposRepository.findByEmail(data.getArgument("email"));
		};
		return RuntimeWiring.newRuntimeWiring().type("Query",
				typeWriting -> typeWriting.dataFetcher("getAllPersons", fetcher1).dataFetcher("findPerson", fetcher2))
				.build();

	}

	@PostMapping("/employees")
	private String addPerson(@RequestBody List<Person> persons) {
		resposRepository.saveAll(persons);
		return "saved all ";
	}

	@GetMapping("/employees")
	private List<Person> getPerson() {
		List<Person> result = (List<Person>) resposRepository.findAll();
		return result;
	}

	@PostMapping("/getAll")
	public ResponseEntity<Object> getAll(@RequestBody String query) {
		ExecutionResult result = graphQL.execute(query);
		return new ResponseEntity<Object>(result, HttpStatus.OK);

	}

	@PostMapping("/employeeByEmail")
	public ResponseEntity<Object> getPersonByEmail(@RequestBody String query) {
		ExecutionResult result = graphQL.execute(query);
		return new ResponseEntity<Object>(result, HttpStatus.OK);

	}

}
