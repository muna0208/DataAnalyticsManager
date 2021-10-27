package com.daumsoft.dataAnalyticsManager.common.service;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.daumsoft.dataAnalyticsManager.common.utils.EncryptionUtil;
import com.daumsoft.dataAnalyticsManager.common.utils.MakeUtil;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.User;
import com.daumsoft.dataAnalyticsManager.restFullApi.service.UserMngService;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider {

	private Logger logger = LoggerFactory.getLogger(UserAuthenticationProvider.class);

	@Autowired(required = false)
	private HttpServletRequest request;

	@Autowired
	UserMngService userService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String userId = authentication.getName();
		String password = (String) authentication.getCredentials();

		User user = new User();
		user.setUserId(userId);
		try {
			user.setUserPw(EncryptionUtil.encrypt(password));
		} catch (Exception e) {
			logger.error("FAILED TO SET PASSWORD ENCRYPTED!!!");
			e.printStackTrace();
		}

		// test
		try {
			user = userService.getUserInfo(user);
		} catch (Exception e) {
			logger.error("FAILED TO GET USERINFO!!!");
			e.printStackTrace();
		}

		if (user == null) {
			throw new BadCredentialsException("Login Error !!");
		}

		user.setUserPw(null);

		// 마지막 접속 업데이트
		try {
			userService.updateLastAccessDate(userId);
		} catch (Exception e) {
			logger.error("FAILED TO UPDATE LAST ACCESS DATE!!!");
			e.printStackTrace();
		}

		// 유저정보 세션에 저장
		userService.createSession(request, user);

		ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
		if ("admin".equals(user.getUserAuth())) {
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		} else {
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		}

		MakeUtil.log(user.getUserId() + ": Login Success!!");
		return new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
