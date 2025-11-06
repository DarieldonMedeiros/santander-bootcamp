package me.dio.santander_bootcamp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.dio.santander_bootcamp.controller.dto.AccountDto;
import me.dio.santander_bootcamp.controller.dto.CardDto;
import me.dio.santander_bootcamp.controller.dto.UserDto;
import me.dio.santander_bootcamp.domain.model.Account;
import me.dio.santander_bootcamp.domain.model.Card;
import me.dio.santander_bootcamp.domain.model.User;
import me.dio.santander_bootcamp.service.UserService;
import me.dio.santander_bootcamp.service.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("Testes para UserController")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp(){
        user = createUser();
        user.setId(1L);
        userDto = new UserDto(user);
    }

    // Teste para a requisição GET /users
    @Test
    @DisplayName("GET /users - Deve retornar 200 com lista de usuários")
    void shouldReturn200WithUserList() throws Exception {
        List<User> users = List.of(user, createUserWithId(2L));
        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value(user.getName()))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(userService).findAll();
    }

    @Test
    @DisplayName("GET /users - Deve retornar 200 com lista vazia quando não há usuários")
    void shouldReturn200WithEmptyList() throws Exception{
        when(userService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService).findAll();
    }

    // Testes para a requisição GET /users/{id}
    @Test
    @DisplayName("GET - /users/{id} - Deve retornar 200 com o usuário com ID existente")
    void shouldReturn200WithUserWhenIdExist() throws  Exception{
        Long id = 1L;
        when(userService.findById(id)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.account").exists())
                .andExpect(jsonPath("$.account.number").value(user.getAccount().getNumber()))
                .andExpect(jsonPath("$.card").exists())
                .andExpect(jsonPath("$.card.number").value(user.getCard().getNumber()));

        verify(userService).findById(id);
    }

    @Test
    @DisplayName("GET /users/{id} - Deve retornar 404 quando o usuário não existe")
    void shouldReturn404WhenUserDoesNotExist() throws Exception{
        Long id = 999L;
        when(userService.findById(id)).thenThrow(new NotFoundException());

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound());

        verify(userService).findById(id);
    }

    // Testes para a requisição POST /users
    @Test
    @DisplayName("POST /users - Deve retornar 201 ao criar um usuário com sucesso")
    void shouldReturn201WhenCreateUserSuccessfully() throws Exception{
        UserDto newUserDto = createUserDtoWithoutId();
        User createdUser = newUserDto.toModel();
        createdUser.setId(1L);

        when(userService.create(any(User.class))).thenReturn(createdUser);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "http://localhost/users/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(newUserDto.name()));

        verify(userService).create(any(User.class));
    }

    @Test
    @DisplayName("POST /users - Deve retornar 422 quando os dados são inválidos")
    void shouldReturn422WhenDataIsInvalid() throws Exception{
        UserDto invalidUserDto = createUserDtoWithoutId();
        invalidUserDto = new UserDto(null, null, null, null, null, null);

        when(userService.create(any(User.class))).thenThrow(new RuntimeException("Validation Error"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().is5xxServerError());

        verify(userService).create(any(User.class));
    }

    // Testes para a requisição PUT/users/{id}
    @Test
    @DisplayName("PUT /users/{id} - Deve retornar 200 ao atualizar um usuário com sucesso")
    void shouldReturn200WhenUpdateUserSuccessfully() throws Exception{
        Long id = 2L;
        UserDto updatedUserDto = createUserDtoWithId(id);
        User updatedUser = updatedUserDto.toModel();

        when(userService.update(eq(id), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(updatedUserDto.name()));

        verify(userService).update(eq(id), any(User.class));
    }

    @Test
    @DisplayName("PUT /users/{id} - Deve retornar 404 quando o usuário não existe")
    void shouldReturn404WhenUpdateNonExistentUser() throws Exception{
        Long id = 999L;
        UserDto userDto = createUserDtoWithId(id);

        when(userService.update(eq(id), any(User.class))).thenThrow(new NotFoundException());

        mockMvc.perform(put("/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound());

        verify(userService).update(eq(id), any(User.class));
    }

    // Testes para a requisição DELETE /users/{id}
    @Test
    @DisplayName("DELETE /users/{id} - Deve retornar 204 ao deletar um usuário com sucesso")
    void shouldReturn204WhenDeleteUserSuccessfully() throws Exception{
        Long id = 2L;
        doNothing().when(userService).delete(id);

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent());

        verify(userService).delete(id);
    }

    @Test
    @DisplayName("DELETE /users/{id} - Deve retornar 404 quando o usuário não existe")
    void shouldReturn404WhenDeleteNonExistentUser() throws Exception{
        Long id = 999L;
        doThrow(new NotFoundException())
                .when(userService).delete(id);

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNotFound());

        verify(userService).delete(id);
    }

    // MÉTODOS UTILITÁRIOS DE TESTE → Instâncias reais de entities com dados fictícios
    private User createUser(){
        User user = new User();
        user.setName("Darieldon");
        user.setAccount(createAccount());
        user.setCard(createCard());
        return user;
    }

    private User createUserWithId(Long id){
        User user = createUser();
        user.setId(id);
        user.setName("Rebeca");
        return user;
    }

    private UserDto createUserDtoWithoutId(){
        return new UserDto(
                null,
                "Darieldon",
                createAccountDto(),
                createCardDto(),
                null,
                null
        );
    }

    private UserDto createUserDtoWithId(Long id){
        return new UserDto(
                id,
                "Darieldon Atualizado",
                createAccountDto(),
                createCardDto(),
                null,
                null
        );
    }

    private Account createAccount(){
        Account account = new Account();
        account.setNumber("00000001-0");
        account.setAgency("0001");
        account.setBalance(new BigDecimal("1000.00"));
        account.setLimit(new BigDecimal("500.00"));
        return account;
    }

    private Card createCard(){
        Card card = new Card();
        card.setNumber("xxxx xxxx xxxx 0001");
        card.setLimit(new BigDecimal("500.00"));
        return card;
    }

    private AccountDto createAccountDto(){
        return new AccountDto(
                null,
                "00000001-0",
                "0001",
                new BigDecimal("1000.00"),
                new BigDecimal("500.00")
        );
    }

    private CardDto createCardDto(){
        return new CardDto(
                null,
                "xxxx xxxx xxxx 0001",
                new BigDecimal("500.00")
        );
    }
}
