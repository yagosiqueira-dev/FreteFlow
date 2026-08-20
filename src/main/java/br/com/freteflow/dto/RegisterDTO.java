package br.com.freteflow.dto;
import br.com.freteflow.entity.UserRole;

public record RegisterDTO(String name, String email, String password, UserRole role) {
}