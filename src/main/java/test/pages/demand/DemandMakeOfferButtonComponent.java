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
package test.pages.demand;

import test.Context;
import test.HtmlElement;
import test.Parameters;
import test.Request;
import test.pages.components.HtmlBlock;
import test.pages.components.HtmlButton;
import test.pages.components.HtmlForm;

import com.bloatit.framework.Demand;
import com.bloatit.web.pages.OfferPage;
import com.bloatit.web.server.Session;

public class DemandMakeOfferButtonComponent extends HtmlElement {

    public DemandMakeOfferButtonComponent(Request request, Demand demand) {
        super();
        Session session = Context.getSession();

        final HtmlBlock makeOfferBlock = new HtmlBlock("make_offer_block");
        {

            HtmlForm makeOfferForm = new HtmlForm(new OfferPage(session, new Parameters("idea", String.valueOf(demand.getId()))));
            {
                HtmlButton makeOfferButton = new HtmlButton(session.tr("Make an offer"));
                makeOfferForm.add(makeOfferButton);
            }
            makeOfferBlock.add(makeOfferForm);
        }
        add(makeOfferBlock);
    }
}
