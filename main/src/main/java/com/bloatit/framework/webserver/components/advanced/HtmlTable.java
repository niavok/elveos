package com.bloatit.framework.webserver.components.advanced;

import com.bloatit.framework.webserver.components.HtmlGenericElement;

/**
 * TODO : Fred has to comment this
 */
public class HtmlTable extends HtmlGenericElement {

    private final HtmlTableModel model;
    private final int columnCount;

    public HtmlTable(HtmlTableModel model) {
        super("table");
        this.model = model;

        columnCount = model.getColumnCount();

        generateHeader();
        generateBody();

    }

    private void generateBody() {
        while (model.next()) {
            HtmlGenericElement tr = new HtmlGenericElement("tr");

            for (int i = 0; i < columnCount; i++) {
                HtmlGenericElement td = new HtmlGenericElement("td");
                td.addText(model.getBody(i));
                if (model.getColumnCss(i) != null) {
                    td.setCssClass(model.getColumnCss(i));
                }
                tr.add(td);
            }
            add(tr);
        }
    }

    private void generateHeader() {
        HtmlGenericElement tr = new HtmlGenericElement("tr");

        for (int i = 0; i < columnCount; i++) {
            HtmlGenericElement th = new HtmlGenericElement("th");
            th.addText(model.getHeader(i));
            tr.add(th);
        }
        add(tr);
    }

    public static abstract class HtmlTableModel {
        public abstract int getColumnCount();
        public abstract String getHeader(int column);
        public abstract String getBody(int column);
        public abstract boolean next();

        public boolean hasHeader() {
            return true;
        }

        public String getColumnCss(int column) {
            return null;
        }
    }
}
