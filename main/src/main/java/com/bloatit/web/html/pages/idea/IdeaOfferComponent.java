/*
 * Copyright (C) 2010 BloatIt. This file is part of BloatIt. BloatIt is free software: you
 * can redistribute it and/or modify it under the terms of the GNU Affero General Public
 * License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version. BloatIt is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details. You should have received a copy of the GNU Affero General
 * Public License along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.web.html.pages.idea;

import com.bloatit.framework.Offer;
import com.bloatit.web.html.HtmlElement;
import com.bloatit.web.html.components.standard.HtmlDiv;
import com.bloatit.web.html.components.standard.HtmlImage;
import com.bloatit.web.html.components.standard.HtmlParagraph;
import com.bloatit.web.html.pages.master.HtmlPageComponent;
import com.bloatit.web.server.Context;
import com.bloatit.web.server.Session;

public class IdeaOfferComponent extends HtmlPageComponent {

    // private Offer offer;
    private HtmlParagraph description;
    private HtmlParagraph title;
    private HtmlParagraph price;
    private HtmlParagraph expirationDate;
    private HtmlParagraph creationDate;
    private HtmlImage authorAvatar;
    private HtmlParagraph author;
    private final Offer offer;

    public IdeaOfferComponent(final Offer offer) {
        super();
        this.offer = offer;
        extractData();
        add(produce());
    }

    protected HtmlElement produce() {
        final HtmlDiv offerBlock = new HtmlDiv("offer_block");
        {
            offerBlock.add(authorAvatar);

            final HtmlDiv offerInfoBlock = new HtmlDiv("offer_info_block");
            {
                offerInfoBlock.add(author);
                offerInfoBlock.add(price);
                offerInfoBlock.add(expirationDate);
                offerInfoBlock.add(creationDate);
            }

            offerBlock.add(offerInfoBlock);
            offerBlock.add(title);
            offerBlock.add(description);

        }
        return offerBlock;
    }

    protected void extractData() {

        final Session session = Context.getSession();
        author = new HtmlParagraph(Context.tr("Author : ") + offer.getAuthor().getFullname(), "offer_author");
        price = new HtmlParagraph(Context.tr("Price : ") + "Unknown yet", "offer_price");
        expirationDate = new HtmlParagraph(Context.tr("Expiration date : ") + offer.getDateExpire().toString(), "offer_expiry_date");
        authorAvatar = new HtmlImage(offer.getAuthor().getAvatar(), "offer_avatar");
        creationDate = new HtmlParagraph(Context.tr("Creation Date : ") + offer.getCreationDate().toString(), "offer_creation_date");

        title = new HtmlParagraph(offer.getDescription().getDefaultTranslation().getTitle(), "offer_title");
        description = new HtmlParagraph(offer.getDescription().getDefaultTranslation().getTitle(), "offer_description");

    }
}
