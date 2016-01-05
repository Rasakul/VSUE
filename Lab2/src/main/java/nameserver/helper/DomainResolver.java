package nameserver.helper;

/**
 * This class encapsulates the methods can be done on a domain address.
 *
 * @author David Molnar
 * @since 04.01.2016
 */
public class DomainResolver {
    private String domainName;

    public DomainResolver(String domain) {
        domainName = domain;
    }

    /**
     * Checks if the domainName contains only alphanumeric characters and dots (two dots to each other is not allowed)
     *
     * @return whether the domainName is valid or not
     */
    public boolean isValidDomain(){
        if (domainName.contains("..")) return false;

        return domainName.matches("^[a-zA-Z0-9.]+$");
    }

    /**
     * Checks if the contained domainname has a subdomain
     *
     * @return whether the domain has a subdomain
     */
    public boolean hasSubdomain(){
        return domainName.indexOf('.') >= 0;
    }

    /**
     * Slits the subdomain from the namespace zone.
     * Before using this method, ensure that domain has a subdomain!
     *
     * @return subdomain
     */
    public String getSubdomain(){
        return domainName.substring(0, domainName.lastIndexOf('.'));
    }

    /**
     * Returns the zone of the domain address.
     *
     * @return zone
     */
    public String getZone(){
        String[] split = domainName.split("\\.");

        String zone = split[split.length-1];

        return zone;
    }

    /**
     * Getter method for obtaining the contained domain address.
     *
     * @return this.domainName
     */
    public String getDomainName(){
        return domainName;
    }
}
