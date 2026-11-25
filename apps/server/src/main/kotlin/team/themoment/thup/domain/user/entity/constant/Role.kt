package team.themoment.thup.domain.user.entity.constant

import org.springframework.security.core.GrantedAuthority

enum class Role : GrantedAuthority {
    STUDENT,
    ADMIN,
    ;

    override fun getAuthority(): String = "ROLE_$name"
}
