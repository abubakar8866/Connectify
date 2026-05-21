package com.abubakar.connectify.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.AuthProvider;
import com.abubakar.connectify.enums.Gender;
import com.abubakar.connectify.enums.Role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "username",unique = true, nullable = false)
    private String uname;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(length = 1000)
    private String bio;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    private Boolean isActive;

    private Boolean isEmailVerified;

    private String emailVerificationToken;

    private LocalDateTime emailVerificationExpiry;

    private String resetToken;

    private LocalDateTime resetTokenExpiry;

    private Long followersCount = 0L;

    private Long followingCount = 0L;

    private Boolean deleted = false;

    private Boolean restoreRequested = false;

    private Boolean unbanRequested = false;

    @Column(columnDefinition = "TEXT")
    private String unbanAppealMessage;

    private LocalDateTime deletedAt;

    private LocalDateTime restoredAt;

    private Boolean isPrivate = false;

    private Boolean isVerified = false;

    private LocalDateTime bannedUntil;

    @Column(columnDefinition = "TEXT")
    private String banReason;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_languages",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "language")
    private List<String> languages = new ArrayList<>();

    private LocalDate dateOfBirth;

    @Transient
    public Integer getAge() {

        if (dateOfBirth == null) {
            return null;
        }

        return Period
                .between(dateOfBirth, LocalDate.now())
                .getYears();
    }

    private String city;

    // USERS WHO FOLLOW THIS USER
    @JsonIgnore
    @OneToMany(
            mappedBy = "following",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Follow> followers = new ArrayList<>();

    // USERS THIS USER FOLLOWS
    @JsonIgnore
    @OneToMany(
            mappedBy = "follower",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Follow> following = new ArrayList<>();

    private LocalDateTime lastLoginAt;

    private LocalDateTime lastSeenAt;

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<SavedPost> savedPosts = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.isActive = true;
        this.isEmailVerified = false;

        if (this.role == null) {
            this.role = Role.USER;
        }

        if (this.accountStatus == null) {
            this.accountStatus = AccountStatus.ACTIVE;
        }

        if (this.provider == null) {
            this.provider = AuthProvider.LOCAL;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountStatus != AccountStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isActive);
    }

}

