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
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_username", columnList = "username"),
                @Index(name = "idx_user_status", columnList = "accountStatus"),
                @Index(name = "idx_user_active", columnList = "isActive")
        }
        )
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

    @Column(unique = true, nullable = false, updatable = false)
    private String email;

    private String password;

    @Column(length = 1000)
    private String bio;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    private String providerId;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Boolean isEmailVerified;

    private String emailVerificationToken;

    private LocalDateTime emailVerificationExpiry;

    private String resetToken;

    private LocalDateTime resetTokenExpiry;

    @Column(nullable = false)
    private Long followersCount = 0L;

    @Column(nullable = false)
    private Long followingCount = 0L;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(nullable = false)
    private Boolean restoreRequested = false;

    @Column(nullable = false)
    private Boolean unbanRequested = false;

    @Column(columnDefinition = "TEXT")
    private String unbanAppealMessage;

    private LocalDateTime deletedAt;

    private LocalDateTime restoredAt;

    @Column(nullable = false)
    private Boolean isPrivate = false;

    @Column(nullable = false)
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
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Follow> followers = new ArrayList<>();

    // USERS THIS USER FOLLOWS
    @JsonIgnore
    @OneToMany(
            mappedBy = "follower",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Follow> following = new ArrayList<>();

    private LocalDateTime lastLoginAt;

    private LocalDateTime lastSeenAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<SavedPost> savedPosts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(
            mappedBy = "reportedUser",
            fetch = FetchType.LAZY
    )
    private List<Report> reports = new ArrayList<>();

    @PrePersist
    public void prePersist() {

        if (this.isActive == null) {
            this.isActive = true;
        }

        if (this.isEmailVerified == null) {
            this.isEmailVerified = false;
        }

        if (this.deleted == null) {
            this.deleted = false;
        }

        if (this.restoreRequested == null) {
            this.restoreRequested = false;
        }

        if (this.unbanRequested == null) {
            this.unbanRequested = false;
        }

        if (this.isPrivate == null) {
            this.isPrivate = false;
        }

        if (this.isVerified == null) {
            this.isVerified = false;
        }

        if (this.followersCount == null) {
            this.followersCount = 0L;
        }

        if (this.followingCount == null) {
            this.followingCount = 0L;
        }

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

        return accountStatus != null
                &&
                accountStatus != AccountStatus.BANNED;
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

