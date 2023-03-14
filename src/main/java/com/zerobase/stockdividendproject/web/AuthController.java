package com.zerobase.stockdividendproject.web;

import com.zerobase.stockdividendproject.model.Auth;
import com.zerobase.stockdividendproject.security.TokenProvider;
import com.zerobase.stockdividendproject.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final MemberService memberService;
	private final TokenProvider tokenProvider;

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
		// 회원가입 api
		var result = memberService.register(request);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/signin")
	public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
		// 로그인 api
		var member = this.memberService.authenticate(request);
		var token = this.tokenProvider.generateToken(member.getUsername(), member.getRoles());
		log.info("user login -> " + request.getUsername());
		return ResponseEntity.ok(token);
	}

}
