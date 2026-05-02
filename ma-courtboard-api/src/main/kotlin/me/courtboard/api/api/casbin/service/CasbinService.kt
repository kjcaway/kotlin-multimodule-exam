package me.courtboard.api.api.casbin.service

import me.courtboard.api.api.casbin.dto.GroupingResDto
import me.courtboard.api.api.casbin.dto.PolicyResDto
import me.courtboard.api.global.Constants
import org.casbin.jcasbin.main.Enforcer
import org.springframework.stereotype.Service

@Service
class CasbinService(
    private val enforcer: Enforcer,
) {

    fun getPolicies(): List<PolicyResDto> =
        enforcer.policy.mapNotNull { rule ->
            if (rule.size >= 4) PolicyResDto(rule[0], rule[1], rule[2], rule[3]) else null
        }

    fun addPolicy(sub: String, obj: String, act: String): Boolean {
        val added = enforcer.addPolicy(sub, Constants.COURTBOARD, obj, act)
        if (added) enforcer.savePolicy()
        return added
    }

    fun removePolicy(sub: String, obj: String, act: String): Boolean {
        val removed = enforcer.removePolicy(sub, Constants.COURTBOARD, obj, act)
        if (removed) enforcer.savePolicy()
        return removed
    }

    fun getGroupings(): List<GroupingResDto> =
        enforcer.getNamedGroupingPolicy("g").mapNotNull { rule ->
            if (rule.size >= 3) GroupingResDto(rule[0], rule[1], rule[2]) else null
        }
}
