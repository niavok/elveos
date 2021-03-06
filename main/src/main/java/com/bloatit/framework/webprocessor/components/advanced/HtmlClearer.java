//
// Copyright (c) 2011 Linkeos.
//
// This file is part of Elveos.org.
// Elveos.org is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Elveos.org is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
// You should have received a copy of the GNU General Public License along
// with Elveos.org. If not, see http://www.gnu.org/licenses/.
//
package com.bloatit.framework.webprocessor.components.advanced;

import com.bloatit.framework.webprocessor.components.HtmlGenericElement;

/**
 * HtmlClearer add to a page an invisible element with the clear:both css
 * property. This is useful to be sure that the next blocks will be displayed
 * below some float blocks.
 */
public class HtmlClearer extends HtmlGenericElement {

    public HtmlClearer() {
        super("hr");
        setCssClass("clearer");
    }

    @Override
    public boolean selfClosable() {
        return true;
    }

}
