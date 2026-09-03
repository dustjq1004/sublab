package me.kimyeonsup.home.seo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SeoController {

    @GetMapping("/sitemap.xml")
    public String sitemap() {
        return "sitemap.xml";
    }

    @GetMapping("/robots.txt")
    public String robots() {
        return "robots.txt";
    }
}
