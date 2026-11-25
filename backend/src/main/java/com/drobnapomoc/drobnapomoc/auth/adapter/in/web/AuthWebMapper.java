package com.drobnapomoc.drobnapomoc.auth.adapter.in.web;

import com.drobnapomoc.drobnapomoc.api.model.AccountRoleRest;
import com.drobnapomoc.drobnapomoc.api.model.CurrentUserRest;
import com.drobnapomoc.drobnapomoc.api.model.LoginInputRest;
import com.drobnapomoc.drobnapomoc.api.model.RegisterInputRest;
import com.drobnapomoc.drobnapomoc.auth.application.command.LoginUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.command.RegisterUserCommand;
import com.drobnapomoc.drobnapomoc.auth.application.dto.CurrentUserDto;
import com.drobnapomoc.drobnapomoc.auth.domain.AccountRole;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface AuthWebMapper {

    RegisterUserCommand toCommand(RegisterInputRest rest);

    LoginUserCommand toCommand(LoginInputRest rest);

    CurrentUserRest toRest(CurrentUserDto dto);

    AccountRoleRest toRest(AccountRole role);

    Set<AccountRoleRest> toRestRoles(Set<AccountRole> roles);
}
