package test.pages.master;

import test.html.HtmlElement;
import test.html.components.standard.HtmlGenericElement;
import test.pages.HtmlContainerElement;


public class Header extends HtmlElement{
    private static final String DESIGN = "/resources/css/core.css";

    public Header(String title, String customCss) {
        super("head");
        HtmlContainerElement meta = new HtmlGenericElement("meta").addAttribute("http-equiv", "content-type").addAttribute("content", "text/html;charset=utf-8");

        HtmlContainerElement link = new HtmlGenericElement("link").addAttribute("rel", "stylesheet")
                                                  .addAttribute("href", DESIGN)
                                                  .addAttribute("type", "text/css")
                                                  .addAttribute("media", "handheld, all");

        add(meta);
        add(link);

        if (customCss != null && !customCss.equals("")) {
            HtmlElement customlink = new HtmlGenericElement("link").addAttribute("rel", "stylesheet")
                                                            .addAttribute("href", "/resources/css/" + customCss)
                                                            .addAttribute("type", "text/css")
                                                            .addAttribute("media", "handheld, all");
            add(customlink);
        }

        add(new HtmlGenericElement("title").addText("Bloatit - " + title));
    }

}