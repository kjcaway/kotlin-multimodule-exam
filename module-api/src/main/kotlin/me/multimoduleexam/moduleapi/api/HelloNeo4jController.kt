package me.multimoduleexam.moduleapi.api

import me.multimoduleexam.moduleapi.api.dto.ApiResult
import me.multimoduleexam.moduleapi.api.neo4jtest.LikesDto
import me.multimoduleexam.moduleapi.api.neo4jtest.LikesRelationship
import me.multimoduleexam.moduleapi.api.neo4jtest.PersonDto
import me.multimoduleexam.moduleapi.api.neo4jtest.PersonDto.Companion.toEntity
import me.multimoduleexam.moduleapi.api.neo4jtest.PersonRepository
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RequestMapping("/api/neo4j/")
@RestController
class HelloNeo4jController(
    val personRepository: PersonRepository
) {
    @GetMapping("/person/{id}")
    fun getPerson(@PathVariable id: String): Mono<ApiResult<*>> {
        return personRepository.findById(id)
            .map {
                ApiResult.ok(it)
            }
    }

    @GetMapping("/person")
    fun getPersons(): Mono<ApiResult<*>> {
        return personRepository.findAll()
            .collectList()
            .map {
                ApiResult.ok(it)
            }
    }

    @PostMapping("/person")
    fun postPerson(@RequestBody data: PersonDto): Mono<ApiResult<*>> {
        return personRepository.save(data.toEntity())
            .map { ApiResult.ok(it) }
    }

    @PostMapping("/person/likes")
    fun postPerson(@RequestBody data: LikesDto): Mono<ApiResult<*>> {

        return personRepository.findById(data.id)
            .flatMap { i ->
                personRepository.findById(data.likes)
                    .flatMap { target ->
                        if (i.likesList == null) {
                            i.likesList = mutableListOf()
                        }
                        i.likesList?.add(LikesRelationship(null, target))
                        personRepository.save(i)
                            .map {
                                ApiResult.ok()
                            }
                    }
            }
    }
}