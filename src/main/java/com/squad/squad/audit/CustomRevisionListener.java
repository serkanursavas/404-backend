package com.squad.squad.audit;

import com.squad.squad.context.GroupContext;
import com.squad.squad.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity rev = (CustomRevisionEntity) revisionEntity;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails ud) {
            rev.setUserId(ud.getId());
            rev.setUsername(ud.getUsername());
        }

        rev.setSquadId(GroupContext.getCurrentGroupId());

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            rev.setRequestUrl(request.getRequestURI());
            rev.setClientIp(request.getRemoteAddr());
        }
    }
}
