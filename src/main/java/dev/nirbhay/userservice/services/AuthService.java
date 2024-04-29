package dev.nirbhay.userservice.services;

import dev.nirbhay.userservice.dtos.UserDto;
import dev.nirbhay.userservice.models.SessionStatus;
import dev.nirbhay.userservice.models.User;
import dev.nirbhay.userservice.models.Session;
import dev.nirbhay.userservice.repositories.SessionRepository;
import dev.nirbhay.userservice.repositories.UserRepository;
import io.jsonwebtoken.security.MacAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import java.time.LocalDate;
import java.util.*;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public ResponseEntity<UserDto> login(String email, String password) {
        /*
        todo :
         1. add validation for max 3 sessions per users
         2. set token expiry while login and also validate while next login request
        */
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return null;// can throw exception if password dont matches
        }

        //String token = RandomStringUtils.randomAlphanumeric(30);
        // Create a test key suitable for the desired HMAC-SHA algorithm:
        MacAlgorithm alg = Jwts.SIG.HS256; //or HS384 or HS256
        SecretKey key = alg.key().build();

        //String message = "Hello World!";
        //json :: Key:value
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("email", user.getEmail());
        jsonMap.put("roles", List.of(user.getRoles()) );
        jsonMap.put("created_at", new Date());
        jsonMap.put("expiry_at", DateUtils.addDays(new Date(), 10));

        //byte[] content = message.getBytes(StandardCharsets.UTF_8);
        // Create the compact JWS:
  //      String jws = Jwts.builder().content(content, "text/plain").signWith(key, alg).compact();
        // Parse the compact JWS:
  //      content = Jwts.parser().verifyWith(key).build().parseSignedContent(jws).getPayload();
        String jws = Jwts.builder()
                .claims(jsonMap)
                .signWith(key,alg)
                .compact();

        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setToken(jws);
        session.setUser(user);
        sessionRepository.save(session);

        UserDto userDto = new UserDto();
        userDto.setEmail(email);

//        Map<String, String> headers = new HashMap<>();
//        headers.put(HttpHeaders.SET_COOKIE, token);

        MultiValueMapAdapter<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, "auth-token:" + jws);



        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, headers, HttpStatus.OK);
//        response.getHeaders().add(HttpHeaders.SET_COOKIE, token);

        return response;
    }

    public ResponseEntity<Void> logout(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty()) {
            return null;
        }

        Session session = sessionOptional.get();

        session.setSessionStatus(SessionStatus.ENDED);

        sessionRepository.save(session);

        return ResponseEntity.ok().build();
    }

    public UserDto signUp(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password)); // We should store the encrypted password in the DB for a user.
        System.out.println("got user signup request with details user :"+ user.getEmail()+"  and encoded passsword :"+user.getPassword());
        User savedUser = userRepository.save(user);

        return UserDto.from(savedUser);
    }

    public SessionStatus validate(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty()) {
            return null;
        }

        return SessionStatus.ACTIVE;
    }

}
