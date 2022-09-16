package carldav.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table("USERS")
public class User {

  private Long id;
  private String password;
  private String email;
  private boolean locked;
  private String role;

  @Id
  public Long getId() {
    return id;
  }

  public String getPassword() {
    return password;
  }

  public String getEmail() {
    return email;
  }

  public boolean isLocked() {
    return locked;
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

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    User user = (User) other;
    return locked == user.locked && Objects.equals(id, user.id) && Objects.equals(password, user.password) && Objects.equals(email, user.email) && Objects.equals(role, user.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, password, email, locked, role);
  }
}
