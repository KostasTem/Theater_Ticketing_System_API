package com.unipi.mpsp.ticket_api.Controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.unipi.mpsp.ticket_api.DataClasses.AppUser;
import com.unipi.mpsp.ticket_api.Services.AppUserService;
import com.unipi.mpsp.ticket_api.Services.UserService;
import jakarta.xml.bind.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserService userService;
    private final AppUserService appUserService;
    private final GoogleIdTokenVerifier verifier;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder, UserService userService, AppUserService appUserService, @Value("${auth.client_id}") String clientID) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.userService = userService;
        this.appUserService = appUserService;
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();
        verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientID))
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        try {
            if(appUserService.getUser(username).getProvider().equals("GOOGLE")){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Error-Message","Invalid Login Method").body(null);
            }
            var authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(username, password));
            String token = generateToken(authentication);
            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .body(token);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String,String>> googleLogin(@RequestBody Map<String,String> req){
        String googleToken = req.get("googleToken");
        Map<String, String> res = new HashMap<>();
        try {
            GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), googleToken);
            boolean tokenIsValid = (idToken != null) && verifier.verify(idToken);
            if (tokenIsValid) {
                String email = idToken.getPayload().getEmail();

                if (appUserService.getUser(email) == null) {
                    String givenName = (String) idToken.getPayload().get("given_name");
                    String familyName = (String) idToken.getPayload().get("family_name");
                    String picture = (String) idToken.getPayload().get("picture");
                    userService.create(new AppUser(null, email, "", givenName, familyName, null, "GOOGLE", List.of("USER"), null));
                }
                AppUser appUser = appUserService.getUser(email);
                var authentication =
                        authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(appUser.getEmail(), ""));

                String token = generateToken(authentication);
                res.put("token", token);
                return ResponseEntity.ok().body(res);

            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Error-Message","Invalid Token").body(null);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("Error-Message",e.getMessage()).body(null);
        }
    }

    @PostMapping("/register")
    public AppUser register(@RequestBody AppUser appUser) throws ValidationException, IOException {
        appUser.setProvider("LOCAL");
        appUser.setReservations(new ArrayList<>());
        return userService.create(appUser);
    }

    private String generateToken(Authentication authentication){
        var user = (User) authentication.getPrincipal();

        var now = Instant.now();
        var expiry = 36000L;

        var scope =
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(joining(" "));

        var claims =
                JwtClaimsSet.builder()
                        .issuer("tickets")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(expiry))
                        .subject(format("%s", user.getUsername()))
                        .claim("roles", scope)
                        .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
