package me.dio.santander_bootcamp.service.impl;

import me.dio.santander_bootcamp.domain.model.*;
import me.dio.santander_bootcamp.domain.repository.UserRepository;
import me.dio.santander_bootcamp.service.exception.BusinessException;
import me.dio.santander_bootcamp.service.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para UserServiceImpl")
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Account account;
    private Card card;

    @BeforeEach
    void setUp(){
        // Preparação para os testes
        account = createAccount();
        card = createCard();
        user = createUser();
    }

    // Testes para a função findAll()
    @Test
    @DisplayName("Deve retornar lista de usuários quando existirem usuários")
    void shouldReturnUsersListWhenUsersExists(){
        List<User> users = List.of(user, createUserWithId(2L));
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não existirem usuários")
    void shouldReturnEmptyListWhenNoUsersExist(){
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        List<User> result = userService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    // Testes para a função findById()
    @Test
    @DisplayName("Deve retornar usuário quando ID existe")
    void shouldReturnUserWhenIdExists(){
        Long id = 1L;
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getAccount(), result.getAccount());
        assertEquals(user.getCard(), result.getCard());
        assertEquals(user.getFeatures(), result.getFeatures());
        assertEquals(user.getNews(), result.getNews());
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando o ID não existe")
    void shouldThrowNotFoundExceptionWhenIdDoesNotExist(){
        Long id = 999L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.findById(id)
        );

        assertNotNull(exception);
        assertEquals("Resource not found.", exception.getMessage());
        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any());
    }

    // Testes para a função create()
    @Test
    @DisplayName("Deve lançar BusinessException quando usuário é null")
    void shouldThrowBusinessExceptionWhenUserIsNull(){
        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.create(null) // ✅ Passa null para exercitar a validação
        );

        assertEquals("User to create must not be null.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve criar usuário quando os dados são válidos")
    void shouldCreateUserWhenDataIsValid(){
        User newUser = createUser();
        newUser.setId(null);

        when(userRepository.existsByAccountNumber(newUser.getAccount().getNumber()))
                .thenReturn(false);
        when(userRepository.existsByCardNumber(newUser.getCard().getNumber()))
                .thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocationOnMock -> {
                    User saved = invocationOnMock.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        User result = userService.create(newUser);

        assertNotNull(result);
        assertNotNull(result.getId());
        verify(userRepository).existsByAccountNumber(newUser.getAccount().getNumber());
        verify(userRepository).existsByCardNumber(newUser.getCard().getNumber());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando conta é null")
    void shouldThrowBusinessExceptionWhenAccountIsNull(){
        User userWithoutAccount = createUser();
        userWithoutAccount.setAccount(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.create(userWithoutAccount)
        );

        assertEquals("User account must not be null.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando cartão é nulo")
    void shouldThrowBusinessExceptionWhenCardIsNull(){
        User userWithoutCard = createUser();
        userWithoutCard.setCard(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.create(userWithoutCard)
        );

        assertEquals("User card must not be null.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o número de conta já existe")
    void shouldThrowBusinessExceptionWhenNumberAccountsAlreadyExist() {
        User newUser = createUser();
        String accountNumber = "00000001-0";

        when(userRepository.existsByAccountNumber(accountNumber)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.create(newUser)
        );

        assertEquals("This account number already exists.", exception.getMessage());
        verify(userRepository).existsByAccountNumber(accountNumber);
        verify(userRepository, never()).existsByCardNumber(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando o número do cartão já existe")
    void shouldThrowBusinessExceptionWhenNumberCardAlreadyExist(){
        User newUser = createUser();
        String cardNumber = "xxxx xxxx xxxx 0001";

        when(userRepository.existsByCardNumber(cardNumber)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.create(newUser)
        );

        assertEquals("This card number already exists.", exception.getMessage());
        verify(userRepository).existsByAccountNumber(anyString());
        verify(userRepository).existsByCardNumber(cardNumber);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar criar usuário com ID 1")
    void shouldThrowBusinessExceptionWhenTryingToCreateUserWithId1(){
        User userWithId1 = createUser();
        userWithId1.setId(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.create(userWithId1)
        );

        assertTrue(exception.getMessage().contains("can not be created"));
        verify(userRepository, never()).save(any());
    }

    // Testes para a função update()

    @Test
    @DisplayName("Deve atualizar usuário quando os dados são válidos")
    void shouldUpdateUserWhenDataIsValid(){
        Long id = 2L;
        User existingUser = createUserWithId(id);
        User updatedUser = createUserWithId(id);
        updatedUser.setName("Nome atualizado");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArgument(0));

        User result = userService.update(id, updatedUser);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Nome atualizado", result.getName());
        verify(userRepository).findById(id);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando os IDs são diferentes")
    void shouldThrowBusinessExceptionWhenIdsAreDifferentOnUpdate(){
        Long idPath = 2L;
        Long idBody = 3L;
        User updatedUser = createUserWithId(idBody);

        when(userRepository.findById(idPath)).thenReturn(Optional.of(createUserWithId(idPath)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.update(idPath, updatedUser)
        );

        assertEquals("Update IDs must be the same.", exception.getMessage());
        verify(userRepository).findById(idPath);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar atualizar o usuário com o ID 1")
    void shouldThrowBusinessExceptionWhenTryingToUpdateUserWithId1(){
        Long id = 1L;
        User updatedUser = createUserWithId(id);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.update(id, updatedUser)
        );

        assertTrue(exception.getMessage().contains("can not be updated"));
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar todos os campos do usuário")
    void shouldUpdateAllUserFields(){
        Long id = 2L;
        User existingUser = createUserWithId(id);
        User updatedUser = createUserWithId(id);

        Account newAccount = createAccount();
        newAccount.setNumber("00000121-1");
        Card newCard = createCard();
        newCard.setNumber("xxxx xxxx xxxx 1234");

        updatedUser.setName("Novo nome");
        updatedUser.setAccount(newAccount);
        updatedUser.setCard(newCard);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArgument(0)
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        User result = userService.update(id, updatedUser);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("Novo nome", savedUser.getName());
        assertEquals("00000121-1", savedUser.getAccount().getNumber());
        assertEquals("xxxx xxxx xxxx 1234", savedUser.getCard().getNumber());
    }

    // Testes para a função delete()
    @Test
    @DisplayName("Deve deletar usuário quando o ID existe")
    void shouldDeleteUserWhenIdExist(){
        Long id = 2L;
        User userToDelete = createUserWithId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(userToDelete));
        doNothing().when(userRepository).delete(any(User.class));

        assertDoesNotThrow(() -> userService.delete(id));

        verify(userRepository).findById(id);
        verify(userRepository).delete(userToDelete);
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar deletar o usuário com o ID 1")
    void shouldThrowBusinessExceptionWhenTryingToDeleteUserWithId1(){
        Long id = 1L;

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.delete(id)
        );

        assertTrue(exception.getMessage().contains("can not be deleted"));
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao tentar deletar usuário inexistente")
    void shouldThrowNotFoundExceptionWhenTryingToDeleteNonExistentUser(){
        Long id = 999L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> userService.delete(id)
        );

        verify(userRepository).findById(id);
        verify(userRepository, never()).delete(any());
    }

    // MÉTODOS UTILITÁRIOS DE TESTE → Instâncias reais de entities com dados fictícios
    private User createUser() {
        User user = new User();
        user.setName("Darieldon");
        user.setAccount(createAccount());
        user.setCard(createCard());
        user.setFeatures(createFeatures());
        user.setNews(createNews());
        return user;
    }

    private User createUserWithId(Long id){
        User user = createUser();
        user.setId(id);
        return user;
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
        card.setLimit(new BigDecimal("2000.00"));
        return card;
    }

    private List<Feature> createFeatures(){
        Feature feature1 = new Feature();
        feature1.setIcon("icon1.png");
        feature1.setDescription("Feature 1");

        Feature feature2 = new Feature();
        feature2.setIcon("icon2.png");
        feature2.setDescription("Feature 2");

        return List.of(feature1, feature2);
    }

    private List<News> createNews(){
        News news1 = new News();
        news1.setIcon("news1.png");
        news1.setDescription("News 1");

        News news2 = new News();
        news2.setIcon("news 2.png");
        news2.setDescription("News 2");

        return List.of(news1, news2);
    }
}
