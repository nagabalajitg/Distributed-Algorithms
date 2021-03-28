package com.balaji.naga.resource;

import java.util.HashSet;
import java.util.Set;

public class ServerResourceManager {
    private Set<Resource> resources = null;
    private static final ServerResourceManager MANAGER =  new ServerResourceManager();
    private ServerResourceManager() {
        resources = new HashSet<>();
    }

    public static ServerResourceManager getServerResourceManager() {
        return MANAGER;
    }

    public void addProcessesBy() {

    }
}
