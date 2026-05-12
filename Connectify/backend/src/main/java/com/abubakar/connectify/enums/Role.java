package com.abubakar.connectify.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static com.abubakar.connectify.enums.Permission.*;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
public enum Role {

	USER(Set.of(USER_CREATE,USER_READ,USER_UPDATE,USER_DELETE)),
	ADMIN(Set.of(ADMIN_CREATE,ADMIN_READ,ADMIN_UPDATE,ADMIN_DELETE,
			USER_CREATE,USER_READ,USER_UPDATE,USER_DELETE));
	
	private final Set<Permission> permissions;
	
	public List<SimpleGrantedAuthority> getAuthorities(){
		var authorities = new ArrayList<>(getPermissions().stream().map(permission->new SimpleGrantedAuthority(permission.getPermission())).toList());
		authorities.add(new SimpleGrantedAuthority("ROLE_"+this.name()));
		return authorities;
	}
	
	private Role(Set<Permission> permissions) {
		this.permissions = permissions;
	}

}
