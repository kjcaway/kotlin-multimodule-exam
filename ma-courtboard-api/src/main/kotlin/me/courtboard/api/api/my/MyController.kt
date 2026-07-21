package me.courtboard.api.api.my

import jakarta.validation.Valid
import me.courtboard.api.aop.CheckLogin
import me.courtboard.api.aop.CheckPerm
import me.courtboard.api.api.member.dto.ChangeNameReqDto
import me.courtboard.api.api.member.dto.ChangePasswordReqDto
import me.courtboard.api.api.member.service.MemberAvatarService
import me.courtboard.api.api.member.service.MemberService
import me.courtboard.api.api.tactics.service.TacticsService
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
class MyController(
    private val memberService: MemberService,
    private val tacticsService: TacticsService,
    private val memberAvatarService: MemberAvatarService,
) {
    @CheckPerm
    @GetMapping("/api/my/tactics")
    fun getTactics(): ApiResult<*> {
        val result = tacticsService.getMyTactics()
        return ApiResult.ok(result)
    }

    @CheckPerm
    @GetMapping("/api/my/info")
    fun getMyInfo(): ApiResult<*> {
        val result = memberService.getMyInfo()
        return ApiResult.ok(result)
    }

    @CheckLogin
    @CheckPerm
    @PutMapping("/api/my/info")
    fun changeMyInfo(@Valid @RequestBody dto: ChangeNameReqDto): ApiResult<*> {
        memberService.changeName(dto)
        return ApiResult.ok()
    }

    @CheckLogin
    @CheckPerm
    @PutMapping("/api/my/password")
    fun changePassword(@Valid @RequestBody dto: ChangePasswordReqDto): ApiResult<*> {
        memberService.changePassword(dto)
        return ApiResult.ok()
    }

    @CheckLogin
    @CheckPerm
    @DeleteMapping("/api/my/account")
    fun deleteMember(): ApiResult<*> {
        memberService.deleteMember()
        return ApiResult.ok()
    }

    @CheckLogin
    @CheckPerm
    @PostMapping("/api/my/avatar")
    fun uploadAvatar(@RequestPart("file") file: MultipartFile): ApiResult<*> {
        val memberId = CourtboardContext.getContext().memberId
        val result = memberAvatarService.uploadAvatar(memberId, file)
        return ApiResult.ok(result)
    }

    @CheckLogin
    @CheckPerm
    @DeleteMapping("/api/my/avatar")
    fun deleteAvatar(): ApiResult<*> {
        val memberId = CourtboardContext.getContext().memberId
        val result = memberAvatarService.deleteAvatar(memberId)
        return ApiResult.ok(result)
    }
}
