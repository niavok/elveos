package com.bloatit.web.html;

import java.util.Collections;
import java.util.Iterator;

import com.bloatit.web.server.Text;

/**
 * <p>
 * Class used to directly input text (any format) into the page.
 * </p>
 * <p>
 * Usage another_component.add(new HtmlText("<span class="plop">foo</span>));
 * </p>
 */
public class HtmlText extends HtmlNode {

    private final String content;

    /**
     * Creates a component to add raw Html to a page
     * 
     * @param content the Html string to add
     */
    public HtmlText(final String content) {
        super();
        this.content = content;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Iterator<HtmlNode> iterator() {
        return Collections.EMPTY_LIST.iterator();
    }

    /**
     * Do not use
     * Only present as a quick hack to write a tad cleaner html content
     */
    public String _getContent() {
        return content;
    }

    

    @Override
    public final void write(final Text txt) {
        txt.writeLine(content);
    }
}