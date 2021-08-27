package vcs.citydb.wfs.security;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.config.operation.WFSOperation;
import vcs.citydb.wfs.config.server.AbstractAccessRule;
import vcs.citydb.wfs.config.server.AccessControl;
import vcs.citydb.wfs.config.server.AccessScope;
import vcs.citydb.wfs.config.server.AllowAccessRule;
import vcs.citydb.wfs.exception.AccessControlException;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public class AccessController {
    private EnumMap<WFSOperation, AccessPermission> permissions;

    public static AccessController build(WFSConfig config) throws AccessControlException {
        AccessController controller = new AccessController();

        if (config.getServer().getSecurity().isEnabled()) {
            controller.permissions = new EnumMap<>(WFSOperation.class);

            for (AccessControl control : config.getServer().getSecurity().getAccessControls()) {
                AccessPermission permission = new AccessPermission();
                for (AbstractAccessRule accessRule : control.getRules()) {
                    boolean allow = accessRule instanceof AllowAccessRule;

                    for (String ipAddressString : accessRule.getIPAddresses()) {
                        try {
                            IPAddress ipAddress = new IPAddressString(ipAddressString).toAddress();
                            permission.addIPAddress(ipAddress, allow);
                        } catch (AddressStringException e) {
                            throw new AccessControlException("Failed to map the string '" + ipAddressString + "' to an IP address.");
                        }
                    }

                    if (allow) {
                        AllowAccessRule allowRule = (AllowAccessRule) accessRule;
                        allowRule.getTokens().forEach(permission::addToken);
                    }
                }

                Set<WFSOperation> operations = new HashSet<>();
                for (AccessScope scope : control.getScopes())
                    operations.addAll(scope.getOperations());

                if (operations.isEmpty())
                    operations.addAll(Arrays.asList(WFSOperation.values()));

                for (WFSOperation operation : operations) {
                    controller.permissions
                            .computeIfAbsent(operation, v -> new AccessPermission())
                            .addPermissions(permission);
                }
            }
        }

        return controller;
    }

    public void requireAccess(String operationName, HttpServletRequest request) throws AccessControlException {
        if (permissions != null) {
            WFSOperation operation = WFSOperation.fromValue(operationName);
            if (operation == null)
                throw new AccessControlException("Failed to map client request '" + operationName + "' to a valid WFS operation.");

            AccessPermission permission = permissions.get(operation);
            if (permission != null)
                permission.requireAccess(operationName, request);
        }
    }
}
