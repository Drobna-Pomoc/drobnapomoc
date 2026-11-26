package com.drobnapomoc.drobnapomoc.auth.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.model.AccountRoleRest;
import com.drobnapomoc.drobnapomoc.api.model.LoginInputRest;
import com.drobnapomoc.drobnapomoc.api.model.RegisterInputRest;
import com.drobnapomoc.drobnapomoc.auth.application.dto.CurrentUserDto;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SoftAssertionsExtension.class)
class AuthWebMapperTest {

    private static final String EMAIL = "anna@example.com";
    private static final String VALID_PHRASE = "Haslo123!";
    private static final String FIRST_NAME = "Anna";
    private static final String LAST_NAME = "Kowalska";
    private static final String PHONE = "+48123456789";

    @InjectSoftAssertions
    private SoftAssertions softly;

    private final AuthWebMapper mapper = Mappers.getMapper(AuthWebMapper.class);

    @Test
    void shouldMapRegisterInputToCommand() {
        // given
        final var input = new RegisterInputRest(EMAIL, VALID_PHRASE, FIRST_NAME, LAST_NAME, PHONE);

        // when
        final var command = mapper.toCommand(input);

        // then
        softly.assertThat(command.email()).isEqualTo(EMAIL);
        softly.assertThat(command.password()).isEqualTo(VALID_PHRASE);
        softly.assertThat(command.firstName()).isEqualTo(FIRST_NAME);
        softly.assertThat(command.lastName()).isEqualTo(LAST_NAME);
        softly.assertThat(command.phone()).isEqualTo(PHONE);
    }

    @Test
    void shouldMapLoginInputToCommand() {
        // given
        final var input = new LoginInputRest(EMAIL, VALID_PHRASE);

        // when
        final var command = mapper.toCommand(input);

        // then
        softly.assertThat(command.email()).isEqualTo(EMAIL);
        softly.assertThat(command.password()).isEqualTo(VALID_PHRASE);
    }

    @Test
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
        softly.assertThat(rest.getId()).isEqualTo(42L);
        softly.assertThat(rest.getEmail()).isEqualTo(EMAIL);
        softly.assertThat(rest.getFirstName()).isEqualTo(FIRST_NAME);
        softly.assertThat(rest.getLastName()).isEqualTo(LAST_NAME);
        softly.assertThat(rest.getPhone()).isEqualTo(PHONE);
        softly.assertThat(rest.getRoles()).containsExactlyInAnyOrder(AccountRoleRest.CLIENT, AccountRoleRest.ADMIN);
    }

    @ParameterizedTest
    @EnumSource(AccountRole.class)
    void shouldMapAllAccountRoles(final AccountRole role) {
        // given
        final var expected = AccountRoleRest.valueOf(role.name());

        // when
        final var mapped = mapper.toRest(role);

        // then
        assertThat(mapped).isEqualTo(expected);
    }

    @Test
    void shouldKeepAccountRoleInSyncWithGeneratedRestEnum() {
        // given
        final var domainRoles = Arrays.stream(AccountRole.values()).map(Enum::name).toList();
        final var restRoles = Arrays.stream(AccountRoleRest.values()).map(Enum::name).toList();

        // when & then
        assertThat(domainRoles).containsExactlyInAnyOrderElementsOf(restRoles);
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void shouldReturnNullForNullInputs() {
        // given
        final RegisterInputRest nullRegister = null;
        final LoginInputRest nullLogin = null;
        final CurrentUserDto nullDto = null;
        final AccountRole nullRole = null;
        final Set<AccountRole> nullRoles = null;

        // when & then
        softly.assertThat(mapper.toCommand(nullRegister)).isNull();
        softly.assertThat(mapper.toCommand(nullLogin)).isNull();
        softly.assertThat(mapper.toRest(nullDto)).isNull();
        softly.assertThat(mapper.toRest(nullRole)).isNull();
        softly.assertThat(mapper.toRestRoles(nullRoles)).isNull();
    }
}
