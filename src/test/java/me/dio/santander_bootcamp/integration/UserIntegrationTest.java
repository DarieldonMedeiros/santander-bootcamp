package me.dio.santander_bootcamp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.dio.santander_bootcamp.controller.dto.AccountDto;
import me.dio.santander_bootcamp.controller.dto.CardDto;
import me.dio.santander_bootcamp.controller.dto.UserDto;
import me.dio.santander_bootcamp.domain.model.Account;
import me.dio.santander_bootcamp.domain.model.Card;
import me.dio.santander_bootcamp.domain.model.User;
import me.dio.santander_bootcamp.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de integração para User")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        // Limpar o banco de dados antes de cada teste
        userRepository.deleteAll();

        // Criar usuário que receberá ID 1
        User tempUser = createUser();
        tempUser.getAccount().setNumber("00000001-1");
        tempUser.getCard().setNumber("xxxx xxxx xxxx 0002");
        User saved = userRepository.save(tempUser);
    }

    @Test
    @DisplayName("Deve criar, buscar, atualizar e deletar usuário completo")
    void shouldCreateSearchUpdateAndDeleteUserComplete() throws Exception{
        // ========== 1. Criar usuário ==========
        UserDto userDto = createUserDto();

        String userJson = objectMapper.writeValueAsString(userDto);

        String response = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto createdUser = objectMapper.readValue(response, UserDto.class);
        Long userId = createdUser.id();
        assertNotNull(userId);

        // Verificar se foi salvo no banco de dados
        assertTrue(userRepository.existsById(userId));
        User savedUser = userRepository.findById(userId).orElseThrow();
        assertEquals(userDto.name(), savedUser.getName());

        // ========== 2. Buscar usuário criado ==========
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value(userDto.name()))
                .andExpect(jsonPath("$.account.number").value(userDto.account().number()));

        // ========== 3. Buscar todos os usuários ==========
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        // ========== 4. Atualizar usuário ==========
        User dataUser = userRepository.findById(userId).orElseThrow();
        UserDto createdUserDto = new UserDto(dataUser);

        UserDto updatedUserDto = new UserDto(
                userId,
                "Nome Atualizado",
                createdUserDto.account(),
                createdUserDto.card(),
                createdUserDto.features(),
                createdUserDto.news()
        );

        mockMvc.perform(put("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome Atualizado"));

        // Verificar se o nome foi atualizado no banco de dados
        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals("Nome Atualizado", updatedUser.getName());

        // ========== 5. Deletar usuário ==========
        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        // Verificar se o usuário foi deletado do banco
        assertFalse(userRepository.existsById(userId));

    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar usuário inexistente")
    void shouldReturn404WhenSearchingForNonExistingUser() throws Exception{
        mockMvc.perform(get("/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 422 ao tentar criar usuário com número de conta duplicado")
    void shouldReturn422WhenCreateUserWithDuplicatedAccount() throws Exception{
        UserDto userDto1 = createUserDto();
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isCreated());

        UserDto userDto2 = new UserDto(
                null,
                "Outro usuário",
                userDto1.account(),
                createCardDto(),
                null,
                null
        );

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto2)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Deve retornar 422 ao criar usuário nulo")
    void shouldReturn422WhenTryingToCreateNullUser() throws Exception{
        UserDto invalidUser = new UserDto(
                null,
                null,
                null,
                null,
                null,
                null
        );
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Deve retornar 422 ao tentar deletar usuário com ID 1")
    void shouldReturn422WhenTryingToDeleteUserWithId1() throws Exception{
        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isUnprocessableEntity());
    }

    // MÉTODOS UTILITÁRIOS DE TESTE → Instâncias reais de entities com dados fictícios
    private UserDto createUserDto(){
        return new UserDto(
                null,
                "Darieldon",
                createAccountDto(),
                createCardDto(),
                null,
                null
        );
    }

    private User createUser(){
        User user = new User();
        user.setName("Darieldon");
        Account account = new Account();
        account.setNumber("00000001-0");
        account.setAgency("0001");
        account.setBalance(new BigDecimal("1000.00"));
        account.setLimit(new BigDecimal("500.00"));
        user.setAccount(account);

        Card card = new Card();
        card.setNumber("xxxx xxxx xxxx 0001");
        card.setLimit(new BigDecimal("2000.00"));
        user.setCard(card);

        return user;
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
                new BigDecimal("2000.00")
        );
    }
}
