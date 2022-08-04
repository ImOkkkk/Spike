package com.liuwy.util;

import com.liuwy.context.WebContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ImOkkkk
 * @date 2022/8/4 9:53
 * @since 1.0
 */
public class JwtTokenUtil {

  private static final String USER_ID = "userId";
  private static final String USER_NAME = "userName";
  private static final String SECRET = "spkie";
  private static final String CREATE_DATETIME = "createDatetime";
  private static final String TOKEN_PREFIX = "Bearer";
  private static JwtParser jwtParser = Jwts.parser().setSigningKey(SECRET);

  private JwtTokenUtil() {
  }

  public static void parseTokenInfo(String token) {
    try {
      Claims claims = getClaims(token);
      WebContext.setCurrentUser(
          claims.get(USER_ID, String.class));
    } catch (Exception e) {
      if (e instanceof ExpiredJwtException) {
        WebContext.setIsExpired(Boolean.TRUE);
      } else {
        throw e;
      }
    }
  }

  private static Claims getClaims(String token) {
    return jwtParser.parseClaimsJws(token.replace(TOKEN_PREFIX, "").trim()).getBody();
  }

  public static String generateToken(String userId, String userName) {
    Map<String, Object> initParams = new HashMap<>(4);
    initParams.put(USER_ID, userId);
    initParams.put(USER_NAME, userName);
    initParams.put(CREATE_DATETIME, new Date());
    return generateToken(initParams);
  }

  private static String generateToken(Map<String, Object> claims) {
    return Jwts.builder()
        .setClaims(claims)
        .signWith(SignatureAlgorithm.HS512, SECRET)
        .compact();
  }

}
