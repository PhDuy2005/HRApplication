package com.se347.nhom4.HRApplication.config;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.domain.table.Permission;
import com.se347.nhom4.HRApplication.domain.table.Role;
import com.se347.nhom4.HRApplication.service.EmployeeService;
import com.se347.nhom4.HRApplication.util.SecurityUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Transactional
public class PermissionInterceptor implements HandlerInterceptor {
    @Autowired
    EmployeeService empService;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response, Object handler)
            throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);

        // check permission here
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        if (email != null && email.isEmpty() == false) {
            Employee user = this.empService.handleFindByUsername(email);
            if (user != null) {
                Role role = user.getRole();
                if (role != null) {
                    List<Permission> permissions = role.getPermissions();
                    boolean isAllowed = permissions.stream().anyMatch(item -> item.getApiPath().equals(path)
                            && item.getMethod().equals(httpMethod));
                    System.out.println(">>> isAllowed= " + isAllowed);
                    System.out.println(">>> requestURI= " + requestURI);

                    if (!isAllowed)
                        throw new AccessDeniedException("Interceptor: Bạn không có quyền truy cập vào endpoint này");
                } else
                    throw new AccessDeniedException("Interceptor: Bạn không có quyền truy cập vào endpoint này");
            }
            // } else
            // throw new InsufficientAuthenticationException("You must login first");
        }

        return true;
    }
}