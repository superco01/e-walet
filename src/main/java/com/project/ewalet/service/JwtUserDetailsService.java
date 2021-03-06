package com.project.ewalet.service;

import com.project.ewalet.mapper.UserMapper;
import com.project.ewalet.model.User;
import com.project.ewalet.model.payload.UserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class JwtUserDetailsService implements UserDetailsService {

	@Autowired
	UserMapper userMapper;

	@Autowired
	private PasswordEncoder bcryptEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userMapper.findByPhoneNumber(username);
		if (user == null) {
			throw new UsernameNotFoundException("User not found with email: " + username);
		}
		return new org.springframework.security.core.userdetails.User(user.getPhone_number(), user.getPassword(),
				new ArrayList<>());
	}

//	public void updateToken(String token, String email) {
//		User user = userMapper.findByEmail(email);
//		userMapper.updateToken(token, user.getId());
//	}
	
	public User save(UserPayload user) {
		User newUser = new User();
		newUser.setEmail(user.getEmail());
		newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
		newUser.setFirst_name(user.getFirst_name());
		newUser.setLast_name(user.getLast_name());
		newUser.setPhone_number(user.getPhone_number());
		userMapper.save(newUser);
		User returnCreatedUser = userMapper.findByPhoneNumber(newUser.getPhone_number());
		return returnCreatedUser;
	}
}