package S12P11D110.ssacle.domain.auth.service;


import S12P11D110.ssacle.domain.auth.entity.CustomUserDetail;
import S12P11D110.ssacle.domain.user.entity.User;
import S12P11D110.ssacle.domain.user.repository.UserRepository;
import S12P11D110.ssacle.global.exception.AuthErrorException;
import S12P11D110.ssacle.global.exception.AuthErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public CustomUserDetail loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> findUser = userRepository.findByEmail(email);
        if (findUser.isEmpty()) {
            try {
                throw new AuthErrorException(AuthErrorStatus.GET_USER_FAILED);
            } catch (AuthErrorException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new CustomUserDetail(findUser.get());
        }
    }
}
