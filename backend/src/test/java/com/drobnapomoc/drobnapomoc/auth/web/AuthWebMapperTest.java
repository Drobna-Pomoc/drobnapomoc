package com.drobnapomoc.drobnapomoc.auth.web;

import com.drobnapomoc.drobnapomoc.api.model.AccountRoleRest;
import com.drobnapomoc.drobnapomoc.api.model.LoginInputRest;
import com.drobnapomoc.drobnapomoc.api.model.RegisterInputRest;
import com.drobnapomoc.drobnapomoc.auth.application.command.CurrentUserDto;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthWebMapper - testy mapowania warstwy web")
class AuthWebMapperTest {

    private static final String EMAIL = "anna@example.com";
    private static final String VALID_PHRASE = "Haslo123!";
    private static final String FIRST_NAME = "Anna";
    private static final String LAST_NAME = "Kowalska";
    private static final String PHONE = "+48123456789";

    private final AuthWebMapper mapper = Mappers.getMapper(AuthWebMapper.class);

    @Test
    @DisplayName("Powinno zmapować RegisterInputRest na RegisterUserCommand")
    void shouldMapRegisterInputToCommand() {
        // given
        final var input = new RegisterInputRest(EMAIL, VALID_PHRASE, FIRST_NAME, LAST_NAME, PHONE);

        // when
        final var command = mapper.toCommand(input);

        // then
        assertThat(command.email()).isEqualTo(EMAIL);
        assertThat(command.password()).isEqualTo(VALID_PHRASE);
        assertThat(command.firstName()).isEqualTo(FIRST_NAME);
        assertThat(command.lastName()).isEqualTo(LAST_NAME);
        assertThat(command.phone()).isEqualTo(PHONE);
    }

    @Test
    @DisplayName("Powinno zmapować LoginInputRest na LoginUserCommand")
    void shouldMapLoginInputToCommand() {
        // given
        final var input = new LoginInputRest(EMAIL, VALID_PHRASE);

        // when
        final var command = mapper.toCommand(input);

        // then
        assertThat(command.email()).isEqualTo(EMAIL);
        assertThat(command.password()).isEqualTo(VALID_PHRASE);
    }

    @Test
    @DisplayName("Powinno zmapować CurrentUserDto na CurrentUserRest")
    void shouldMapCurrentUserDtoToRest() {
        // given
        final var dto = new CurrentUserDto(
                42L,
                EMAIL,
                FIRST_NAME,
                LAST_NAME,
                PHONE,
                Set.of(AccountRole.CLIENT, AccountRole.ADMIN)
        );

        // when
        final var rest = mapper.toRest(dto);

        // then
        assertThat(rest.getId()).isEqualTo(42L);
        assertThat(rest.getEmail()).isEqualTo(EMAIL);
        assertThat(rest.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(rest.getLastName()).isEqualTo(LAST_NAME);
        assertThat(rest.getPhone()).isEqualTo(PHONE);
        assertThat(rest.getRoles()).containsExactlyInAnyOrder(AccountRoleRest.CLIENT, AccountRoleRest.ADMIN);
    }

    @ParameterizedTest
    @EnumSource(AccountRole.class)
    @DisplayName("Powinno zmapować każdą rolę domenową na odpowiednik REST")
    void shouldMapAllAccountRoles(final AccountRole role) {
        // given
        final var expected = AccountRoleRest.valueOf(role.name());

        // when
        final var mapped = mapper.toRest(role);

        // then
        assertThat(mapped).isEqualTo(expected);
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    @DisplayName("Powinno zwrócić null przy mapowaniu argumentów null")
    void shouldReturnNullForNullInputs() {
        // given
        final RegisterInputRest nullRegister = null;
        final LoginInputRest nullLogin = null;
        final CurrentUserDto nullDto = null;
        final AccountRole nullRole = null;
        final Set<AccountRole> nullRoles = null;

        // when & then
        assertThat(mapper.toCommand(nullRegister)).isNull();
        assertThat(mapper.toCommand(nullLogin)).isNull();
        assertThat(mapper.toRest(nullDto)).isNull();
        assertThat(mapper.toRest(nullRole)).isNull();
        assertThat(mapper.toRestRoles(nullRoles)).isNull();
    }
}
