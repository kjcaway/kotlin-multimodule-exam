package me.multimoduleexam.moduleapi.api.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import me.multimoduleexam.domain.Member
import me.multimoduleexam.validator.ReservationTimeChecker
import java.time.LocalDateTime

data class MemberRequestDto(
    val id: Long?,
    @field:NotBlank(message = "name cannot be blank")
    val name: String,
    @field:Email(message = "email is invalid")
    val email: String,
    @field:Digits(integer = 3, fraction = 0)
    val age: Int,

//    @JsonSerialize(using = LocalDateTimeSerializer::class)
//    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    val sendTime: LocalDateTime,

    @field:ReservationTimeChecker("reservation time cannot be past")
    val reservationStartTime: LocalDateTime
) {
    companion object {
        fun toEntity(dto: MemberRequestDto): Member {
            return Member(
                id = dto.id,
                name = dto.name,
                email = dto.email,
                age = dto.age
            )
        }
    }
}