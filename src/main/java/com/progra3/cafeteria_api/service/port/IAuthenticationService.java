package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.dto.LoginRequestDTO;
import com.progra3.cafeteria_api.model.dto.LoginResponseDTO;

public interface IAuthenticationService {

    LoginResponseDTO login(LoginRequestDTO loginDTO);
}
