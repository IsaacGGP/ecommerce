package org.isaac.com.ecommers.services;

import org.isaac.com.ecommers.exception.EmailAlreadyExistsException;
import org.isaac.com.ecommers.exception.InvalidCredentialsException;
import org.isaac.com.ecommers.exception.UserNotFoundException;
import org.isaac.com.ecommers.models.UserEntity;
import org.isaac.com.ecommers.models.dto.*;
import org.isaac.com.ecommers.repositories.UserRepository;
import org.isaac.com.ecommers.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    //Replicamos lo que tenemos en el constructor de dependencias de UserService
    @Mock
    private UserRepository userRepository; //Mock crea un repositorio Falso
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    //Caso de exito
    @Test
    void findById_ShouldReturnUser_WhenUserExists(){
        UserEntity entityMock = new UserEntity();
        entityMock.setId(1L);
        entityMock.setName("Isaac");
        entityMock.setEmail("isaac@gmail.com");
        entityMock.setRole("USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(entityMock));

        //Se ejecuta el metodo real
        UserResponse response = userService.findById(1L);
        //verifica que los datos regresados sean correctos
        assertEquals(1L, response.getId());
        assertEquals("Isaac", response.getName());
        assertEquals("isaac@gmail.com", response.getEmail());
        assertEquals("USER", response.getRole());

        //Verifica que el repositorio fue llamado
        verify(userRepository).findById(1L);
    }

    //Caso de error con devolucion de Excepcion
    @Test
    void findById_ShouldThrowException_WhenUserDoesNotExists(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findById(1L));
        verify(userRepository).findById(1L);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists(){
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setName("Isaac");
        entity.setEmail("isaac@gmail.com");
        entity.setRole("USER");
        when(userRepository.findByEmail("isaac@gmail.com")).thenReturn(Optional.of(entity));

        UserResponse response = userService.findByEmail("isaac@gmail.com");

        assertEquals(1L, response.getId());
        assertEquals("Isaac", response.getName());
        assertEquals("isaac@gmail.com", response.getEmail());
        assertEquals("USER", response.getRole());

        verify(userRepository).findByEmail("isaac@gmail.com");
    }

    @Test
    void findByEmail_ShouldThrowException_WhenEmailDoesNotExists(){
        when(userRepository.findByEmail("isaac@gmail.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findByEmail("isaac@gmail.com"));
        verify(userRepository).findByEmail("isaac@gmail.com");
    }

    @Test
    void createUser_ShouldCreateUser_WhenEmailDoesNotExists(){
        CreateUserRequest userDtoMock = new CreateUserRequest("pablo", "pablo@gmail.com", "12345");

        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setName("pablo");
        entity.setEmail("pablo@gmail.com");
        entity.setRole("USER");

        when(userRepository.findByEmail("pablo@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("12345")).thenReturn("HASHED_PASSWORD");
        //Guardamos el usuario en el repository
        when(userRepository.save(any(UserEntity.class))).thenReturn(entity);

        UserResponse response = userService.createUser(userDtoMock);
        assertEquals(1L,response.getId());
        assertEquals("pablo",response.getName());
        assertEquals("pablo@gmail.com",response.getEmail());
        assertEquals("USER",response.getRole());

        verify(userRepository).findByEmail("pablo@gmail.com");
        verify(passwordEncoder).encode("12345");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailExists(){
        CreateUserRequest request = new CreateUserRequest("pablo", "pablo@gmail.com", "12345");
        UserEntity userExist = new UserEntity();
        userExist.setId(1L);
        userExist.setEmail("pablo@gmail.com");
        when(userRepository.findByEmail("pablo@gmail.com")).thenReturn(Optional.of(userExist));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(request));

        verify(userRepository).findByEmail("pablo@gmail.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenDataIsValid(){
        UserEntity userExisting = new UserEntity();
        userExisting.setId(1L);
        userExisting.setName("raul");
        userExisting.setEmail("raul@gmail.com");
        userExisting.setRole("USER");

        UpdateUserRequest updateUser = new UpdateUserRequest("raul Gp", "raul124@gmail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userExisting));
        when(userRepository.findByEmail("raul124@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userExisting);

        UserResponse response = userService.updateUser(1L, updateUser);
        assertEquals("raul Gp", response.getName());
        assertEquals("raul124@gmail.com", response.getEmail());

        verify(userRepository).findById(1L);
        verify(userRepository).findByEmail("raul124@gmail.com");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserDoesNotExists(){
        UpdateUserRequest updateUser = new UpdateUserRequest("raul Gp", "raul124@gmail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, updateUser));
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateUser_ShouldThrowException_WhenEmailAlreadyExists(){
        UserEntity userExisting = new UserEntity();
        userExisting.setId(1L);
        userExisting.setName("raul");
        userExisting.setEmail("raul@gmail.com");
        userExisting.setRole("USER");

        UpdateUserRequest updateUser = new UpdateUserRequest("raul Gp", "raul124@gmail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(userExisting));
        when(userRepository.findByEmail("raul124@gmail.com")).thenReturn(Optional.of(userExisting));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.updateUser(   1L, updateUser));

        verify(userRepository).findById(1L);
        verify(userRepository).findByEmail("raul124@gmail.com");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    //Pruebas Delete
    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists(){
        UserEntity userExist = new UserEntity();
        userExist.setId(1L);
        userExist.setName("raul");
        userExist.setEmail("raul@gmail.com");
        userExist.setRole("USER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userExist));

        userService.deleteUser(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).delete(userExist);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserDoesNotExists(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository).findById(1L);
        verify(userRepository, never()).delete(any(UserEntity.class)); //Verifica que delete nunca haya sido llamado por ningun UserEntity
    }

    @Test
    void getAllUser_ShouldReturnUsers_WhenUsersExists(){
        UserEntity user1 = new UserEntity(1L, "pablo", "pablo@gmail.com", "12345", "USER");
        UserEntity user2 = new UserEntity(2L, "julieta", "Julieta@gmail.com", "14523", "USER");
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponse> response = userService.getAllUsers();

        assertEquals(2, response.size());
        assertEquals("pablo",response.get(0).getName());
        assertEquals("julieta",response.get(1).getName());

        verify(userRepository).findAll();
    }

    @Test
    void getAllUser_ShouldReturnEmptyList_WhenRepositoryIsEmpty(){
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<UserResponse> responses = userService.getAllUsers();
        assertTrue(responses.isEmpty());
        verify(userRepository).findAll();
    }

    //Login Test
    @Test
    void loginUser_ShouldReturnToken_WhenCredentialsAreValid(){
        UserEntity user = new UserEntity();
        user.setEmail("raul@gmail.com");
        user.setPassword("HASHED_PASSWORD");

        when(userRepository.findByEmail("raul@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "HASHED_PASSWORD")).thenReturn(true);
        when(jwtService.generateToken("raul@gmail.com")).thenReturn("JWT_TOKEN");

        LoginRequest loginRequest = new LoginRequest("raul@gmail.com", "123456");
        LoginResponse responseLogin = userService.loginUser(loginRequest);

        assertEquals("JWT_TOKEN", responseLogin.getToken());
        assertEquals("Login successful", responseLogin.getMessage());

        verify(userRepository).findByEmail("raul@gmail.com");
        verify(passwordEncoder).matches("123456", "HASHED_PASSWORD");
        verify(jwtService).generateToken("raul@gmail.com");
        //verifyNoInteractions(jwtService);
    }

    @Test
    void loginUser_ShouldThrowException_WhenEmailIsIncorrect(){
        when(userRepository.findByEmail("raul@gmail.com")).thenReturn(Optional.empty());
        LoginRequest loginRequest = new LoginRequest("raul@gmail.com", "123456");
        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(loginRequest));
        verify(userRepository).findByEmail("raul@gmail.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void loginUser_ShouldThrowException_WhenPasswordIsIncorrect(){
        UserEntity user = new UserEntity();
        user.setEmail("raul@gmail.com");
        user.setPassword("HASHED_PASSWORD");

        when(userRepository.findByEmail("raul@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "HASHED_PASSWORD")).thenReturn(false);

        LoginRequest loginRequest = new LoginRequest("raul@gmail.com", "123456");

        assertThrows(InvalidCredentialsException.class, () ->userService.loginUser(loginRequest));
        verify(userRepository).findByEmail("raul@gmail.com");
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString());

    }
}
