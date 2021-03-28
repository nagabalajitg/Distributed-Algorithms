package com.balaji.naga.resource;

import java.util.HashSet;
import java.util.Set;

public class Group implements Resource {
    private long id;
    private Set<Site> sites;

    private void initProcesses(Set<Long> siteIDs) {
        sites = new HashSet<>();
        for (Long siteID : siteIDs) {
            sites.add(new Site(siteID, this));
        }
    }

    void addSiteByIDs(Set<Long> sites) {

    }
}