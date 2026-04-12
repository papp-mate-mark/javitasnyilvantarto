package otvosuzlet.javitasnyilntarto.model;

import jakarta.persistence.*;
import otvosuzlet.javitasnyilntarto.enums.UserAuthorities;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = true)
    private String name;

    @ElementCollection(targetClass = UserAuthorities.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "authority", columnDefinition = "user_authority") 
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Set<UserAuthorities> authorities = new HashSet<>();
    
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<JobGroup> jobGroups = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<RefreshToken> refreshTokens = new HashSet<>();
}
