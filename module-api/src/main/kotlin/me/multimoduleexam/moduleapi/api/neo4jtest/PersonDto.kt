package me.multimoduleexam.moduleapi.api.neo4jtest

data class PersonDto(
    val id: String,
    val name: String,
    val age: Int
) {
    companion object {
        fun PersonDto.toEntity(): PersonNode {
            return PersonNode(
                id = this.id,
                name = this.name,
                age = this.age,
                likesList = null
            )
        }
    }
}
