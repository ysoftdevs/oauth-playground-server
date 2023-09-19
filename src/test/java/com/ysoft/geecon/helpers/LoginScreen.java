package com.ysoft.geecon.helpers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;

import java.io.IOException;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class LoginScreen {

    private final FormElement form;

    public LoginScreen(Document doc) {
        this.form = doc.expectForm("form");
    }

    public Result submit(String username, String password) throws IOException {
        form.getElementsByAttributeValue("name", "username").val(username);
        form.getElementsByAttributeValue("name", "password").val(password);

        var document = form.submit().post();
        return new Result() {
            @Override
            public ConsentScreen expectSuccess() {
                return new ConsentScreen(document);
            }

            @Override
            public LoginScreen expectError(String error) {
                return new LoginScreen(document).expectError(error);
            }
        };
    }

    private LoginScreen expectError(String error) {
        assertThat(Objects.requireNonNull(form.getElementById("error-popup")).text(), containsString(error));
        return this;
    }

    public interface Result {
        ConsentScreen expectSuccess();

        LoginScreen expectError(String error);
    }
}
