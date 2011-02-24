package com.bloatit.web.pages.admin;

import static com.bloatit.framework.webserver.Context.tr;

import java.util.EnumSet;

import com.bloatit.data.DaoBatch;
import com.bloatit.data.DaoBug.Level;
import com.bloatit.framework.utils.i18n.DateLocale.FormatStyle;
import com.bloatit.framework.webserver.Context;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.annotations.RequestParam;
import com.bloatit.framework.webserver.components.HtmlParagraph;
import com.bloatit.framework.webserver.components.PlaceHolderElement;
import com.bloatit.framework.webserver.components.advanced.HtmlGenericTableModel;
import com.bloatit.framework.webserver.components.advanced.HtmlGenericTableModel.ColumnGenerator;
import com.bloatit.framework.webserver.components.advanced.HtmlGenericTableModel.StringColumnGenerator;
import com.bloatit.framework.webserver.components.form.HtmlDropDown;
import com.bloatit.framework.webserver.components.form.HtmlForm;
import com.bloatit.framework.webserver.components.meta.HtmlBranch;
import com.bloatit.framework.webserver.components.meta.XmlNode;
import com.bloatit.model.Batch;
import com.bloatit.model.Release;
import com.bloatit.model.admin.BatchAdminListFactory;
import com.bloatit.web.actions.AdministrationAction;
import com.bloatit.web.url.BatchAdminPageUrl;

@ParamContainer("admin/batches")
public final class BatchAdminPage extends IdentifiablesAdminPage<DaoBatch, Batch, BatchAdminListFactory> {

    @RequestParam(role = RequestParam.Role.POST)
    private DisplayableBatchState batchState;

    private final BatchAdminPageUrl url;

    public BatchAdminPage(final BatchAdminPageUrl url) {
        super(url, new BatchAdminListFactory());
        this.url = url;
        batchState = url.getBatchState();
    }

    @Override
    protected String getPageTitle() {
        return tr("Administration Kudosable");
    }

    @Override
    public boolean isStable() {
        return true;
    }

    @Override
    protected void addActions(final HtmlDropDown dropDown, final HtmlBranch block) {
        // Add actions into the drop down
        dropDown.addDropDownElements(new AdminActionManager().batchActions());

        // add a demand state selector
        final HtmlDropDown demandState = new HtmlDropDown(AdministrationAction.DEMAND_STATE_CODE);
        demandState.addDropDownElements(EnumSet.allOf(DisplayableBatchState.class));
        demandState.setLabel(tr("Change the batch state"));
        block.add(demandState);
    }

    @Override
    protected void addFormFilters(final HtmlForm form) {

        final HtmlDropDown state = new HtmlDropDown(url.getBatchStateParameter().formFieldData());
        state.addDropDownElements(EnumSet.allOf(DisplayableBatchState.class));
        state.setLabel(tr("Filter by batch state"));
        form.add(state);
    }

    @Override
    protected void addColumns(final HtmlGenericTableModel<Batch> tableModel) {
        BatchAdminPageUrl clonedUrl = url.clone();
        clonedUrl.setOrderByStr("batchState");
        tableModel.addColumn(clonedUrl.getHtmlLink(tr("contribution")), new StringColumnGenerator<Batch>() {
            @Override
            public String getStringBody(final Batch element) {
                return String.valueOf(element.getBatchState());
            }
        });
        tableModel.addColumn(tr("description"), new StringColumnGenerator<Batch>() {
            @Override
            public String getStringBody(final Batch element) {
                return element.getDescription();
            }
        });
        tableModel.addColumn(tr("Release"), new ColumnGenerator<Batch>() {
            @Override
            public XmlNode getBody(final Batch element) {

                PlaceHolderElement place = new PlaceHolderElement();
                for (Release release : element.getReleases()) {
                    place.add(new HtmlParagraph(release.getVersion() + " "
                            + Context.getLocalizator().getDate(release.getCreationDate()).toString(FormatStyle.MEDIUM)));
                }
                return place;
            }
        });
        tableModel.addColumn(tr("Should validated Fatal"), new StringColumnGenerator<Batch>() {
            @Override
            public String getStringBody(final Batch element) {
                return String.valueOf(element.shouldValidatePart(Level.FATAL));
            }
        });
        tableModel.addColumn(tr("Major"), new StringColumnGenerator<Batch>() {
            @Override
            public String getStringBody(final Batch element) {
                return String.valueOf(element.shouldValidatePart(Level.MAJOR));
            }
        });
        tableModel.addColumn(tr("Minor"), new StringColumnGenerator<Batch>() {
            @Override
            public String getStringBody(final Batch element) {
                return String.valueOf(element.shouldValidatePart(Level.MINOR));
            }
        });

    }
}