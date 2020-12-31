package com.bestmoney.cms.authentication;

import org.alfresco.repo.security.authentication.AuthenticationComponentImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;

/**
 * NTML Authenticator that checks the MD4 password with an LDAP Samba SAM Account.
 */
public class LdapSambaAuthenticationComponentImpl extends AuthenticationComponentImpl {
    private static final Log logger = LogFactory.getLog(LdapSambaAuthenticationComponentImpl.class);

    /**
     * LDAP template instance.
     */
    private LdapTemplate m_ldapTemplate;

    /**
     * User base distinguished name - e.g. ou=Opsera,ou=People
     */
    private String m_userBase;


    public LdapSambaAuthenticationComponentImpl() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Spring DI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setLdapTemplate(final LdapTemplate ldapTemplateObj) {
        m_ldapTemplate = ldapTemplateObj;
    }

    public void setUserBase(String userBase) {
        m_userBase = userBase;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public Interface methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getMD4HashedPassword(String userName) {
        final SambaSamAccount sam = getSambaSamAccount(userName);
        if (sam == null) {
            logger.error("SambaSamAccount was null for user " + userName + ", please set it up in LDAP directory.");
            return null;
        } else {
            return sam.getSambaNtPassword();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private and Protected methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private SambaSamAccount getSambaSamAccount(String uid) {
        String andFilter = new AndFilter()
                .and(new EqualsFilter(SambaSamAccount.ATTR_OBJECT_CLASS, SambaSamAccount.OBJECT_CLASS))
                .and(new EqualsFilter(SambaSamAccount.ATTR_UID, uid)).encode();

        ContextMapper mapper = new ContextMapper() {
            public Object mapFromContext(final Object o) {
                final String dUid = ((DirContextOperations) o).getStringAttribute(SambaSamAccount.ATTR_UID);
                final String dHash = ((DirContextOperations) o).getStringAttribute(SambaSamAccount.ATTR_NT_PASS);
                return new SambaSamAccount(dUid, dHash);
            }
        };

        try {
            return (SambaSamAccount) m_ldapTemplate.searchForObject(m_userBase, andFilter, mapper);
        } catch (Exception e) {
            logger.error(e.getClass().getSimpleName() + " when searching for SambaSamAccount for user " + uid +
                    " [message=" + e.getMessage() + "]");
            return null;
        }
    }
}
