package com.bestmoney.cms.authentication;

/**
 * Representation of the Samba SAM Account.
 */
public class SambaSamAccount {
    
    /**
     * The LDAP schema object class.
     */
    public static final String OBJECT_CLASS = "sambaSamAccount";

    /**
     * objectClass attribute constant.
     */
    public static final String ATTR_OBJECT_CLASS = "objectClass";

    /**
     * The LDAP Password attribute name.
     * attributetype ( 1.3.6.1.4.1.7165.2.1.25 NAME 'sambaNTPassword'
	 * DESC 'MD4 hash of the unicode password'
     */
    public static final String ATTR_NT_PASS = "sambaNTPassword";

    /**
     * The LDAP username attribute name.
     */
    public static final String ATTR_UID = "uid";

    /**
     * The NT password hash (14 bytes; hi-lo unicode; MD4)
     */
    private final String m_sambaNtPassword;

    /**
     * The m_uid value.
     */
    private final String m_uid;

    public SambaSamAccount(final String uid, final String hash) {
        m_uid = uid;
        m_sambaNtPassword = hash;
    }

    public String getUid() {
        return m_uid;
    }
    
    public String getSambaNtPassword() {
        return m_sambaNtPassword;
    }
}
