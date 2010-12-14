/*
 * Copyright (C) 2010 BloatIt.
 * 
 * This file is part of BloatIt.
 * 
 * BloatIt is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * BloatIt is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with
 * BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.web.html.components.standard.form;

import com.bloatit.web.html.components.standard.HtmlDiv;

/**
 * <p>
 * Class used to created html textarea blocks
 * </p>
 * <p>
 * 
 * <pre>
 * <div>
 *      <div>
 *          <label for="...">labeltext</label>
 *      </p>
 *      <div>
 *          <textarea name="..." class="cssClass" ...>defaultValue</textarea>
 *      </div>
 * </div>
 * </pre>
 * </p>
 */
public final class HtmlTextArea extends HtmlFormField<String> {

    public HtmlTextArea(final String name, int cols, int rows) {
        super(new HtmlSimpleTextArea( cols, rows), name);
    }

    public HtmlTextArea(final String name, final String label, int cols, int rows) {
        super(new HtmlSimpleTextArea(cols, rows), name, label );
    }

    /**
     * <p>
     * Sets the label for the object
     * </p>
     * <p>
     * 
     * @param label the label for the element
     */
    @Override
    public void setLabel(final String label) {
        this.label = new HtmlLabel(label);
        this.ph.add(new HtmlDiv().add(this.label));
        checkIdLabel();
    }

    @Override
    protected void doSetDefaultValue(final String value) {
        ((HtmlSimpleTextArea) this.element).setDefaultValue(value);
    }
}