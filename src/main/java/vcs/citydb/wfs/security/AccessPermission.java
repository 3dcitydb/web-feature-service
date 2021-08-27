package vcs.citydb.wfs.security;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import vcs.citydb.wfs.exception.AccessControlException;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.util.ServerUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class AccessPermission {
    private Set<String> tokens;
    private List<IPAddress> allowedIPAddresses;
    private List<IPAddress> deniedIPAddresses;

    void requireAccess(String operationName, HttpServletRequest request) throws AccessControlException {
        String remoteHost = request.getRemoteHost();
        if (tokens == null
                && allowedIPAddresses == null
                && deniedIPAddresses == null)
            throw new AccessControlException("No valid access rules defined for operation '" + operationName + "'. Denying client '" + remoteHost + "'.");

        IPAddress ipAddress;
        try {
            ipAddress = new IPAddressString(remoteHost).toAddress();
        } catch (AddressStringException e) {
            throw new AccessControlException("Failed to map the client address '" + remoteHost + "' to an IP address or network.", e);
        }

        if (tokens != null) {
            String token = ServerUtil.getParameter(request, KVPConstants.ACCESS_CONTROL_TOKEN);
            if (token == null) {
                // try to get token from session object
                HttpSession session = request.getSession(false);
                if (session != null) {
                    Object object = session.getAttribute(KVPConstants.ACCESS_CONTROL_TOKEN);
                    if (object instanceof String)
                        token = (String) object;
                }
            }

            if (token == null)
                throw new AccessControlException("The client '" + remoteHost + "' did not send an access token.");

            if (!tokens.contains(token))
                throw new AccessControlException("Invalid token '" + token + "' sent by the client '" + remoteHost + "'.");

            // put token into session object
            request.getSession().setAttribute(KVPConstants.ACCESS_CONTROL_TOKEN, token);
        }

        boolean isAllowed = allowedIPAddresses == null;
        if (!isAllowed) {
            for (IPAddress allowed : allowedIPAddresses) {
                if (allowed.contains(ipAddress)) {
                    isAllowed = true;
                    break;
                }
            }
        }

        if (!isAllowed)
            throw new AccessControlException("The client address '" + remoteHost + "' does not match any allow rule.");

        if (deniedIPAddresses != null) {
            for (IPAddress denied : deniedIPAddresses) {
                if (denied.contains(ipAddress))
                    throw new AccessControlException("The client address '" + remoteHost + "' matches a deny rule.");
            }
        }
    }

    void addToken(String token) {
        if (tokens == null)
            tokens = new HashSet<>();

        tokens.add(token);
    }

    void addIPAddress(IPAddress ipAddress, boolean allow) {
        if (allow)
            addAllowedIPAddress(ipAddress);
        else
            addDeniedIPAddress(ipAddress);
    }

    void addPermissions(AccessPermission other) {
        if (other.tokens != null)
            other.tokens.forEach(this::addToken);

        if (other.allowedIPAddresses != null)
            other.allowedIPAddresses.forEach(this::addAllowedIPAddress);

        if (other.deniedIPAddresses != null)
            other.deniedIPAddresses.forEach(this::addDeniedIPAddress);
    }

    private void addAllowedIPAddress(IPAddress ipAddress) {
        if (allowedIPAddresses == null)
            allowedIPAddresses = new ArrayList<>();

        allowedIPAddresses.add(ipAddress);
    }

    private void addDeniedIPAddress(IPAddress ipAddress) {
        if (deniedIPAddresses == null)
            deniedIPAddresses = new ArrayList<>();

        deniedIPAddresses.add(ipAddress);
    }
}
