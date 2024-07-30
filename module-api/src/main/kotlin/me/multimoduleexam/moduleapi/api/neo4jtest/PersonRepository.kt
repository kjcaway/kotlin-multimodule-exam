package me.multimoduleexam.moduleapi.api.neo4jtest

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository

interface PersonRepository: ReactiveNeo4jRepository<PersonNode, String> {

}