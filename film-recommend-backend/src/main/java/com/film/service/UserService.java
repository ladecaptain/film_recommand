package com.film.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.film.dto.LoginRequest;
import com.film.dto.RegisterRequest;
import com.film.entity.User;
import com.film.exception.BusinessException;
import com.film.mapper.UserMapper;
import com.film.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getPhone, request.getPhoneOrEmail())
            .or()
            .eq(User::getEmail, request.getPhoneOrEmail()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "账号或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId());
        user.setPassword(null);
        return Map.of("token", token, "userInfo", user);
    }

    public Map<String, Object> register(RegisterRequest request) {
        if ((request.getPhone() == null || request.getPhone().isBlank())
            && (request.getEmail() == null || request.getEmail().isBlank())) {
            throw new BusinessException("手机号或邮箱至少填一个");
        }
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>()
            .eq(request.getPhone() != null, User::getPhone, request.getPhone())
            .or()
            .eq(request.getEmail() != null, User::getEmail, request.getEmail()));
        if (exists) {
            throw new BusinessException("该手机号或邮箱已注册");
        }
        User user = new User();
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : "影迷" + System.currentTimeMillis() % 10000);
        userMapper.insert(user);
        String token = jwtUtil.generateToken(user.getId());
        user.setPassword(null);
        return Map.of("token", token, "userInfo", user);
    }

    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }
}
