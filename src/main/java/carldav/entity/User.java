package carldav.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name="users")
public class User {

    private Long id;
    private String password;
    private String email;
    private boolean locked;
    private Set<CollectionItem> collections;
    private String role;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    @NotNull
    @Column(name = "email", nullable=false)
    public String getEmail() {
        return email;
    }

    public boolean isLocked() {
        return locked;
    }

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    public Set<CollectionItem> getCollections() {
        return collections;
    }

    public void setCollections(Set<CollectionItem> collections) {
        this.collections = collections;
    }

    public String getRole() {
        return role;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    public void setRole(final String role) {
        this.role = role;
    }
}
