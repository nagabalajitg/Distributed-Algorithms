package com.balaji.naga.communication;

import com.balaji.naga.resource.Group;
import com.balaji.naga.resource.Process;
import com.balaji.naga.resource.Site;

public interface Channel {
    public void addMessage(Group group, Site site, Process process, String message);
}