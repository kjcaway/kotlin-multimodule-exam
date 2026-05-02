package me.courtboard.api.api.casbin

import jakarta.validation.Valid
import me.courtboard.api.aop.CheckPerm
import me.courtboard.api.api.casbin.dto.PolicyAddReqDto
import me.courtboard.api.api.casbin.dto.PolicyDeleteReqDto
import me.courtboard.api.api.casbin.service.CasbinService
import me.courtboard.api.global.dto.ApiResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CasbinAdminController(
    private val casbinService: CasbinService,
) {
    @CheckPerm
    @GetMapping("/api/admin/casbin/policies")
    fun getPolicies(): ApiResult<*> {
        return ApiResult.ok(casbinService.getPolicies())
    }

    @CheckPerm
    @PostMapping("/api/admin/casbin/policies")
    fun addPolicy(@Valid @RequestBody dto: PolicyAddReqDto): ApiResult<*> {
        casbinService.addPolicy(dto.sub, dto.obj, dto.act)
        return ApiResult.ok()
    }

    @CheckPerm
    @DeleteMapping("/api/admin/casbin/policies")
    fun deletePolicy(@Valid @RequestBody dto: PolicyDeleteReqDto): ApiResult<*> {
        casbinService.removePolicy(dto.sub, dto.obj, dto.act)
        return ApiResult.ok()
    }

    @CheckPerm
    @GetMapping("/api/admin/casbin/groupings")
    fun getGroupings(): ApiResult<*> {
        return ApiResult.ok(casbinService.getGroupings())
    }
}
