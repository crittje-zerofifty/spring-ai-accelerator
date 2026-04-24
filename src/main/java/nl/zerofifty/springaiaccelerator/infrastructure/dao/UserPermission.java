package nl.zerofifty.springaiaccelerator.infrastructure.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_permission")
public class UserPermission {

    @Id
    private Long id;

    private String email;
    private String securityLevel;

    public UserPermission() {
    }

    public UserPermission(String email, String securityLevel) {
        this.email = email;
        this.securityLevel = securityLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }
}
