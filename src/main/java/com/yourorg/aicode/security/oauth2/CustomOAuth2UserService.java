package com.yourorg.aicode.security.oauth2;

import com.yourorg.aicode.model.User;
import com.yourorg.aicode.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        OAuth2UserInfo oAuth2UserInfo = new GoogleOAuth2UserInfo(oauth2User.getAttributes());
        
        if (!oAuth2UserInfo.getEmail().isEmpty()) {
            Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
            User user;
            
            if (userOptional.isPresent()) {
                user = userOptional.get();
                // Update existing user info
                user.setFirstName(oAuth2UserInfo.getFirstName());
                user.setLastName(oAuth2UserInfo.getLastName());
                user.setImageUrl(oAuth2UserInfo.getImageUrl());
            } else {
                // Register new user
                user = new User();
                user.setFirstName(oAuth2UserInfo.getFirstName());
                user.setLastName(oAuth2UserInfo.getLastName());
                user.setEmail(oAuth2UserInfo.getEmail());
                user.setProvider(User.AuthProvider.GOOGLE);
                user.setProviderId(oAuth2UserInfo.getId());
                user.setImageUrl(oAuth2UserInfo.getImageUrl());
                user.setEmailVerified(true);
            }
            
            user = userRepository.save(user);
        }
        
        return oauth2User;
    }
}
