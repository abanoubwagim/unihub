package com.unihub.identity.application;

import java.security.SecureRandom;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.unihub.identity.api.dto.ResendVerificationRequest;
import com.unihub.identity.application.event.EmailVerificationRequestedEvent;
import com.unihub.identity.domain.User;
import com.unihub.identity.domain.UserRepository;
import com.unihub.shared.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResendVerificationService implements ResendVerificationUseCase {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
   
    @Override
    public void resendVerification(ResendVerificationRequest request) {
      
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found with email: " + email));

        if(user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        String otp = generateOtp();

        eventPublisher.publishEvent(
            new EmailVerificationRequestedEvent(user.getId(), user.getEmail(), otp)
        );
    }

    private String generateOtp(){
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    


}
