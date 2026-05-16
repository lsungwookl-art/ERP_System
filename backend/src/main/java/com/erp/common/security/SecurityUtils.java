package com.erp.common.security;

import com.erp.auth.security.ErpUserDetails;
import com.erp.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static ErpUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof ErpUserDetails)) {
            throw new BusinessException("인증 정보가 없습니다.", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        return (ErpUserDetails) auth.getPrincipal();
    }

    public static Long currentCompanyId() {
        return currentUser().getCompanyId();
    }

    public static Long currentUserId() {
        return currentUser().getUserId();
    }

    public static String currentEmail() {
        return currentUser().getEmail();
    }
}
