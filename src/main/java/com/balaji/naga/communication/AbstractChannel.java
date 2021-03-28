package com.balaji.naga.communication;

import com.balaji.naga.resource.Group;
import com.balaji.naga.resource.Process;
import com.balaji.naga.resource.ServerResourceManager;
import com.balaji.naga.resource.Site;

public abstract class AbstractChannel implements Channel{
    AbstractChannel (ServerResourceManager resource) {

    }
    public void addMessage(Group group, Site site, Process process, String message);
}
