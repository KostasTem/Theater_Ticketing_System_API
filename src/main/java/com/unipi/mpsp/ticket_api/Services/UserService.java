package com.unipi.mpsp.ticket_api.Services;

import com.unipi.mpsp.ticket_api.DataClasses.AppUser;
import com.unipi.mpsp.ticket_api.Repositories.AppUserRepository;
import jakarta.transaction.Transactional;
import jakarta.xml.bind.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;

@Service
@Transactional()
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final AppUserService appUserService;
    private final AppUserRepository appUserRepository;

    @Transactional
    public AppUser create(AppUser appUser) throws ValidationException {
        if (appUserService.getUser(appUser.getEmail()) != null) {
            throw new ValidationException("User exists!");
        }
        if(!appUser.getRoles().contains("USER") && !appUser.getRoles().contains("ADMIN")) {
            appUser.setRoles(List.of("USER"));
        }
        return appUserService.saveUser(appUser,true);
    }

    /*@Transactional
    public UserView update(ObjectId id, UpdateUserRequest request) {
        var user = userRepo.getById(id);
        userEditMapper.update(request, user);

        user = userRepo.save(user);

        return userViewMapper.toUserView(user);
    }

    @Transactional
    public UserView upsert(CreateUserRequest request) {
        var optionalUser = userRepo.findByUsername(request.username());

        if (optionalUser.isEmpty()) {
            return create(request);
        } else {
            UpdateUserRequest updateUserRequest =
                    new UpdateUserRequest(request.fullName(), request.authorities());
            return update(optionalUser.get().getId(), updateUserRequest);
        }
    }

    @Transactional
    public UserView delete(ObjectId id) {
        var user = userRepo.getById(id);

        user.setUsername(
                user.getUsername().replace("@", String.format("_%s@", user.getId().toString())));
        user.setEnabled(false);
        user = userRepo.save(user);

        return userViewMapper.toUserView(user);
    }*/

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByEmail(email);
        if(appUser == null){
            throw new UsernameNotFoundException("User not found");
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        appUser.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        return new User(appUser.getEmail(),appUser.getPassword(), authorities);
    }
}