package com.project.ewalet.controller;

import com.project.ewalet.config.auth.JwtTokenUtil;
import com.project.ewalet.mapper.OtpMapper;
import com.project.ewalet.model.*;
import com.project.ewalet.service.AsyncService;
import com.project.ewalet.service.EmailService;
import com.project.ewalet.service.JwtUserDetailsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import javax.validation.ValidationException;

@Configuration
@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private OtpMapper otpMapper;

    @Value("${twilio.account.sid}")
    private String accountSid;
    @Value("${twilio.auth.token}")
    private String authToken;
    @Value("${twilio.phonenumber.trial}")
    private String trialPhoneNumber;

    @PostMapping(value = "/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getEmail(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getEmail());

        final String token = jwtTokenUtil.generateToken(userDetails);

       // userDetailsService.updateToken(token, authenticationRequest.getEmail());

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping(value = "/sign-up")
    public ResponseEntity<?> saveUser(@Valid @RequestBody UserDTO user) throws Exception {
        JSONObject jsonObject = new JSONObject();
        System.out.println(user);
        //TODO user input validation and respose
        String CustomValidationResponse = "failed";

        String otpCode = new DecimalFormat("000000").format(new Random().nextInt(999999));
        //

        User savedUser = userDetailsService.save(user);
        Otp otp = new Otp();
        otp.setUser_id(savedUser.getId());
        otp.setCode(Integer.parseInt(otpCode));
        otp.setStatus(true);
        otpMapper.save(otp);

        sendSms(savedUser.getPhone_number(), otpCode);
        sendEmail(savedUser.getEmail(), otpCode);

        //TODO perform sendOtp() thru MQ
        /*JSONObject payload = new JSONObject();
        payload.put("key", 1);
        payload.put("email", savedUser.getEmail());
        payload.put("phone_number", savedUser.getPhone_number());
        payload.put("otp_code", otpCode);
        MQProducer.sendOtp(payload.toString());*/

        if (savedUser != null) {
            jsonObject.put("status", 200);
            jsonObject.put("message", "created");
            return new ResponseEntity<>(jsonObject, HttpStatus.OK);
        } else {
            jsonObject.put("status", 400);
            jsonObject.put("message", CustomValidationResponse);
            return new ResponseEntity<>(jsonObject, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    @Async
    String sendSms(String phoneNumber, String code) {
        String body = "Your E-Walet verification code is " + code;

        Twilio.init(accountSid, authToken);
        Message message = Message
                .creator(new PhoneNumber("+" + phoneNumber),
                        new PhoneNumber(trialPhoneNumber),
                        body)
                .create();
        System.out.println(message.getStatus());
        return "OTP sent";
    }

    @Autowired
    private AsyncService service;

    void sendEmail(String toEmail, String code) {
        CompletableFuture<JSONObject> sendEmail = service.sendEmail(toEmail, code);
    }

}