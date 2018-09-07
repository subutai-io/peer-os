package io.subutai.core.identity.impl.model;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.UserStatus;
import io.subutai.common.security.objects.UserType;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;


/**
 * Implementation of User interface. Used for storing user information.
 */
@Entity
@Table( name = "userl" )
@Access( AccessType.FIELD )
public class UserEntity implements User
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id" )
    private long id;

    @Column( name = "user_name", unique = true, nullable = false )
    private String userName;

    @Column( name = "full_name" )
    private String fullName;

    @Column( name = "password", nullable = false )
    private String password;

    @Column( name = "salt" )
    private String salt;

    @Column( name = "email", unique = true )
    private String email;

    @Column( name = "type", nullable = false )
    private int type = UserType.REGULAR.getId();

    @Column( name = "status" )
    private int status = UserStatus.ACTIVE.getId(); // Active

    @Column( name = "security_key_id" )
    private String securityKeyId = ""; // PGP KeyID


    @Column( name = "trust_level" )
    private int trustLevel = KeyTrustLevel.FULL.getId(); //Default Full Trust


    @Column( name = "fingerprint" )
    private String fingerprint = ""; // User key fingerprint

    @Column( name = "auth_id" )
    private String authId = ""; // Authorization ID


    @Column( name = "valid_date" )
    private Date validDate = null;


    //*********************************************
    @ManyToMany( targetEntity = RoleEntity.class, fetch = FetchType.EAGER )
    @JoinTable( name = "user_roles", joinColumns = {
            @JoinColumn( name = "user_id", referencedColumnName = "id" )
    }, inverseJoinColumns = { @JoinColumn( name = "role_id", referencedColumnName = "id" ) } )
    private List<Role> roles = new ArrayList<>();
    //*********************************************


    @Override
    public int getTrustLevel()
    {
        return trustLevel;
    }


    @Override
    public void setTrustLevel( final int trustLevel )
    {
        this.trustLevel = trustLevel;
    }


    @Override
    public Long getId()
    {
        return id;
    }


    @Override
    public void setId( final Long id )
    {
        this.id = id;
    }


    @Override
    public String getUserName()
    {
        return userName;
    }


    @Override
    public void setUserName( final String userName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( userName ) && !userName.trim().isEmpty() );

        this.userName = userName.trim().toLowerCase();
    }


    @Override
    public String getFullName()
    {
        return fullName;
    }


    @Override
    public void setFullName( final String fullName )
    {
        this.fullName = fullName;
    }


    @Override
    public String getPassword()
    {
        return password;
    }


    @Override
    public void setPassword( final String password )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( password ) && !password.trim().isEmpty() );

        this.password = password.trim();
    }


    @Override
    public String getSalt()
    {
        return salt;
    }


    @Override
    public void setSalt( final String salt )
    {
        this.salt = salt;
    }


    @Override
    public String getEmail()
    {
        return email;
    }


    @Override
    public void setEmail( final String email )
    {
        this.email = email;
    }


    @Override
    public List<Role> getRoles()
    {
        return roles;
    }


    @Override
    public void setRoles( final List<Role> roles )
    {
        this.roles = roles;
    }


    @Override
    public int getType()
    {
        return type;
    }


    @Override
    public void setType( final int type )
    {
        this.type = type;
    }


    @Override
    public int getStatus()
    {
        return status;
    }


    @Override
    public void setStatus( final int status )
    {
        this.status = status;
    }


    public void setId( final long id )
    {
        this.id = id;
    }


    @Override
    public String getSecurityKeyId()
    {
        return securityKeyId;
    }


    @Override
    public void setSecurityKeyId( final String securityKeyId )
    {
        this.securityKeyId = securityKeyId;
    }


    @Override
    public void setFingerprint( final String fingerprint )
    {
        this.fingerprint = fingerprint;
    }


    @Override
    public String getFingerprint()
    {
        return fingerprint;
    }


    @Override
    public String getStatusName()
    {
        return UserStatus.values()[status - 1].getName();
    }


    @Override
    public String getTypeName()
    {
        return UserType.values()[type - 1].getName();
    }


    @Override
    public String getLinkId()
    {
        return String.format( "%s|%s", getClassPath(), getUniqueIdentifier() );
    }


    @Override
    public String getUniqueIdentifier()
    {
        return String.valueOf( getId() );
    }


    @Override
    public String getClassPath()
    {
        return this.getClass().getSimpleName();
    }


    @Override
    public String getContext()
    {
        return PermissionObject.IDENTITY_MANAGEMENT.getName();
    }


    @Override
    public String getKeyId()
    {
        return getSecurityKeyId();
    }


    @Override
    public Date getValidDate()
    {
        return validDate;
    }


    @Override
    public void setValidDate( final Date validDate )
    {
        this.validDate = validDate;
    }


    @Override
    public String getAuthId()
    {
        return authId;
    }


    @Override
    public void setAuthId( final String authId )
    {
        this.authId = authId;
    }


    @Override
    public boolean isIdentityValid()
    {
        return validDate == null || System.currentTimeMillis() <= validDate.getTime();
    }


    @Override
    public boolean isBazaarUser()
    {
        return type == UserType.BAZAAR.getId();
    }
}
