package com.ysoft.geecon;

import com.ysoft.geecon.dto.OAuthClient;
import com.ysoft.geecon.dto.User;
import com.ysoft.geecon.repo.ClientsRepo;
import com.ysoft.geecon.repo.UsersRepo;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@QuarkusTest
public class AuthCodeGrantTest {
    @Inject
    ClientsRepo clientsRepo;
    @Inject
    UsersRepo usersRepo;

    @TestHTTPResource("auth")
    String authUrl;

    @BeforeEach
    void beforeAll() {
        clientsRepo.register(new OAuthClient("myclient", "", null, "https://myserver:8888/success"));
        usersRepo.register(new User("bob", "password"));
    }

    @Test
    public void authCodeGrant() throws IOException {
        String state = "test state is not random";
        FormElement login = Jsoup.connect(authUrl)
                .data("client_id", "myclient")
                .data("redirect_uri", "https://myserver:8888/success")
                .data("state", state)
                .data("scope", "scope1 scope2")
                .get().forms().get(0);
        login.getElementsByAttributeValue("name", "username").val("bob");
        login.getElementsByAttributeValue("name", "password").val("password");

        Document consentsDoc = login.submit().post();
        FormElement consents = consentsDoc.expectForm("form");

        consents.expectFirst("input[name=scope][value=scope1]");
        consents.expectFirst("input[name=scope][value=scope2]");

        Document success = consents.submit().followRedirects(false).post();
        Connection.Response response = success.connection().response();
        assertThat(response.statusCode(), is(303));
        assertThat(response.header("location"), startsWith("https://myserver:8888/success"));

        URI location = URI.create(Objects.requireNonNull(response.header("location")));
        Map<String, String> query = URLEncodedUtils.parse(location.getQuery(), Charset.defaultCharset())
                .stream().collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

        assertThat(query.get("state"), is(state));
        assertThat(query.get("code"), is(notNullValue()));

        given()
                .formParam("grant_type", "authorization_code")
                .formParam("client_id", "myclient")
                .formParam("redirect_uri", "https://myserver:8888/success")
                .formParam("code", query.get("code"))
                .when()
                .post("/auth/token")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("token_type", is("Bearer"))
                .body("expires_in", is(notNullValue()))
                .body("access_token", is(notNullValue()))
                .body("refresh_token", is(notNullValue()));
    }

}