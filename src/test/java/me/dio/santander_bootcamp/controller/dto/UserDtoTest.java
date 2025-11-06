package me.dio.santander_bootcamp.controller.dto;

import me.dio.santander_bootcamp.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes para conversões de UserDto")
public class UserDtoTest {

    private User user;
    private Account account;
    private Card card;
    private List<Feature> features;
    private List<News> news;

    @BeforeEach
    void setUp(){
        account = createAccount();
        card = createCard();
        features = createFeatures();
        news = createNews();

        user = new User();
        user.setId(1L);
        user.setName("Darieldon");
        user.setAccount(account);
        user.setCard(card);
        user.setFeatures(features);
        user.setNews(news);
    }

    // Testes para UserDto - User
    @Test
    @DisplayName("Deve converter User para UserDto corretamente")
    void shouldConvertUserToUserDto(){
        UserDto userDto = new UserDto(user);

        assertNotNull(userDto);
        assertEquals(user.getId(), userDto.id());
        assertEquals(user.getName(), userDto.name());
        assertNotNull(userDto.account());
        assertEquals(user.getAccount().getNumber(), userDto.account().number());
        assertNotNull(userDto.card());
        assertEquals(user.getCard().getNumber(), userDto.card().number());
        assertNotNull(userDto.features());
        assertEquals(2, userDto.features().size());
        assertNotNull(userDto.news());
        assertEquals(2, userDto.news().size());
    }

    @Test
    @DisplayName("Deve converter User com campos null para UserDto")
    void shouldConvertUserWithNullFieldsToUserDto(){
        User userWithoutFields = new User();
        userWithoutFields.setId(1L);
        userWithoutFields.setName("Rebeca");

        UserDto userDto = new UserDto(userWithoutFields);

        assertNotNull(userDto);
        assertEquals(1L, userDto.id());
        assertEquals("Rebeca", userDto.name());
        assertNull(userDto.account());
        assertNull(userDto.card());
        assertNotNull(userDto.features());
        assertTrue(userDto.features().isEmpty());
        assertNotNull(userDto.news());
        assertTrue(userDto.news().isEmpty());
    }

    @Test
    @DisplayName("Deve converter UserDto com campos null para User")
    void shouldConvertUserDtoWithNullFieldsToUser(){
        UserDto userDto = new UserDto(
                1L,
                "Harry",
                null,
                null,
                null,
                null
        );

        User result = userDto.toModel();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Harry", result.getName());
        assertNull(result.getAccount());
        assertNull(result.getCard());
        assertNotNull(result.getFeatures());
        assertTrue(result.getFeatures().isEmpty());
        assertNotNull(result.getNews());
        assertTrue(result.getNews().isEmpty());
    }

    @Test
    @DisplayName("Deve manter idempotência na conversão User -> DTO -> User")
    void shouldMaintainIdempotencyInConversion(){
        User originalUser = user;

        UserDto userDto = new UserDto(originalUser);
        User convertedUser = userDto.toModel();

        assertEquals(originalUser.getId(), convertedUser.getId());
        assertEquals(originalUser.getName(), convertedUser.getName());
        assertEquals(originalUser.getAccount().getNumber(), convertedUser.getAccount().getNumber());
        assertEquals(originalUser.getCard().getNumber(), convertedUser.getCard().getNumber());
        assertEquals(originalUser.getFeatures().size(), convertedUser.getFeatures().size());
        assertEquals(originalUser.getNews().size(), convertedUser.getNews().size());
    }

    // Testes para AccountDto - Account
    @Test
    @DisplayName("Deve converter Account para AccountDto corretamente")
    void shouldConvertAccountToAccountDto(){
        Account account = createAccount();

        AccountDto accountDto = new AccountDto(account);

        assertNotNull(accountDto);
        assertEquals(account.getId(), accountDto.id());
        assertEquals(account.getNumber(), accountDto.number());
        assertEquals(account.getAgency(), accountDto.agency());
        assertEquals(account.getBalance(), accountDto.balance());
        assertEquals(account.getLimit(), accountDto.limit());
    }

    @Test
    @DisplayName("Deve converter AccountDto para Account corretamente")
    void shouldConvertAccountDtoToAccount(){
        AccountDto accountDto = new AccountDto(
                1L,
                "00000001-0",
                "0001",
                new BigDecimal("1000.00"),
                new BigDecimal("500.00")
        );

        Account account = accountDto.toModel();

        assertNotNull(account);
        assertEquals(accountDto.id(), account.getId());
        assertEquals(accountDto.number(), account.getNumber());
        assertEquals(accountDto.agency(), account.getAgency());
        assertEquals(accountDto.balance(), account.getBalance());
        assertEquals(accountDto.limit(), account.getLimit());
    }

    // Testes para CardDto - Card
    @Test
    @DisplayName("Deve converter Card para CardDto corretamente")
    void shouldConvertCardToCardDto(){
        Card card = createCard();

        CardDto cardDto = new CardDto(card);

        assertNotNull(cardDto);
        assertEquals(card.getId(), cardDto.id());
        assertEquals(card.getNumber(), cardDto.number());
        assertEquals(card.getLimit(), cardDto.limit());
    }

    @Test
    @DisplayName("Deve converter CardDto para Card corretamente")
    void shouldConvertCardDtoToCard(){
        CardDto cardDto = new CardDto(
                1L,
                "xxxx xxxx xxxx 0001",
                new BigDecimal("500.00")
        );

        Card card = cardDto.toModel();

        assertNotNull(card);
        assertEquals(cardDto.id(), card.getId());
        assertEquals(cardDto.number(), card.getNumber());
        assertEquals(cardDto.limit(), card.getLimit());
    }

    // MÉTODOS UTILITÁRIOS DE TESTE → Instâncias reais de entities com dados fictícios
    private Account createAccount(){
        Account account = new Account();
        account.setId(1L);
        account.setNumber("00000001-0");
        account.setAgency("0001");
        account.setBalance(new BigDecimal("1000.00"));
        account.setLimit(new BigDecimal("500.00"));
        return account;
    }

    private Card createCard(){
        Card card = new Card();
        card.setId(1L);
        card.setNumber("xxxx xxxx xxxx 0001");
        card.setLimit(new BigDecimal("500.00"));
        return card;
    }

    private List<Feature> createFeatures(){
        Feature feature1 = new Feature();
        feature1.setId(1L);
        feature1.setIcon("icon1.png");
        feature1.setDescription("Feature 1");

        Feature feature2 = new Feature();
        feature2.setId(2L);
        feature2.setIcon("icon2.png");
        feature2.setDescription("Feature2");

        return List.of(feature1, feature2);
    }

    private List<News> createNews(){
        News news1 = new News();
        news1.setId(1L);
        news1.setIcon("news1.png");
        news1.setDescription("News 1");

        News news2 = new News();
        news2.setId(2L);
        news2.setIcon("news2.png");
        news2.setDescription("News 2");

        return List.of(news1, news2);
    }

}
