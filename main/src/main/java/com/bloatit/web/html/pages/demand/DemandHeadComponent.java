/*
 * Copyright (C) 2010 BloatIt.
 *
 * This file is part of BloatIt.
 *
 * BloatIt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BloatIt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.web.html.pages.demand;


import com.bloatit.framework.Demand;
import com.bloatit.web.html.components.standard.HtmlDiv;
import com.bloatit.web.html.pages.master.HtmlPageComponent;

public class DemandHeadComponent extends HtmlPageComponent {

    public DemandHeadComponent(final Demand demand) {
        super();
        final HtmlDiv demandHead = new HtmlDiv("demand_head");
        {
            // Add progress bar
            final HtmlDiv demandHeadProgress = new HtmlDiv("demand_head_progress");
            {
                demandHeadProgress.add(new DemandProgressBarComponent(demand));
            }
            demandHead.add(demandHeadProgress);

            // Add kudo box
            final HtmlDiv demandHeadKudo = new HtmlDiv("demand_head_kudo");
            {
                demandHeadKudo.add(new DemandKudoComponent(demand));
            }
            demandHead.add(demandHeadKudo);

        }
        add(demandHead);
    }
}
