package pe.edu.upeu.lp2_gp2.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LoginAttempts {
    private final Cache<String, Window> perAddress = attempts();
    private final Cache<String, Window> perAccount = attempts();

    public void check(String address, String username) {
        if (increment(perAddress, address) > 40 || increment(perAccount, key(username)) > 8) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos. Vuelve a intentarlo en 15 minutos.");
        }
    }

    public void succeeded(String username) {
        perAccount.invalidate(key(username));
    }

    private static Cache<String, Window> attempts() {
        return Caffeine.newBuilder().maximumSize(20_000).expireAfterAccess(Duration.ofMinutes(16)).build();
    }

    private static int increment(Cache<String, Window> attempts, String key) {
        Instant now = Instant.now();
        return attempts.asMap().compute(key, (ignored, window) -> {
            if (window == null || !window.until().isAfter(now)) {
                return new Window(1, now.plus(Duration.ofMinutes(15)));
            }
            return new Window(Math.min(window.count() + 1, 41), window.until());
        }).count();
    }

    private static String key(String username) {
        return username.toLowerCase(Locale.ROOT);
    }
    private record Window(int count, Instant until) {}
}
