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
package com.bloatit.web.linkable.contribution;

import com.bloatit.framework.webprocessor.annotations.Optional;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.web.linkable.master.LoggedElveosPage;
import com.bloatit.web.url.QuotationPageUrl;

@ParamContainer("quotationPage")
public abstract class QuotationPage extends LoggedElveosPage {

    @Optional("false")
    @RequestParam(name = "show_fees_detail")
    private final Boolean showFeesDetails;

    protected QuotationPage(final QuotationPageUrl url) {
        super(url);
        showFeesDetails = url.getShowFeesDetails();
    }

    protected boolean hasToShowFeeDetails() {
        return showFeesDetails;
    }
}
