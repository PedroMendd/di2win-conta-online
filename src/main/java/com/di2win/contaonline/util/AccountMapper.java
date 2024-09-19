package com.di2win.contaonline.util;

import com.di2win.contaonline.dto.AccountResponseDTO;
import com.di2win.contaonline.dto.ClientResponseDTO;
import com.di2win.contaonline.entity.Account;

public class AccountMapper {

    public static AccountResponseDTO mapToAccountResponseDTO(Account account) {
        AccountResponseDTO responseDTO = new AccountResponseDTO();
        responseDTO.setId(account.getId());
        responseDTO.setNumeroConta(account.getNumeroConta());
        responseDTO.setAgencia(account.getAgencia());
        responseDTO.setSaldo(account.getSaldo());
        responseDTO.setLimiteDiarioSaque(account.getLimiteDiarioSaque());
        responseDTO.setBloqueada(account.isBloqueada());

        ClientResponseDTO clientResponseDTO = new ClientResponseDTO();
        clientResponseDTO.setId(account.getCliente().getId());
        clientResponseDTO.setCpf(account.getCliente().getCpf());
        clientResponseDTO.setNome(account.getCliente().getNome());
        clientResponseDTO.setDataNascimento(account.getCliente().getDataNascimento());

        responseDTO.setCliente(clientResponseDTO);

        return responseDTO;
    }
}
