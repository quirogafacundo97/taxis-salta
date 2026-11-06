
package com.unsa.taxis.dto;

import java.util.List;

public record LoginResponse(
        String token,
        String username,
        String nombreCompleto,
        List<String> roles,
        boolean debeCambiarPassword
) {
}
