package com.zerobase.stockdividendproject.security;

import com.zerobase.stockdividendproject.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {

	private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60;
	private static final String KEY_ROLES = "roles";

	private final MemberService memberService;

	@Value("${spring.jwt.secret}")
	private String secretKey;

	public String generateToken(String username, List<String> roles) {
		Claims claims = Jwts.claims().setSubject(username);
		claims.put(KEY_ROLES, roles);

		var now = new Date();
		var expireDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(expireDate)
				.signWith(SignatureAlgorithm.HS512, this.secretKey) // 사용할 암호화 알고리즘, 비밀키
				.compact();
	}

	public Authentication getAuthentication(String jwt) {
		log.debug("6=============== jwt -> "+jwt);
		UserDetails userDetails = this.memberService.loadUserByUsername(this.getUsername(jwt));
		log.debug("7===============" + this.getUsername(jwt));
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
	}

	public String getUsername(String token) {
		return this.parseClaims(token).getSubject();
	}

	public boolean validateToken(String token) {
		log.debug(StringUtils.hasText(token) ? token + "3==============" : "3==============");
		if (!StringUtils.hasText(token)) {
			return false;
		}
		var claims = this.parseClaims(token);
		log.debug("claims{}{}", claims.keySet(),claims.values() + "4==============");
		log.debug("now, expire{}{}", claims.getIssuedAt(),claims.getExpiration() + "5==============");
		return !claims.getExpiration().before(new Date());
	}

	private Claims parseClaims(String token) {
		try {
			return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}

}
