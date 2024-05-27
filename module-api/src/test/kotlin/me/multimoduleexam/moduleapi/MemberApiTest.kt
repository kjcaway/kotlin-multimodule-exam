package me.multimoduleexam.moduleapi

import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@Tag("api")
@ExtendWith(MockitoExtension::class)
@ExtendWith(SpringExtension::class)
@TestMethodOrder(MethodOrderer.MethodName::class)
@AutoConfigureMockMvc
class MemberApiTest @Autowired constructor(
    val mockMvc: MockMvc
) : IntegrationTest() {

    @Test
    fun `_01 test`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(
                MockMvcResultMatchers.content().contentType("application/json")
            )
    }

    @Test
    fun `_02 test batch sql`() {
        val data = """[
                {
                    "name": "abc", "email":"abc@gmail.com", "age": 11
                },
                {
                    "name": "abcdd2", "email":"abcdd2@gmail.com", "age": 13
                }
            ]
        """.trimIndent()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/jdbc-template-test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(data)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        mockMvc.perform(MockMvcRequestBuilders.get("/api/member"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[?(@.name == 'abc')]").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[?(@.name == 'abcdd2')]").exists())

    }
}