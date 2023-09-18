package com.ysoft.geecon.helpers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class ConsentScreen {

    private final FormElement consents;

    public ConsentScreen(Document document) {
        consents = document.expectForm("form");
    }

    public List<String> getScopes() {
        Elements checkboxes = consents.select("input[name=scope]");
        return checkboxes.stream().map(Element::val).toList();
    }

    public Document submit() throws IOException {
        return consents.submit().followRedirects(false).post();
    }
}
