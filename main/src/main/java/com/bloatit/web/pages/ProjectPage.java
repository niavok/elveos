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
package com.bloatit.web.pages;

import static com.bloatit.framework.webserver.Context.tr;

import com.bloatit.framework.exceptions.RedirectException;
import com.bloatit.framework.exceptions.UnauthorizedOperationException;
import com.bloatit.framework.webserver.PageNotFoundException;
import com.bloatit.framework.webserver.annotations.Message.Level;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.annotations.RequestParam;
import com.bloatit.framework.webserver.components.HtmlParagraph;
import com.bloatit.framework.webserver.components.HtmlTitleBlock;
import com.bloatit.model.Project;
import com.bloatit.web.pages.master.MasterPage;
import com.bloatit.web.url.ProjectPageUrl;

@ParamContainer("member")
public final class ProjectPage extends MasterPage {

    public static final String PROJECT_FIELD_NAME = "id";

    @RequestParam(name = PROJECT_FIELD_NAME, level = Level.ERROR)
    private final Project project;

    private final ProjectPageUrl url;

    public ProjectPage(final ProjectPageUrl url) {
        super(url);
        this.url = url;
        this.project = url.getProject();
    }

    @Override
    protected void doCreate() throws RedirectException {
        if (url.getMessages().hasMessage(Level.ERROR)) {
            throw new PageNotFoundException();
        }

        project.authenticate(session.getAuthToken());

        try {
            HtmlTitleBlock projectName;
            projectName = new HtmlTitleBlock(project.getName(), 1);

            add(projectName);
        } catch (final UnauthorizedOperationException e) {
            add(new HtmlParagraph(tr("For obscure reasons, you are not allowed to see the details of this project.")));
        }
    }

    @Override
    protected String getPageTitle() {
        if (project != null) {
            try {
                return tr("Project - ") + project.getName();
            } catch (final UnauthorizedOperationException e) {
                return tr("Project - Windows 8");
            }
        }
        return tr("Member - No member");
    }

    @Override
    public boolean isStable() {
        return true;
    }
}
