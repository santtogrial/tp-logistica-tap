package ar.edu.up.tap.logistica.seguridad;

import ar.edu.up.tap.logistica.Fixtures;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // Clave de 256 bits solo para tests
    private static final String SECRETO = "dGVzdC1zZWNyZXRvLXBhcmEtbG9zLXRlc3RzLTI1Ni1iaXRzISE=";

    private final JwtService jwtService = new JwtService(SECRETO, 60);

    @Test
    void elTokenGeneradoDevuelveElEmailDelUsuario() {
        String token = jwtService.generarToken(Fixtures.operador());

        assertThat(jwtService.extraerEmail(token)).isEqualTo("operador@logistica.com");
    }

    @Test
    void unTokenModificadoSeRechaza() {
        String token = jwtService.generarToken(Fixtures.operador());
        String alterado = token.substring(0, token.length() - 4) + "AAAA";

        assertThatThrownBy(() -> jwtService.extraerEmail(alterado)).isInstanceOf(JwtException.class);
    }

    @Test
    void unTokenVencidoSeRechaza() {
        JwtService vencido = new JwtService(SECRETO, -1);
        String token = vencido.generarToken(Fixtures.operador());

        assertThatThrownBy(() -> jwtService.extraerEmail(token)).isInstanceOf(JwtException.class);
    }
}
