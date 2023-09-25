package com.ysoft.geecon.repo;


import com.ysoft.geecon.dto.OAuthClient;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class ClientsRepo {
    private final Map<String, OAuthClient> clients = new HashMap<>();

    public ClientsRepo() {
        register(new OAuthClient("my-public-client", "Example public client", null, "https://localhost:8888/oauth_success"));
        register(new OAuthClient("oauthdebugger", "Example public client", null, "https://oauthdebugger.com/debug"));

        register(new OAuthClient("oauth-playground", "OAuth playground", null, "https://oauth-playground.online/flow/code-2"));
        register(new OAuthClient("oauth-playground-localhost", "OAuth playground", null, "http://localhost:5555/flow/code-2"));
    }

    public Optional<OAuthClient> getClient(String clientId) {
        return Optional.ofNullable(clients.get(clientId));
    }

    public void register(OAuthClient client) {
        clients.put(client.clientId(), client);
    }
}
