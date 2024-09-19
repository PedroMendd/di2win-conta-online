package com.di2win.contaonline.util;

import com.di2win.contaonline.dto.ClientResponseDTO;
import com.di2win.contaonline.entity.Client;

public class ClientMapper {

    public static ClientResponseDTO mapToClientResponseDTO(Client client) {
        ClientResponseDTO responseDTO = new ClientResponseDTO();
        responseDTO.setId(client.getId());
        responseDTO.setCpf(client.getCpf());
        responseDTO.setNome(client.getNome());
        responseDTO.setDataNascimento(client.getDataNascimento());

        return responseDTO;
    }
}
