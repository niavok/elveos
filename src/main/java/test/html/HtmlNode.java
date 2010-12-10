package test.html;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import test.pages.demand.DemandPage;

import com.bloatit.web.server.Language;
import com.bloatit.web.server.SessionManager;
import test.Context;
import test.IndentedHtmlText;
import test.Parameters;
import test.Request;
import test.Text;

/**
 * Class used to describe all elements used to render Html
 */
public abstract class HtmlNode implements Iterable<HtmlNode> {

    public static class Tag {

        private String tag;
        private Map<String, String> attributes = new HashMap<String, String>();

        public Tag(String tag) {
            super();
            this.tag = tag;
        }

        public Tag addAttribute(String name, String value) {
            if (!value.equals("") && !name.equals("")) {
                attributes.put(name, value);
            }
            return this;
        }

        /**
         * Finds the id of the tag
         * @return the value entetered for the field "id" or null if no field
         * id exists
         */
        public String getId(){
            return this.attributes.get("id");
        }

        public String getOpenTag() {
            return createOpenTagButWithoutLastChar().append(">").toString();
        }

        public String getClosedTag() {
            return createOpenTagButWithoutLastChar().append("/>").toString();
        }

        public String getCloseTag() {
            return "</" + tag + ">";
        }

        private StringBuilder createOpenTagButWithoutLastChar() {
            StringBuilder openTag = new StringBuilder();
            openTag.append("<");
            openTag.append(tag);
            for (Entry<String, String> att : attributes.entrySet()) {
                openTag.append(" ");
                openTag.append(att.getKey()).append("=").append("\"").append(att.getValue()).append("\"");
            }
            return openTag;
        }
    }

    /**
     * The id of the node
     */
    private String id;

    protected abstract void write(Text txt);

    public static void main(String[] args) {
        Text txt = new IndentedHtmlText() {

            @Override
            protected void append(String text) {
                System.out.print(text);
            }
        };
//        new HtmlBlock().write(plop);
//        
//        new HtmlBlock("cssClass").add(new HtmlButton("button"))
//                                 .add(new HtmlDateField(new Date(), "date !"))
//                                 .add(new HtmlImage(new Image("plop", ImageType.LOCAL)))
//                                 .add(new HtmlInput("text"))
//                                 .add(new HtmlLinkComponent("link", "other"))
//                                 .write(txt);
        com.bloatit.model.data.util.SessionManager.beginWorkUnit();
        Context.setSession(SessionManager.createSession());
        Context.getSession().setLanguage(new Language());
        DemandPage page = new DemandPage(new Request("demand", new Parameters("id", "321").add("title", "Hello")));
        page.create();
        page.write(txt);
        com.bloatit.model.data.util.SessionManager.endWorkUnitAndFlush();
    }
}