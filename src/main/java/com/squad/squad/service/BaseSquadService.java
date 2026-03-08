package com.squad.squad.service;

import com.squad.squad.context.GroupContext;

public abstract class BaseSquadService {
    protected Integer getSquadId() {
        return GroupContext.getCurrentGroupId();
    }
}
