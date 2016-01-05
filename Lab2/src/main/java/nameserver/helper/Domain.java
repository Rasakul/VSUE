package nameserver.helper;

/**
 * Created by David on 04.01.2016.
 */
public class Domain {
    private String domainName;

    public Domain(String domain) {
        domainName = domain;
    }

    public boolean hasSubdomain(){
        return domainName.indexOf('.') >= 0;
    }

    public String getSubdomain(){
        return domainName.substring(0, domainName.lastIndexOf('.'));
    }

    public String getZone(){
        String[] split = domainName.split("\\.");
        return split[split.length-1];
    }

    public String getDomainName(){
        return domainName;
    }
}
