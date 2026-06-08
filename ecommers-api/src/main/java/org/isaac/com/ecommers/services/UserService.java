package org.isaac.com.ecommers.services;

import org.isaac.com.ecommers.exception.EmailAlreadyExistsException;
import org.isaac.com.ecommers.exception.InvalidCredentialsException;
import org.isaac.com.ecommers.models.dto.*;
import org.isaac.com.ecommers.models.UserEntity;
import org.isaac.com.ecommers.exception.UserNotFoundException;
import org.isaac.com.ecommers.repositories.UserRepository;
import org.isaac.com.ecommers.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    //Refactorizacion de campos
    public UserResponse mapToResponse(UserEntity entity){
        UserResponse response = new UserResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setEmail(entity.getEmail());
        response.setRole(entity.getRole());
        return response;
    }

    public UserResponse findByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(()
                -> new UserNotFoundException("User not found"));

        return mapToResponse(userEntity); //cambio a mapToResponse
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setName(request.getName());
        userEntity.setEmail(request.getEmail());
        userEntity.setPassword(passwordEncoder.encode(request.getPassword())); //Haseha la contraseña
        userEntity.setRole("USER");

        UserEntity saveUser = userRepository.save(userEntity);

        return mapToResponse(saveUser); //cambio a mapToRespon
    }

    //Seguimiento del CRUD
    public UserResponse findById(Long id){
        UserEntity entity = userRepository.findById(id).orElseThrow(() ->
                new UserNotFoundException("User not found"));

        return mapToResponse(entity);
    }

    //Actualizar Usuario
    public UserResponse updateUser(Long id, UpdateUserRequest update){
        UserEntity entity = userRepository.findById(id).orElseThrow(() ->
                new UserNotFoundException("User not found"));

        if (!entity.getEmail().equals(update.getEmail()) && userRepository.findByEmail(update.getEmail()).isPresent()){
            throw new EmailAlreadyExistsException("Email already exists");
        }

        entity.setName(update.getName());
        entity.setEmail(update.getEmail());

        UserEntity saveUser = userRepository.save(entity);

        return mapToResponse(saveUser); //cambio a mapToRespon
    }

    //Eliminar Usuario
    public void deleteUser(Long id){
        UserEntity entity = userRepository.findById(id).orElseThrow(() ->
                new UserNotFoundException("User not found"));
        userRepository.delete(entity);
    }

    //Obtener todos los usuarios
    public List<UserResponse> getAllUsers(){
        /*//Version sin simplificar
        List<UserEntity> users = userRepository.findAll();
        List<UserResponse> usersSave = new ArrayList<>();
        for (UserEntity entity : users){
            usersSave.add(mapToResponse(entity));
        }
        return usersSave;*/
        return userRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    //Login
    public LoginResponse loginUser(LoginRequest loginRequest) {
        UserEntity entity = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(
                () -> new InvalidCredentialsException("Invalid credentials")
        );
        if (!passwordEncoder.matches(
                loginRequest.getPassword(), entity.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        String token = jwtService.generateToken(entity.getEmail());
        return new LoginResponse(token, "Login successful");
    }
}
